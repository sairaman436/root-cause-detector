/*
 * Purpose: Provides a minimal non-business surface for admin portal verification.
 * Why it exists: Confirms the admin portal runtime is operational without implementing administration workflows.
 * Architecture fit: Supports local development and frontend deployment readiness.
 */
export default function AdminPortalFoundationPage() {
  return <main data-testid="admin-portal-foundation">Admin portal foundation is operational.</main>;
}
