# Stage 1: Build Angular Production Application
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies with clean cache
COPY package*.json ./
RUN npm ci

# Copy source and build distribution
COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Production Nginx Server
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Clean default assets
RUN rm -rf ./*

# Copy compiled Angular distribution
COPY --from=build /app/dist/frontend/browser .

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
