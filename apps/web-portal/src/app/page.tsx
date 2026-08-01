'use client';

/*
 * Purpose: Provides the Sprint 1 integrated MVP dashboard.
 * Why it exists: Users need one executable workflow for login, survey creation, evidence upload, AI/RAG analysis, recommendations, reports, and profile/settings.
 * Architecture fit: Next.js web adapter that consumes the approved backend API contracts without owning business logic.
 */

import { ChangeEvent, FormEvent, useState } from 'react';

const API_BASE = process.env.NEXT_PUBLIC_CORE_BACKEND_URL ?? 'http://localhost:8080';
const PLATFORM_ORG_ID = '00000000-0000-0000-0000-000000000001';
const PLATFORM_ORG_CODE = 'PLATFORM';

type ApiEnvelope<T> = {
  data: T;
  requestId?: string;
};

type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  accessTokenExpiresAt: string;
};

type SurveyResponse = {
  id: string;
  organizationId: string;
  name: string;
  description: string;
  status: string;
  currentVersion: number;
};

type EvidenceResponse = {
  id: string;
  originalFileName: string;
  mimeType: string;
  sizeBytes: number;
  sha256Checksum: string;
  title: string;
};

type RagResponse = {
  id: string;
  answer: string;
  citations: Array<{ sourceType: string; sourceId: string; excerpt: string; score: number }>;
};

type DecisionResponse = {
  id: string;
  finalDecision: string;
  confidence: number;
  rootCauses: Array<{ title: string; description: string; confidence: number }>;
  recommendations: Array<{
    title: string;
    description: string;
    priority: number;
    confidence: number;
  }>;
};

type ReportResponse = {
  id: string;
  title: string;
  reportType: string;
  executiveSummary: string;
  contentMarkdown: string;
  pdfDownloadUrl: string;
  csvDownloadUrl: string;
};

type UserProfile = {
  id: string;
  username: string;
  email: string;
  fullName: string;
  organizationId: string;
  roles: string[];
  permissions: string[];
};

type WorkflowState = {
  token?: TokenResponse;
  profile?: UserProfile;
  survey?: SurveyResponse;
  evidence?: EvidenceResponse;
  rag?: RagResponse;
  decision?: DecisionResponse;
  report?: ReportResponse;
};

const navItems = [
  'Dashboard',
  'Login',
  'Survey',
  'Evidence Upload',
  'AI Assistant',
  'Reports',
  'User Profile',
  'Settings',
];

