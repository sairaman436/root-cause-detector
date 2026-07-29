// Purpose: Verifies that the web portal health route is wired.
// Why it exists: Gives frontend CI a meaningful foundation test before product UI exists.
// Architecture fit: Supports container and orchestrator readiness checks for the frontend.
import { describe, expect, it } from 'vitest';

import { GET } from '../src/app/api/health/route';

describe('web portal foundation', () => {
  it('returns a healthy service contract', async () => {
    const body = await GET().json();

    expect(body).toEqual({
      service: 'web-portal',
      status: 'ok',
    });
  });
});
