# Smart Building Digital Twin Frontend

Part of the **Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System**.

## Overview
The frontend is a dark-first industrial control center built with React, TypeScript, and Vite. In Phase 1, it provides the core operational shell, navigation console, real-time subsystem heartbeat monitoring, and structural placeholder routing for all 7 project milestones.

## Technology Stack
- **Framework**: React 18 + TypeScript + Vite
- **Styling**: Tailwind CSS
- **Routing**: React Router DOM (v6)
- **HTTP Client**: Axios
- **Graphics/3D**: Three.js, React Three Fiber, React Three Drei
- **Icons**: Lucide React
- **Testing**: Vitest + React Testing Library + JSDOM

## Routes Configured
- `/dashboard`: Primary system health, edge telemetry, and pipeline topology.
- `/buildings`: Building, campus, floor, and zone hierarchy management (Phase 2).
- `/sensors`: Virtual IoT sensor configuration and stream monitor (Phase 3).
- `/digital-twin`: 3D multi-floor structural digital twin viewer (Phase 5).
- `/occupancy`: Machine-learning occupancy estimation and utilization (Phase 3/4).
- `/safety`: Real-time IAQ, environmental anomaly, and hazard detection (Phase 4).
- `/emergency`: Automated evacuation routing and emergency alarms (Phase 6).
- `/analytics`: Long-term time-series building efficiency reporting (Phase 7).
- `/settings`: Platform gateway, WebSocket broker, and telemetry settings.

## Local Execution

### 1. Install Dependencies
```bash
npm install
```

### 2. Configure Environment
Set `VITE_API_BASE_URL` in `.env` (default is `http://localhost:8080/api/v1`):
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### 3. Run Development Server
```bash
npm run dev
```
Open `http://localhost:5173` in your browser.

### 4. Run Automated Tests
```bash
npm test
```

### 5. Build for Production
```bash
npm run build
```
