/*
 * Purpose: Exposes the web portal health endpoint.
 * Why it exists: Gives Docker Compose, CI, and future orchestrators a stable readiness signal.
 * Architecture fit: Implements the approved observability foundation for frontend services.
 */
export function GET() {
  return Response.json({
    service: 'web-portal',
    status: 'ok',
  });
}
