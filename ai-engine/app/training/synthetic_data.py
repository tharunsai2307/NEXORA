import numpy as np
from typing import Tuple


def generate_synthetic_anomaly_dataset(n_samples: int = 2000, contamination: float = 0.05) -> np.ndarray:
    """Generates physically consistent multi-modal sensor dataset for Isolation Forest training.
    
    Feature columns (8 features):
    0: temperature (C)
    1: humidity (%)
    2: smoke (ppm)
    3: air_quality (AQI)
    4: occupancy (people)
    5: utilization_pct (%)
    6: temp_diff (C)
    7: smoke_diff (ppm)
    """
    np.random.seed(42)
    n_anomalies = int(n_samples * contamination)
    n_normal = n_samples - n_anomalies

    # 1. Normal operating samples
    norm_temp = np.random.normal(22.5, 1.8, n_normal)
    norm_temp = np.clip(norm_temp, 18.0, 27.5)

    norm_hum = np.random.normal(48.0, 6.0, n_normal)
    norm_hum = np.clip(norm_hum, 32.0, 64.0)

    norm_smoke = np.random.exponential(0.4, n_normal)
    norm_smoke = np.clip(norm_smoke, 0.0, 2.2)

    norm_aqi = np.random.normal(38.0, 10.0, n_normal)
    norm_aqi = np.clip(norm_aqi, 15.0, 75.0)

    norm_cap = np.random.choice([30, 50, 80], n_normal)
    norm_util = np.random.beta(2, 3, n_normal) * 85.0
    norm_occ = np.round((norm_util / 100.0) * norm_cap)
    norm_util_pct = (norm_occ / norm_cap) * 100.0

    norm_temp_diff = np.random.normal(0.0, 0.4, n_normal)
    norm_smoke_diff = np.random.normal(0.0, 0.1, n_normal)

    normal_matrix = np.column_stack([
        norm_temp, norm_hum, norm_smoke, norm_aqi,
        norm_occ, norm_util_pct, norm_temp_diff, norm_smoke_diff
    ])

    # 2. Injected realistic anomalous samples
    anom_types = np.random.choice(["fire", "hvac_fail", "aqi_pollute", "overcrowd"], n_anomalies)

    anom_temp = np.random.normal(22.5, 1.8, n_anomalies)
    anom_hum = np.random.normal(48.0, 6.0, n_anomalies)
    anom_smoke = np.random.exponential(0.4, n_anomalies)
    anom_aqi = np.random.normal(38.0, 10.0, n_anomalies)
    anom_cap = np.random.choice([30, 50, 80], n_anomalies)
    anom_occ = np.random.uniform(5, 30, n_anomalies)
    anom_temp_diff = np.zeros(n_anomalies)
    anom_smoke_diff = np.zeros(n_anomalies)

    for i, a_type in enumerate(anom_types):
        if a_type == "fire":
            anom_smoke[i] = np.random.uniform(15.0, 85.0)
            anom_temp[i] = np.random.uniform(36.0, 62.0)
            anom_temp_diff[i] = np.random.uniform(8.0, 25.0)
            anom_smoke_diff[i] = np.random.uniform(12.0, 70.0)
        elif a_type == "hvac_fail":
            anom_temp[i] = np.random.choice([np.random.uniform(33.0, 44.0), np.random.uniform(10.0, 15.0)])
            anom_hum[i] = np.random.choice([np.random.uniform(10.0, 20.0), np.random.uniform(80.0, 95.0)])
            anom_temp_diff[i] = np.random.uniform(6.0, 14.0)
        elif a_type == "aqi_pollute":
            anom_aqi[i] = np.random.uniform(180.0, 420.0)
        elif a_type == "overcrowd":
            anom_occ[i] = anom_cap[i] * np.random.uniform(1.15, 1.6)

    anom_util_pct = (anom_occ / anom_cap) * 100.0

    anom_matrix = np.column_stack([
        anom_temp, anom_hum, anom_smoke, anom_aqi,
        anom_occ, anom_util_pct, anom_temp_diff, anom_smoke_diff
    ])

    dataset = np.vstack([normal_matrix, anom_matrix])
    np.random.shuffle(dataset)
    return dataset


def generate_synthetic_occupancy_dataset(n_samples: int = 2400) -> Tuple[np.ndarray, np.ndarray]:
    """Generates structured time-series occupancy sequence for 15-minute forecasting.
    
    Feature columns (8 features):
    0: current_occupancy
    1: capacity
    2: utilization_pct
    3: hour_of_day (0-23)
    4: day_of_week (0-6)
    5: room_type_idx (0=OFFICE, 1=CLASSROOM, 2=LAB, 3=CONFERENCE, 4=OTHER)
    6: recent_avg
    7: occupancy_velocity
    
    Target:
    y: occupancy 15 minutes ahead (clamped to capacity)
    """
    np.random.seed(42)
    X_list = []
    y_list = []

    room_types = [0, 1, 2, 3]
    capacities = [30, 40, 50, 70, 100]

    for _ in range(n_samples):
        hour = np.random.randint(0, 24)
        dow = np.random.randint(0, 7)
        r_type = np.random.choice(room_types)
        cap = np.random.choice(capacities)

        # Baseline arrival distribution based on hour and weekend
        if dow >= 5:  # Weekend
            base_rate = 0.05
        else:
            if 8 <= hour < 12:
                base_rate = 0.70
            elif 12 <= hour < 14:
                base_rate = 0.50
            elif 14 <= hour < 18:
                base_rate = 0.65
            elif 18 <= hour < 21:
                base_rate = 0.20
            else:
                base_rate = 0.02

        curr_occ = int(np.clip(np.random.normal(base_rate * cap, 0.15 * cap), 0, cap))
        util_pct = (curr_occ / cap) * 100.0

        # Simulate recent trend
        vel = np.random.choice([-3, -2, -1, 0, 1, 2, 3])
        recent_avg = float(np.clip(curr_occ - vel * 0.5, 0, cap))

        # Target after 15 min: drift based on velocity and mean reversion
        drift = int(vel * 0.8 + np.random.choice([-1, 0, 1]))
        target_occ = int(np.clip(curr_occ + drift, 0, cap))

        X_list.append([
            curr_occ, cap, util_pct, hour, dow, r_type, recent_avg, float(vel)
        ])
        y_list.append(target_occ)

    return np.array(X_list, dtype=float), np.array(y_list, dtype=float)