export default function WebPortalSprintOnePage() {
  const [active, setActive] = useState('Dashboard');
  const [state, setState] = useState<WorkflowState>({});
  const [message, setMessage] = useState('Ready to run Sprint 1 workflow.');
  const [error, setError] = useState('');
  const [email, setEmail] = useState('admin@platform.local');
  const [password, setPassword] = useState('ChangeMe12345!');
  const [surveyName, setSurveyName] = useState('Village Water Reliability Survey');
  const [surveyDescription, setSurveyDescription] = useState(
    'Field survey for identifying water access root causes.',
  );
  const [problemStatement, setProblemStatement] = useState(
    'Village households report unreliable water access and delayed repairs.',
  );
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
    const headers = new Headers(init.headers);
    if (!(init.body instanceof FormData)) {
      headers.set('Content-Type', 'application/json');
    }
    if (state.token) {
      headers.set('Authorization', `Bearer ${state.token.accessToken}`);
    }
    const response = await fetch(`${API_BASE}${path}`, {
      ...init,
      headers,
    });
    if (!response.ok) {
      const body = await response.text();
      throw new Error(body || `Request failed with ${response.status}`);
    }
    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.includes('application/json')) {
      return (await response.blob()) as T;
    }
    const envelope = (await response.json()) as ApiEnvelope<T>;
    return envelope.data;
  }

  async function submitLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await run('Logged in successfully.', async () => {
      let token: TokenResponse;
      try {
        token = await api<TokenResponse>('/api/v1/auth/login', {
          method: 'POST',
          body: JSON.stringify({ email, password }),
        });
      } catch {
        token = await api<TokenResponse>('/api/v1/auth/register', {
          method: 'POST',
          body: JSON.stringify({
            username: email.split('@')[0],
            email,
            fullName: 'Sprint One Administrator',
            password,
            organizationCode: PLATFORM_ORG_CODE,
          }),
        });
      }
      setState((current) => ({ ...current, token }));
      const profile = await fetchProfile(token.accessToken);
      setState((current) => ({ ...current, token, profile }));
    });
  }

  async function fetchProfile(accessToken: string) {
    const response = await fetch(`${API_BASE}/api/v1/users/me`, {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    return ((await response.json()) as ApiEnvelope<UserProfile>).data;
  }

  async function createSurvey() {
    await run('Survey created and stored in PostgreSQL.', async () => {
      const survey = await api<SurveyResponse>('/api/v1/surveys', {
        method: 'POST',
        body: JSON.stringify({
          organizationId: state.profile?.organizationId ?? PLATFORM_ORG_ID,
          name: surveyName,
          description: surveyDescription,
          tags: ['sprint-1', 'mvp', 'rural-intelligence'],
        }),
      });
      setState((current) => ({ ...current, survey }));
    });
  }

  async function uploadEvidence() {
    await run('Evidence uploaded, checksummed, stored, and audited.', async () => {
      if (!selectedFile) {
        throw new Error('Select a file before uploading evidence.');
      }
      const body = new FormData();
      body.append('file', selectedFile);
      body.append('organizationId', state.profile?.organizationId ?? PLATFORM_ORG_ID);
      if (state.survey?.id) body.append('surveyId', state.survey.id);
      body.append('title', selectedFile.name);
      body.append('description', 'Sprint 1 field evidence uploaded from the MVP dashboard.');
      body.append('tags', 'sprint-1');
      const evidence = await api<EvidenceResponse>('/api/v1/evidence', { method: 'POST', body });
      setState((current) => ({ ...current, evidence }));
    });
  }

  async function runAiWorkflow() {
    await run('RAG, root-cause analysis, and recommendations generated.', async () => {
      const rag = await api<RagResponse>('/api/v1/ai/rag/query', {
        method: 'POST',
        body: JSON.stringify({
          query: problemStatement,
          collectionName: 'knowledge',
          modelId: 'qwen2.5-local',
          topK: 5,
        }),
      });
      const decision = await api<DecisionResponse>('/api/v1/decision/analyze', {
        method: 'POST',
        body: JSON.stringify({
          surveyId: state.survey?.id,
          organizationId: state.profile?.organizationId ?? PLATFORM_ORG_ID,
          evidenceIds: state.evidence ? [state.evidence.id] : [],
          problemStatement,
          surveyEvidence: {
            surveyName: state.survey?.name,
            evidenceFile: state.evidence?.originalFileName,
            ragAnswer: rag.answer,
          },
          mlPredictions: { model: 'qwen2.5-local', confidence: 0.78 },
          agentOutputs: { knowledgeCitations: rag.citations },
          requireHumanApproval: true,
        }),
      });
      setState((current) => ({ ...current, rag, decision }));
    });
  }

  async function generateReport(reportType: 'EXECUTIVE' | 'VILLAGE' | 'DISTRICT') {
    await run(`${reportType} report generated with PDF and CSV exports.`, async () => {
      if (!state.decision) {
        throw new Error('Run AI analysis before generating a report.');
      }
      const report = await api<ReportResponse>('/api/v1/reports', {
        method: 'POST',
        body: JSON.stringify({
          decisionId: state.decision.id,
          surveyId: state.survey?.id,
          organizationId: state.profile?.organizationId ?? PLATFORM_ORG_ID,
          reportType,
          title: `${reportType.toLowerCase()} rural intelligence report`,
        }),
      });
      setState((current) => ({ ...current, report }));
    });
  }

  async function run(success: string, action: () => Promise<void>) {
    setError('');
    setMessage('Working...');
    try {
      await action();
      setMessage(success);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected workflow failure');
      setMessage('Workflow stopped.');
    }
  }

  function fileChanged(event: ChangeEvent<HTMLInputElement>) {
    setSelectedFile(event.target.files?.[0] ?? null);
  }

  return (
    <main className="appShell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">Enterprise Rural Intelligence</p>
          <h1>Sprint 1 MVP</h1>
        </div>
        <nav>
          {navItems.map((item) => (
            <button
              className={active === item ? 'active' : ''}
              key={item}
              onClick={() => setActive(item)}
              type="button"
            >
              {item}
            </button>
          ))}
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Operational workflow</p>
            <h2>{active}</h2>
          </div>
          <div className={state.token ? 'status ok' : 'status'}>
            {state.token ? 'Authenticated' : 'Not signed in'}
          </div>
        </header>

        <section className="notice">
          <span>{message}</span>
          {error ? <strong>{error}</strong> : null}
        </section>

        {active === 'Dashboard' ? (
          <section className="grid">
            <Metric label="Survey" value={state.survey?.status ?? 'Not created'} />
            <Metric label="Evidence" value={state.evidence ? 'Uploaded' : 'Not uploaded'} />
            <Metric
              label="AI Analysis"
              value={
                state.decision
                  ? `${Math.round(state.decision.confidence * 100)}% confidence`
                  : 'Pending'
              }
            />
            <Metric label="Report" value={state.report?.reportType ?? 'Pending'} />
            <article className="panel wide">
              <h3>End-to-end workflow</h3>
              <ol className="steps">
                <li>Login or auto-register the first administrator.</li>
                <li>Create a governed survey definition.</li>
                <li>Upload evidence to the backend storage abstraction.</li>
                <li>Run RAG and decision intelligence.</li>
                <li>Generate PDF and CSV reports.</li>
              </ol>
            </article>
          </section>
        ) : null}

        {active === 'Login' ? (
          <form className="panel form" onSubmit={submitLogin}>
            <label>
              Email
              <input onChange={(event) => setEmail(event.target.value)} value={email} />
            </label>
            <label>
              Password
              <input
                onChange={(event) => setPassword(event.target.value)}
                type="password"
                value={password}
              />
            </label>
            <button type="submit">Login</button>
          </form>
        ) : null}

        {active === 'Survey' ? (
          <section className="panel form">
            <label>
              Survey name
              <input onChange={(event) => setSurveyName(event.target.value)} value={surveyName} />
            </label>
            <label>
              Description
              <textarea
                onChange={(event) => setSurveyDescription(event.target.value)}
                value={surveyDescription}
              />
            </label>
            <button disabled={!state.token} onClick={createSurvey} type="button">
              Create Survey
            </button>
            <pre>{state.survey ? JSON.stringify(state.survey, null, 2) : 'No survey yet.'}</pre>
          </section>
        ) : null}

        {active === 'Evidence Upload' ? (
          <section className="panel form">
            <label>
              Evidence file
              <input onChange={fileChanged} type="file" />
            </label>
            <button disabled={!state.token || !state.survey} onClick={uploadEvidence} type="button">
              Upload Evidence
            </button>
            <pre>
              {state.evidence
                ? JSON.stringify(state.evidence, null, 2)
                : 'Create a survey, then upload evidence.'}
            </pre>
          </section>
        ) : null}

        {active === 'AI Assistant' ? (
          <section className="panel form">
            <label>
              Problem statement
              <textarea
                onChange={(event) => setProblemStatement(event.target.value)}
                value={problemStatement}
              />
            </label>
            <button disabled={!state.token || !state.survey} onClick={runAiWorkflow} type="button">
              Run AI Analysis
            </button>
            <Result title="RAG" value={state.rag} />
            <Result title="Decision" value={state.decision} />
          </section>
        ) : null}

        {active === 'Reports' ? (
          <section className="panel form">
            <div className="actions">
              <button
                disabled={!state.decision}
                onClick={() => generateReport('EXECUTIVE')}
                type="button"
              >
                Executive Report
              </button>
              <button
                disabled={!state.decision}
                onClick={() => generateReport('VILLAGE')}
                type="button"
              >
                Village Report
              </button>
              <button
                disabled={!state.decision}
                onClick={() => generateReport('DISTRICT')}
                type="button"
              >
                District Report
              </button>
            </div>
            {state.report ? (
              <article className="report">
                <h3>{state.report.title}</h3>
                <p>{state.report.executiveSummary}</p>
                <a href={`${API_BASE}${state.report.pdfDownloadUrl}`}>Download PDF</a>
                <a href={`${API_BASE}${state.report.csvDownloadUrl}`}>Download CSV</a>
                <pre>{state.report.contentMarkdown}</pre>
              </article>
            ) : (
              <p>Run AI analysis before generating reports.</p>
            )}
          </section>
        ) : null}

        {active === 'User Profile' ? (
          <Result title="Authenticated profile" value={state.profile} />
        ) : null}

        {active === 'Settings' ? (
          <section className="panel">
            <h3>Runtime settings</h3>
            <dl>
              <dt>Backend API</dt>
              <dd>{API_BASE}</dd>
              <dt>Organization</dt>
              <dd>{state.profile?.organizationId ?? PLATFORM_ORG_ID}</dd>
              <dt>Model</dt>
              <dd>qwen2.5-local through Ollama-compatible gateway</dd>
            </dl>
          </section>
        ) : null}
      </section>
    </main>
  );
}

function Metric({ label, value }: Readonly<{ label: string; value: string }>) {
  return (
    <article className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function Result({ title, value }: Readonly<{ title: string; value: unknown }>) {
  return (
    <article className="panel result">
      <h3>{title}</h3>
      <pre>{value ? JSON.stringify(value, null, 2) : 'No data yet.'}</pre>
    </article>
  );
}
