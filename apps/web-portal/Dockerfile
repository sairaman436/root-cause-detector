# Purpose: Builds and runs the Next.js web portal service shell.
# Why it exists: Gives the frontend an independently deployable container with health checks before product UI implementation.
# Architecture fit: Supports the approved containerized deployment model for user-facing applications.
FROM node:20-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
COPY apps/web-portal/package.json apps/web-portal/package.json
COPY apps/admin-portal/package.json apps/admin-portal/package.json
RUN npm ci
COPY apps/web-portal apps/web-portal
RUN npm run build:web

FROM node:20-alpine

ENV NODE_ENV=production
WORKDIR /app
COPY --from=build /app/apps/web-portal/.next/standalone ./
COPY --from=build /app/apps/web-portal/.next/static ./apps/web-portal/.next/static
EXPOSE 3000
CMD ["node", "apps/web-portal/server.js"]
