# ===========================================================
# Frontend Dockerfile — React 18 / Vite
# Multi-stage: Node 20 build → Nginx 1.27 (Alpine) serve
# ===========================================================

# ---- Stage 1: Build ----
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies (layer cached until package-lock.json changes)
COPY package*.json ./
RUN npm ci --silent

# Copy source
COPY . .

# VITE_API_BASE_URL is kept as /api so Nginx handles the proxy.
# Override at build time with --build-arg if your setup differs.
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ---- Stage 2: Serve ----
FROM nginx:1.27-alpine AS runtime

# Replace default Nginx config with our SPA + reverse-proxy config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built static assets
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
