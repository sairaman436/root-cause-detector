/*
 * Purpose: Configures the admin portal test runner.
 * Why it exists: Provides a local and CI verification surface before admin workflows are implemented.
 * Architecture fit: Supports frontend quality gates for the administration application boundary.
 */
import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'node',
    include: ['tests/**/*.test.ts', 'tests/**/*.test.tsx'],
  },
});
