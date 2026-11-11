import os
import joblib
import numpy as np
from typing import Dict, Any, Tuple
from datetime import datetime, timezone
from sklearn.ensemble import IsolationForest

from app.schemas.anomaly import AnomalyInferenceRequest, AnomalyInferenceResponse
from app.utils.logger import setup_logger

logger = setup_logger("anomaly_detector")


class AnomalyDetector:
    """Isolation Forest based multi-modal environmental and occupancy anomaly detector."""

    MODEL_VERSION = "v1.0.0"
    MODEL_FILENAME = "isolation_forest_v1.joblib"

    FEATURE_NAMES = [
        "temperature",
        "humidity",
        "smoke",
        "air_quality",
        "occupancy",
        "utilization_pct",
        "temp_diff",
        "smoke_diff",
    ]

    def __init__(self, artifacts_dir: str):
        self.artifacts_dir = artifacts_dir
        self.model_path = os.path.join(artifacts_dir, self.MODEL_FILENAME)
        self.model: IsolationForest | None = None
        self.is_ready = False
        self._load_or_init()

    def _load_or_init(self):
        if os.path.exists(self.model_path):
            try:
                self.model = joblib.load(self.model_path)
                self.is_ready = True
                logger.info(f"Loaded existing Isolation Forest model from {self.model_path}")
                return
            except Exception as e:
                logger.warning(f"Failed to load existing model artifact: {e}. Reinitializing.")

        # If not present, initialize with standard parameters
        self.model = IsolationForest(
            n_estimators=100,
            contamination=0.05,
            random_state=42,
            n_jobs=-1
        )
        self.is_ready = False

    def train(self, X: np.ndarray, save: bool = True) -> Dict[str, Any]:
        """Trains the Isolation Forest model on multi-modal feature matrix X."""
        logger.info(f"Training Isolation Forest on dataset shape {X.shape}...")
        self.model.fit(X)
        self.is_ready = True

        if save:
            os.makedirs(self.artifacts_dir, exist_ok=True)
            joblib.dump(self.model, self.model_path)
            logger.info(f"Saved trained Isolation Forest model to {self.model_path}")

        preds = self.model.predict(X)
        anomaly_ratio = float(np.mean(preds == -1))

        metrics = {
            "n_estimators": self.model.n_estimators,
            "contamination": float(self.model.contamination),
            "training_samples": len(X),
            "anomaly_rate": round(anomaly_ratio, 4),
            "features": self.FEATURE_NAMES,
        }
        return metrics

    def extract_features(self, req: AnomalyInferenceRequest) -> Tuple[np.ndarray, Dict[str, float]]:
        """Extracts and formats ordered feature vector and dictionary from request."""
        temp = req.temperature if req.temperature is not None else 22.0
        hum = req.humidity if req.humidity is not None else 45.0
        smoke = req.smoke if req.smoke is not None else 0.5
        aqi = req.air_quality if req.air_quality is not None else 40.0
        occ = req.occupancy if req.occupancy is not None else 10
        cap = max(1, req.capacity)
        util_pct = (occ / cap) * 100.0

        temp_diff = (temp - req.temp_recent_avg) if req.temp_recent_avg is not None else 0.0
        smoke_diff = (smoke - req.smoke_recent_avg) if req.smoke_recent_avg is not None else 0.0

        features_dict = {
            "temperature": round(temp, 2),
            "humidity": round(hum, 2),
            "smoke": round(smoke, 2),
            "air_quality": round(aqi, 2),
            "occupancy": float(occ),
            "utilization_pct": round(util_pct, 1),
            "temp_diff": round(temp_diff, 2),
            "smoke_diff": round(smoke_diff, 2),
        }

        feature_vector = np.array([[
            temp, hum, smoke, aqi, occ, util_pct, temp_diff, smoke_diff
        ]], dtype=float)

        return feature_vector, features_dict

    def detect(self, req: AnomalyInferenceRequest) -> AnomalyInferenceResponse:
        """Executes anomaly detection inference and returns explainable structured response."""
        vec, feat_dict = self.extract_features(req)

        # Baseline raw score from model if ready, or heuristic fallback
        if self.is_ready and self.model is not None:
            raw_score = float(self.model.decision_function(vec)[0])
            # Model score: positive inliers, negative outliers around -0.3 to +0.3
            calibrated_score = float(np.clip(0.5 - (raw_score * 1.6), 0.0, 1.0))
        else:
            # Heuristic calculation if model not yet trained
            calibrated_score = 0.1

        # Domain safety adjustments
        reasons = []
        is_override = False

        smoke_val = feat_dict["smoke"]
        temp_val = feat_dict["temperature"]
        aqi_val = feat_dict["air_quality"]
        occ_val = int(feat_dict["occupancy"])
        cap_val = max(1, req.capacity)

        # Smoke rule
        if smoke_val > 15.0:
            calibrated_score = max(calibrated_score, 0.92)
            reasons.append(f"Critical smoke level detected ({smoke_val:.1f} ppm, normal < 2.0 ppm).")
            is_override = True
        elif smoke_val > 5.0:
            calibrated_score = max(calibrated_score, 0.72)
            reasons.append(f"Elevated smoke concentration ({smoke_val:.1f} ppm).")

        # Temperature rule
        if temp_val > 45.0:
            calibrated_score = max(calibrated_score, 0.88)
            reasons.append(f"Severe ambient thermal surge ({temp_val:.1f}°C).")
            is_override = True
        elif temp_val > 32.0 or temp_val < 15.0:
            calibrated_score = max(calibrated_score, 0.65)
            reasons.append(f"Unusual temperature departure ({temp_val:.1f}°C, baseline 20-26°C).")

        # Air quality rule
        if aqi_val > 250.0:
            calibrated_score = max(calibrated_score, 0.80)
            reasons.append(f"Hazardous air quality index ({aqi_val:.0f} AQI).")
        elif aqi_val > 120.0:
            calibrated_score = max(calibrated_score, 0.58)
            reasons.append(f"Poor air quality ({aqi_val:.0f} AQI).")

        # Occupancy capacity rule
        if occ_val > cap_val:
            calibrated_score = max(calibrated_score, 0.75)
            reasons.append(f"Occupancy overflow ({occ_val} people exceeds capacity of {cap_val}).")

        # Severity mapping
        if calibrated_score >= 0.85:
            severity = "CRITICAL"
            anomaly_detected = True
        elif calibrated_score >= 0.65:
            severity = "HIGH"
            anomaly_detected = True
        elif calibrated_score >= 0.50:
            severity = "MEDIUM"
            anomaly_detected = True
        elif calibrated_score >= 0.35:
            severity = "LOW"
            anomaly_detected = False
        else:
            severity = "NORMAL"
            anomaly_detected = False

        if not reasons:
            explanation = "Nominal conditions. All environmental metrics and occupant density align with learned room baseline."
        else:
            explanation = " ".join(reasons)

        now_iso = datetime.now(timezone.utc).isoformat()

        return AnomalyInferenceResponse(
            room_id=req.room_id,
            detection_timestamp=req.timestamp or now_iso,
            anomaly_detected=anomaly_detected,
            anomaly_score=round(calibrated_score, 4),
            severity=severity,
            contributing_features=feat_dict,
            explanation=explanation,
            model_version=self.MODEL_VERSION,
        )

