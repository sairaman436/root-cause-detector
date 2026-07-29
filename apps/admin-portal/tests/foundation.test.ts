// Purpose: Verifies that the admin portal health route is wired.
// Why it exists: Gives frontend CI a meaningful foundation test before admin workflows exist.
// Architecture fit: Supports container and orchestrator readiness checks for the admin portal.
import { describe, expect, it } from 'vitest';

import { GET } from '../src/app/api/health/route';

describe('admin portal foundation', () => {
  it('returns a healthy service contract', async () => {
    const body = await GET().json();

    expect(body).toEqual({
      service: 'admin-portal',
      status: 'ok',
    });
  });
});
