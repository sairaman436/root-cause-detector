# Purpose: Starts the full local platform foundation.
# Why it exists: Gives developers one command to build and run every operational service shell plus shared infrastructure.
# Architecture fit: Supports Milestone 2 feature development on top of the approved service topology without adding business behavior.
docker compose up -d --build
