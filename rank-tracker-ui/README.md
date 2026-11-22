# RankPulse UI — Frontend Client

The frontend client for **RankPulse – SEO Rank Tracking & Analytics Platform**, built with **React 19**, **Vite**, and **Recharts**.

## Getting Started

### Install Dependencies
```bash
npm install
```

### Run Development Server
```bash
npm run dev
```
The application will be accessible at `http://localhost:5173`.

### Production Build
```bash
npm run build
```
Build output is generated in the `dist/` directory.

### Configuration
API requests proxy to the Spring Boot backend at `http://localhost:8085/api`.
Configure the backend endpoint in `src/services/api.js`.
