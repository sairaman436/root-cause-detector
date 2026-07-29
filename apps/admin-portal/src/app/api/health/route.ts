/*
 * Purpose: Exposes the admin portal health endpoint.
 * Why it exists: Gives Docker Compose, CI, and future orchestrators a stable readiness signal.
 * Architecture fit: Implements the approved observability foundation for administration services.
 */
export function GET() {
  return Response.json({
    service: 'admin-portal',
    status: 'ok',
  });
}
