// Purpose: Defines Vitest configuration for the web portal.
// Why it exists: Establishes the frontend test runner before business UI tests are added.
// Architecture fit: Supports the approved frontend testing foundation.
import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'node',
    include: ['tests/**/*.test.ts', 'tests/**/*.test.tsx'],
  },
});
