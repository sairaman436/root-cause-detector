/*
 * Purpose: Provides a minimal non-business landing surface for local service verification.
 * Why it exists: Confirms the web portal runtime is operational without implementing product workflows.
 * Architecture fit: Supports frontend container health and developer onboarding.
 */
export default function WebPortalFoundationPage() {
  return <main data-testid="web-portal-foundation">Web portal foundation is operational.</main>;
}
