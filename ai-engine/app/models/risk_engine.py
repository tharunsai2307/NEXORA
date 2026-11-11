from typing import Dict, Any, Tuple
from datetime import datetime, timezone
import numpy as np

from app.schemas.risk import RiskAssessmentRequest, RiskAssessmentResponse
from app.utils.logger import setup_logger

logger = setup_logger("risk_engine")


class RiskEngine:
    """Explainable Multi-Criteria Room Safety Risk Assessment Engine.
    
    Formula: R = 0.35*T + 0.35*S + 0.20*A + 0.10*O
    Includes autonomous Critical Smoke Override Rule for life safety assurance.
    """

    @staticmethod
    def calculate_temperature_score(temp: float) -> float:
        """Normalizes temperature reading in Celsius to 0-100 risk scale."""
        if temp <= 22.0:
            return 5.0
        elif temp <= 28.0:
            # 22-28C: nominal comfort range (5-25 risk)
            return 5.0 + ((temp - 22.0) / 6.0) * 20.0
        elif temp <= 38.0:
            # 28-38C: thermal discomfort & mild hazard (25-60 risk)
            return 25.0 + ((temp - 28.0) / 10.0) * 35.0
        elif temp <= 55.0:
            # 38-55C: extreme heat / combustion proximity (60-95 risk)
            return 60.0 + ((temp - 38.0) / 17.0) * 35.0
        else:
            return 100.0

    @staticmethod
    def calculate_smoke_score(smoke_ppm: float) -> float:
        """Normalizes smoke concentration in ppm to 0-100 risk scale."""
        if smoke_ppm <= 1.0:
            return 2.0
        elif smoke_ppm <= 4.0:
            return 2.0 + ((smoke_ppm - 1.0) / 3.0) * 18.0
        elif smoke_ppm <= 12.0:
            return 20.0 + ((smoke_ppm - 4.0) / 8.0) * 40.0
        elif smoke_ppm <= 30.0:
            return 60.0 + ((smoke_ppm - 12.0) / 18.0) * 35.0
        else:
            return 100.0

    @staticmethod
    def calculate_air_quality_score(aqi: float) -> float:
        """Normalizes AQI (0-500) to 0-100 risk scale."""
        if aqi <= 50.0:
            return 5.0
        elif aqi <= 100.0:
            return 5.0 + ((aqi - 50.0) / 50.0) * 20.0
        elif aqi <= 200.0:
            return 25.0 + ((aqi - 100.0) / 100.0) * 35.0
        elif aqi <= 350.0:
            return 60.0 + ((aqi - 200.0) / 150.0) * 30.0
        else:
            return 100.0

    @staticmethod
    def calculate_occupancy_score(occ: int, cap: int) -> float:
        """Normalizes room utilization to 0-100 risk scale."""
        if cap <= 0:
            return 0.0
        util = (occ / cap) * 100.0
        if util <= 60.0:
            return (util / 60.0) * 20.0
        elif util <= 90.0:
            return 20.0 + ((util - 60.0) / 30.0) * 30.0
        elif util <= 100.0:
            return 50.0 + ((util - 90.0) / 10.0) * 25.0
        else:
            # Overcrowding penalty
            overage = min(50.0, util - 100.0)
            return min(100.0, 75.0 + overage * 0.5)

    def assess(self, req: RiskAssessmentRequest) -> RiskAssessmentResponse:
        """Computes multi-component explainable risk score and checks critical fire overrides."""
        temp = req.temperature if req.temperature is not None else 22.0
        smoke = req.smoke if req.smoke is not None else 0.5
        aqi = req.air_quality if req.air_quality is not None else 40.0
        occ = req.occupancy if req.occupancy is not None else 10
        cap = max(1, req.capacity)

        # 1. Component calculations
        t_score = round(self.calculate_temperature_score(temp), 1)
        s_score = round(self.calculate_smoke_score(smoke), 1)
        a_score = round(self.calculate_air_quality_score(aqi), 1)
        o_score = round(self.calculate_occupancy_score(occ, cap), 1)
        anom_score = round(float(req.anomaly_score) * 100.0, 1)

        # 2. Weighted baseline formula: R = 0.35T + 0.35S + 0.20A + 0.10O
        base_risk = (0.35 * t_score) + (0.35 * s_score) + (0.20 * a_score) + (0.10 * o_score)

        # Factor in anomaly detection multiplier if active
        if req.is_anomaly or anom_score > 50.0:
            base_risk = max(base_risk, base_risk * 0.85 + (anom_score * 0.15))

        base_risk = round(float(np.clip(base_risk, 0.0, 100.0)), 1)

        # 3. CRITICAL OVERRIDE RULES (Fire Safety & Severe Hazard Invariants)
        critical_override = False
        reasons = []

        if s_score >= 70.0 or smoke >= 15.0:
            base_risk = max(base_risk, 88.0)
            critical_override = True
            reasons.append(f"CRITICAL OVERRIDE: Dangerous smoke density ({smoke:.1f} ppm) escalated risk to emergency level.")
        elif s_score >= 45.0:
            reasons.append(f"Elevated smoke readings ({smoke:.1f} ppm).")

        if t_score >= 75.0 or temp >= 45.0:
            base_risk = max(base_risk, 85.0)
            critical_override = True
            reasons.append(f"CRITICAL OVERRIDE: Thermal hazard ({temp:.1f}°C) escalated risk score.")
        elif t_score >= 40.0:
            reasons.append(f"High ambient temperature ({temp:.1f}°C).")

        if a_score >= 65.0:
            reasons.append(f"Degraded air quality ({aqi:.0f} AQI).")

        if o_score >= 75.0:
            reasons.append(f"Capacity overcrowding ({occ}/{cap} occupants).")

        # 4. Classification
        if base_risk >= 80.0:
            risk_level = "CRITICAL"
        elif base_risk >= 60.0:
            risk_level = "HIGH_RISK"
        elif base_risk >= 30.0:
            risk_level = "WARNING"
        else:
            risk_level = "NORMAL"

        if not reasons:
            explanation = "Room operating under optimal safety conditions. All physical parameters within nominal margins."
        else:
            explanation = " ".join(reasons)

        factors = {
            "temperature_score": t_score,
            "smoke_score": s_score,
            "air_quality_score": a_score,
            "occupancy_score": o_score,
            "anomaly_score": anom_score,
            "raw_temperature_c": round(temp, 1),
            "raw_smoke_ppm": round(smoke, 2),
            "raw_air_quality_aqi": round(aqi, 1),
            "raw_occupancy": float(occ),
        }

        now_iso = datetime.now(timezone.utc).isoformat()

        return RiskAssessmentResponse(
            room_id=req.room_id,
            overall_risk_score=base_risk,
            risk_level=risk_level,
            temperature_score=t_score,
            smoke_score=s_score,
            air_quality_score=a_score,
            occupancy_score=o_score,
            anomaly_score=anom_score,
            contributing_factors=factors,
            explanation=explanation,
            assessment_timestamp=now_iso,
            critical_override=critical_override,
        )

