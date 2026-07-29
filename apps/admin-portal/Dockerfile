# Purpose: Builds and runs the Next.js admin portal service shell.
# Why it exists: Gives the administration frontend an independently deployable container with health checks before workflows exist.
# Architecture fit: Supports the approved separated administration deployment boundary.
FROM node:20-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
COPY apps/web-portal/package.json apps/web-portal/package.json
COPY apps/admin-portal/package.json apps/admin-portal/package.json
RUN npm ci
COPY apps/admin-portal apps/admin-portal
RUN npm run build:admin

FROM node:20-alpine

ENV NODE_ENV=production
WORKDIR /app
COPY --from=build /app/apps/admin-portal/.next/standalone ./
COPY --from=build /app/apps/admin-portal/.next/static ./apps/admin-portal/.next/static
EXPOSE 3001
CMD ["node", "apps/admin-portal/server.js"]
