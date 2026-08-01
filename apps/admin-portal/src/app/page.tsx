/*
 * Purpose: Provides the Sprint 1 administration overview.
 * Why it exists: Operators need a concise view of enabled MVP services, quality gates, and integration surfaces.
 * Architecture fit: Administration portal boundary for platform operations without duplicating backend business logic.
 */
const services = [
  ['Core Backend', 'Identity, survey, evidence, AI, decision, reports, audit'],
  ['AI Inference Service', 'Ollama-compatible inference with deterministic local fallback'],
  ['RAG Service', 'Knowledge indexing and citation-preserving retrieval'],
  ['Agent Orchestrator', 'Workflow sequencing for survey-to-report paths'],
  ['Reporting Service', 'CSV and PDF rendering boundary'],
  ['Notification Service', 'Local auditable delivery boundary'],
];

export default function AdminPortalSprintOnePage() {
  return (
    <main
      style={{
        background: '#f4f7f6',
        color: '#17211d',
        fontFamily: 'Inter, system-ui, sans-serif',
        minHeight: '100vh',
        padding: 32,
      }}
    >
      <section style={{ margin: '0 auto', maxWidth: 1040 }}>
        <p
          style={{
            color: '#176b5b',
            fontSize: 12,
            fontWeight: 800,
            margin: 0,
            textTransform: 'uppercase',
          }}
        >
          Enterprise Rural Intelligence
        </p>
        <h1>Sprint 1 Administration</h1>
        <p>
          The MVP service topology is enabled for login, surveys, evidence, AI/RAG analysis,
          recommendations, reports, audit logging, and local development validation.
        </p>
        <div
          style={{
            display: 'grid',
            gap: 16,
            gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
          }}
        >
          {services.map(([name, description]) => (
            <article
              key={name}
              style={{
                background: '#fff',
                border: '1px solid #dae4e0',
                borderRadius: 8,
                boxShadow: '0 10px 30px rgba(20, 35, 31, 0.06)',
                padding: 18,
              }}
            >
              <h2 style={{ fontSize: 18, marginTop: 0 }}>{name}</h2>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
