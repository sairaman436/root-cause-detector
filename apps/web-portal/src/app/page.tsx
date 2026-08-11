'use client';

/*
 * Purpose: Provides the Sprint 1 integrated MVP dashboard.
 * Why it exists: Users need one executable workflow for login, survey creation, evidence upload, AI/RAG analysis, recommendations, reports, and profile/settings.
 * Architecture fit: Next.js web adapter that consumes the approved backend API contracts without owning business logic.
 */

import { ChangeEvent, FormEvent, useEffect, useRef, useState } from 'react';
import {
  AiAnalysisView,
  EvidenceRagView,
  EmptyState,
  MetricCard,
  RecommendationWorkspace,
  RootCauseView,
} from './components/decision-support';

const API_BASE = process.env.NEXT_PUBLIC_CORE_BACKEND_URL ?? 'http://localhost:8080';
const LLM_MODEL = process.env.NEXT_PUBLIC_LLM_MODEL ?? 'qwen2.5:0.5b';
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

type SurveySectionResponse = {
  id: string;
  code: string;
  title: string;
};

type SurveyQuestionResponse = {
  id: string;
  code: string;
  questionType: string;
  required: boolean;
};

type SubmissionResponse = {
  id: string;
  surveyId: string;
  status: string;
  submittedAt: string;
  answers: Array<{ questionId: string; questionCode: string; value: string }>;
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

type LlmAnalysisResponse = {
  id: string;
  requestId: string;
  provider: string;
  model: string;
  modelVersion?: string;
  promptId: string;
  promptVersion: string;
  status: string;
  latencyMs: number;
  tokensEstimate: number;
  output: {
    problem: string;
    summary: string;
    contributingFactors: string[];
    rootCauses: string[];
    evidence: string[];
    confidence: number;
    recommendations: string[];
    limitations: string[];
  };
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

type RootCauseAnalysisResponse = {
  analysisId: string;
  problem: {
    village: string;
    domain: string;
    description: string;
    severity: string;
  };
  observedFacts: Array<{
    statement: string;
    sourceType: string;
    category: string;
    confidence: number;
  }>;
  contributingFactors: Array<{
    factor: string;
    supportingEvidence: string[];
    contradictingEvidence: string[];
    confidence: number;
  }>;
  candidateRootCauses: Array<{
    description: string;
    confidence: number;
    uncertainty: string;
    reasoningSummary: string;
  }>;
  validatedRootCauses: Array<{
    description: string;
    confidence: number;
    reasoningSummary: string;
  }>;
  alternativeHypotheses: Array<{
    description: string;
    confidence: number;
    missingEvidence: string[];
  }>;
  uncertainties: Array<{ statement: string; severity: string; followUpQuestions: string[] }>;
  confidence: { overall: number; interpretation: string };
  limitations: string[];
  followUpQuestions: string[];
  causalGraph: Array<{ from: string; to: string; relationshipType: string; confidence: number }>;
};

type RecommendationSetResponse = {
  recommendationSetId: string;
  rootCauseAnalysisId?: string;
  status: string;
  options: Array<{
    recommendationId: string;
    title: string;
    description: string;
    targetRootCause: string;
    targetPopulation?: number;
    domain: string;
    interventionType: string;
    priority: number;
    expectedOutcomes: string[];
    requiredResources: string[];
    estimatedEffort: string;
    estimatedTimeframe: string;
    feasibility: {
      rating: string;
      supportingFactors: string[];
      constraints: string[];
      resourceStatus: string;
    };
    risks: Array<{
      riskType: string;
      description: string;
      severity: string;
      likelihood: string;
      mitigation: string;
      evidenceOrAssumption: string;
    }>;
    dependencies: string[];
    evidence: string[];
    confidence: {
      evidenceStrength: number;
      recommendationConfidence: number;
      implementationFeasibility: number;
      interpretation: string;
    };
    assumptions: string[];
    limitations: string[];
    implementationPlan: Array<{
      phase: string;
      actions: string[];
      responsibleRole: string;
      requiredInputs: string[];
      dependencies: string[];
      successIndicators: string[];
    }>;
    successIndicators: Array<{
      name: string;
      baseline: string;
      target: string;
      measurementMethod: string;
      measurementFrequency: string;
      dataGap: string;
    }>;
    status: string;
  }>;
  comparison: Array<{
    recommendationId: string;
    priorityScore: number;
    advantages: string[];
    disadvantages: string[];
    effortCategory: string;
    complexity: string;
    unintendedConsequences: string[];
  }>;
  schemeMatches: Array<{
    schemeName: string;
    source: string;
    eligibilityEvidence: string[];
    applicablePopulation: string;
    relevantBenefit: string;
    limitations: string[];
    status: string;
  }>;
  model: string;
  promptVersion: string;
  knowledgeSnapshot: string;
  evidenceSnapshot: string;
};

type RecommendationReviewResponse = {
  reviewId: string;
  recommendationSetId: string;
  action: string;
  status: string;
  reviewedAt: string;
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

type TrainingCandidateResponse = {
  id: string;
  learningRecordId: string;
  taskType: string;
  scenarioGroup: string;
  input: string;
  retrievedContext?: string;
  aiOutput: string;
  acceptedOutput?: string;
  evidenceUsedJson: string;
  sourceType: string;
  modelVersion: string;
  promptVersion: string;
  synthetic: boolean;
  approvalStatus: string;
  trainingReadiness: string;
  reviewer?: string;
  reviewerUserId?: string;
  datasetVersion?: string;
  createdAt?: string;
};

type HumanEvaluationExample = {
  evaluationSetVersion: string;
  rubricVersion: string;
  exampleId: string;
  task: string;
  scenarioGroup: string;
  input: string;
  retrievedContext: string;
  output: string;
  citations: Array<{ source_id?: string; sourceId?: string; excerpt?: string }>;
  provenance: Record<string, unknown>;
  modelVersion: string;
  promptVersion: string;
  inferenceConfiguration: Record<string, unknown>;
  outputSha256: string;
  reviewStatus: 'REMAINING' | 'SCORED';
  reviewCount: number;
};

type HumanEvaluationQueue = {
  evaluationSetVersion: string;
  rubricVersion: string;
  total: number;
  scored: number;
  remaining: number;
  examples: HumanEvaluationExample[];
};

type HumanScoreState = {
  rootCauseQuality: number | '';
  recommendationQuality: number | '';
  ragEvidenceQuality: number | '';
  uncertaintyHandling: number | '';
  practicalUsefulness: number | '';
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
  surveySection?: SurveySectionResponse;
  surveyQuestion?: SurveyQuestionResponse;
  submission?: SubmissionResponse;
  evidence?: EvidenceResponse;
  rag?: RagResponse;
  llmAnalysis?: LlmAnalysisResponse;
  rootCauseAnalysis?: RootCauseAnalysisResponse;
  recommendations?: RecommendationSetResponse;
  decision?: DecisionResponse;
  report?: ReportResponse;
  trainingCandidates?: TrainingCandidateResponse[];
  humanEvaluation?: HumanEvaluationQueue;
};

type PageResponse<T> = {
  content: T[];
  totalElements: number;
};

type PlatformSnapshot = {
  aiHealth?: { status: string; activeNodes: number; healthyNodes: number; circuitBreakerStatus: string };
  deployment?: { status: string; environment: string; region: string };
  candidateCount?: number;
  pendingCandidateCount?: number;
  humanEvaluation?: { total: number; scored: number; remaining: number; evaluationSetVersion: string };
  datasets?: Array<{ name: string; type: string; status: string; qualityScore: number | null }>;
  evaluations?: Array<{ id: string; modelName: string; status: string; recommendation: string; overallScore: number | null }>;
};

const navItems = [
  'Dashboard',
  'Login',
  'Survey',
  'Evidence / RAG',
  'AI Analysis',
  'Root Cause Analysis',
  'Recommendations',
  'Training Review',
  'Human Evaluation',
  'Model Evaluation',
  'Governance / Dataset',
  'Reports',
  'User Profile',
  'Settings',
];

export default function WebPortalSprintOnePage() {
  const [active, setActive] = useState('Dashboard');
  const [state, setState] = useState<WorkflowState>({});
  const [platformSnapshot, setPlatformSnapshot] = useState<PlatformSnapshot>({});
  const refreshInFlight = useRef<Promise<TokenResponse | undefined> | null>(null);
  const [message, setMessage] = useState('Ready to run Sprint 1 workflow.');
  const [error, setError] = useState('');
  const [isBusy, setIsBusy] = useState(false);
  const [email, setEmail] = useState('admin@platform.local');
  const [password, setPassword] = useState('');
  const [recommendationReviewNotes, setRecommendationReviewNotes] = useState('');
  const [recommendationEditJson, setRecommendationEditJson] = useState('');
  const [trainingReviewCandidateId, setTrainingReviewCandidateId] = useState('');
  const [trainingReviewOutput, setTrainingReviewOutput] = useState('');
  const [trainingReviewReason, setTrainingReviewReason] = useState('');
  const [humanEvaluationExampleId, setHumanEvaluationExampleId] = useState('');
  const [humanScores, setHumanScores] = useState<HumanScoreState>({
    rootCauseQuality: '',
    recommendationQuality: '',
    ragEvidenceQuality: '',
    uncertaintyHandling: '',
    practicalUsefulness: '',
  });
  const [humanEvidenceReferences, setHumanEvidenceReferences] = useState('');
  const [humanReviewerComments, setHumanReviewerComments] = useState('');
  const [surveyName, setSurveyName] = useState('Village Water Reliability Survey');
  const [surveyDescription, setSurveyDescription] = useState(
    'Field survey for identifying water access root causes.',
  );
  const [surveyAnswer, setSurveyAnswer] = useState('well');
  const [problemStatement, setProblemStatement] = useState(
    'Village households report unreliable water access and delayed repairs.',
  );
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
    const request = (accessToken?: string) => {
      const headers = new Headers(init.headers);
      if (!(init.body instanceof FormData)) {
        headers.set('Content-Type', 'application/json');
      }
      if (accessToken) {
        headers.set('Authorization', `Bearer ${accessToken}`);
      }
      return fetch(`${API_BASE}${path}`, { ...init, headers });
    };

    let response = await request(state.token?.accessToken);
    if (response.status === 401 && state.token?.refreshToken && !path.startsWith('/api/v1/auth/')) {
      const refreshedToken = await refreshAccessToken(state.token.refreshToken);
      if (refreshedToken) {
        response = await request(refreshedToken.accessToken);
      }
    }
    if (!response.ok) {
      const body = await response.text();
      throw new Error(readableApiError(body, response.status));
    }
    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.includes('application/json')) {
      return (await response.blob()) as T;
    }
    const envelope = (await response.json()) as ApiEnvelope<T>;
    return envelope.data;
  }

  async function refreshAccessToken(refreshToken: string): Promise<TokenResponse | undefined> {
    if (refreshInFlight.current) {
      return refreshInFlight.current;
    }
    const refreshPromise = (async () => {
      const response = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });
      if (!response.ok) {
        setState((current) => ({ ...current, token: undefined, profile: undefined }));
        return undefined;
      }
      const envelope = (await response.json()) as ApiEnvelope<TokenResponse>;
      setState((current) => ({ ...current, token: envelope.data }));
      return envelope.data;
    })();
    refreshInFlight.current = refreshPromise;
    try {
      return await refreshPromise;
    } finally {
      if (refreshInFlight.current === refreshPromise) {
        refreshInFlight.current = null;
      }
    }
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

  async function loadPlatformSnapshot() {
    if (!state.token) return;
    const [aiHealth, deployment, candidates, humanEvaluation, datasets, evaluations] = await Promise.all([
      api<{ status: string; activeNodes: number; healthyNodes: number; circuitBreakerStatus: string }>('/api/v1/ai/health').catch(() => undefined),
      api<{ status: string; environment: string; region: string }>('/api/v1/platform/deployment-status').catch(() => undefined),
      api<PageResponse<TrainingCandidateResponse>>('/api/v1/learning/candidates?size=50').catch(() => undefined),
      api<HumanEvaluationQueue>('/api/v1/evaluation/human/examples').catch(() => undefined),
      api<PageResponse<{ name: string; type: string; status: string; qualityScore: number | null }>>('/api/v1/datasets?size=50').catch(() => undefined),
      api<PageResponse<{ id: string; modelName: string; status: string; recommendation: string; overallScore: number | null }>>('/api/v1/evaluation/results?size=20').catch(() => undefined),
    ]);
    setPlatformSnapshot({
      aiHealth,
      deployment,
      candidateCount: candidates?.totalElements,
      pendingCandidateCount: candidates?.content.filter((candidate) => candidate.approvalStatus === 'PENDING_APPROVAL').length,
      humanEvaluation: humanEvaluation
        ? {
            total: humanEvaluation.total,
            scored: humanEvaluation.scored,
            remaining: humanEvaluation.remaining,
            evaluationSetVersion: humanEvaluation.evaluationSetVersion,
          }
        : undefined,
      datasets: datasets?.content,
      evaluations: evaluations?.content,
    });
  }

  useEffect(() => {
    if (state.token?.accessToken) {
      void loadPlatformSnapshot();
    }
  }, [state.token?.accessToken]);

  async function createSurvey() {
    await run(
      'Survey definition created, validated, published, and stored in PostgreSQL.',
      async () => {
        let survey = await api<SurveyResponse>('/api/v1/surveys', {
          method: 'POST',
          body: JSON.stringify({
            organizationId: state.profile?.organizationId ?? PLATFORM_ORG_ID,
            name: surveyName,
            description: surveyDescription,
            tags: ['sprint-1', 'mvp', 'rural-intelligence'],
          }),
        });
        const surveySection = await api<SurveySectionResponse>(
          `/api/v1/surveys/${survey.id}/sections`,
          {
            method: 'POST',
            body: JSON.stringify({
              code: 'water_access',
              title: 'Water Access',
              description: 'Water access conditions',
              orderIndex: 1,
              repeatable: false,
            }),
          },
        );
        const surveyQuestion = await api<SurveyQuestionResponse>(
          `/api/v1/surveys/${survey.id}/questions`,
          {
            method: 'POST',
            body: JSON.stringify({
              sectionId: surveySection.id,
              code: 'water_source',
              prompt: 'Primary water source',
              questionType: 'single_select',
              orderIndex: 1,
              required: true,
              options: [
                { value: 'well', label: 'Well', orderIndex: 1 },
                { value: 'tap', label: 'Tap', orderIndex: 2 },
                { value: 'tanker', label: 'Tanker', orderIndex: 3 },
              ],
              validationRules: [
                { ruleType: 'REQUIRED', message: 'Water source is required', orderIndex: 1 },
              ],
            }),
          },
        );
        for (const status of ['REVIEW', 'APPROVED', 'PUBLISHED']) {
          survey = await api<SurveyResponse>(`/api/v1/surveys/${survey.id}/workflow`, {
            method: 'POST',
            body: JSON.stringify({ status, reason: 'Sprint 1 recovery workflow' }),
          });
        }
        setState((current) => ({ ...current, survey, surveySection, surveyQuestion }));
      },
    );
  }

  async function submitSurvey() {
    await run('Survey response submitted and persisted.', async () => {
      if (!state.survey || !state.surveyQuestion) {
        throw new Error('Create the survey definition before submitting a response.');
      }
      const submission = await api<SubmissionResponse>(
        `/api/v1/surveys/${state.survey.id}/submissions`,
        {
          method: 'POST',
          body: JSON.stringify({
            answers: [{ questionId: state.surveyQuestion.id, value: surveyAnswer }],
          }),
        },
      );
      setState((current) => ({ ...current, submission }));
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
      if (state.surveyQuestion?.id) body.append('questionId', state.surveyQuestion.id);
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
      const llmAnalysis = await api<LlmAnalysisResponse>('/api/v1/ai/analysis/root-cause', {
        method: 'POST',
        body: JSON.stringify({
          surveyId: state.survey?.id,
          submissionId: state.submission?.id,
          problem: problemStatement,
          modelId: LLM_MODEL,
          evidenceIds: state.evidence ? [state.evidence.id] : [],
          citations: rag.citations,
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
            llmSummary: llmAnalysis.output.summary,
            llmRootCauses: llmAnalysis.output.rootCauses,
          },
          mlPredictions: { model: llmAnalysis.model, confidence: llmAnalysis.output.confidence },
          agentOutputs: { knowledgeCitations: rag.citations },
          requireHumanApproval: true,
        }),
      });
      const rootCauseAnalysis = await api<RootCauseAnalysisResponse>(
        '/api/v1/analysis/root-cause',
        {
          method: 'POST',
          body: JSON.stringify({
            problem: {
              problemId: state.submission?.id ?? state.survey?.id ?? 'dashboard-problem',
              village: 'Demo Village',
              domain: 'Water',
              description: problemStatement,
              affectedPopulation: 120,
              severity: 'HIGH',
              evidence: [
                state.evidence?.originalFileName ?? 'No uploaded evidence selected',
                state.submission ? 'Survey submission available' : 'Survey submission pending',
              ],
              source: 'dashboard-workflow',
            },
            surveyResponses: [
              {
                surveyName: state.survey?.name,
                primaryWaterSource: surveyAnswer,
                submittedAt: state.submission?.submittedAt,
              },
            ],
            evidence: state.evidence
              ? [
                  {
                    title: state.evidence.title,
                    fileName: state.evidence.originalFileName,
                    mimeType: state.evidence.mimeType,
                    checksum: state.evidence.sha256Checksum,
                  },
                ]
              : [],
            structuredData: {
              organizationId: state.profile?.organizationId ?? PLATFORM_ORG_ID,
              surveyId: state.survey?.id,
              evidenceId: state.evidence?.id,
            },
            retrievedDocuments: rag.citations.map((citation) => ({
              source: citation.sourceId,
              excerpt: citation.excerpt,
              score: citation.score,
            })),
            surveyId: state.survey?.id,
            organizationId: state.profile?.organizationId ?? PLATFORM_ORG_ID,
            surveyVersion: String(state.survey?.currentVersion ?? 'unversioned'),
            knowledgeSnapshot: 'rag-service:latest',
            requireHumanReview: true,
          }),
        },
      );
      const recommendations = await api<RecommendationSetResponse>(
        '/api/v1/recommendations/generate',
        {
          method: 'POST',
          body: JSON.stringify({
            rootCauseAnalysisId: rootCauseAnalysis.analysisId,
            villageContext: {
              village: rootCauseAnalysis.problem.village,
              domain: rootCauseAnalysis.problem.domain,
              problem: rootCauseAnalysis.problem.description,
              severity: rootCauseAnalysis.problem.severity,
            },
            evidence: [
              ...rootCauseAnalysis.observedFacts.slice(0, 5).map((fact) => ({
                statement: fact.statement,
                sourceType: fact.sourceType,
                category: fact.category,
                confidence: fact.confidence,
              })),
              ...(state.evidence
                ? [
                    {
                      id: state.evidence.id,
                      fileName: state.evidence.originalFileName,
                      checksum: state.evidence.sha256Checksum,
                    },
                  ]
                : []),
            ],
            availableResources: {
              fieldTeam: 'Requires confirmation during human review',
              monitoringCapacity: 'Monthly review capacity requested',
            },
            constraints: {
              humanApprovalRequired: true,
              noAutomaticExecution: true,
              implementationAuthority: 'Human reviewer',
            },
            domain: rootCauseAnalysis.problem.domain,
            targetPopulation: 120,
            knowledgeSnapshot: 'rag-service:latest',
            evidenceSnapshot: state.evidence?.sha256Checksum ?? 'dashboard-current-evidence',
            requireHumanApproval: true,
          }),
        },
      );
      setState((current) => ({
        ...current,
        rag,
        llmAnalysis,
        decision,
        rootCauseAnalysis,
        recommendations,
      }));
      setRecommendationEditJson(JSON.stringify({ options: recommendations.options }, null, 2));
    });
  }

  async function reviewRecommendations(action: 'EDIT' | 'APPROVE' | 'REJECT') {
    await run(`Recommendation ${action.toLowerCase()} recorded.`, async () => {
      const current = state.recommendations;
      if (!current) {
        throw new Error('Run AI analysis before reviewing recommendations.');
      }
      let modifiedRecommendation: Record<string, unknown> | undefined;
      if (action === 'EDIT') {
        try {
          modifiedRecommendation = JSON.parse(recommendationEditJson) as Record<string, unknown>;
        } catch {
          throw new Error('Edited recommendation JSON is invalid.');
        }
      }
      const path =
        action === 'APPROVE'
          ? `/api/v1/recommendations/${current.recommendationSetId}/approve`
          : action === 'REJECT'
            ? `/api/v1/recommendations/${current.recommendationSetId}/reject`
            : `/api/v1/recommendations/${current.recommendationSetId}/review`;
      const review = await api<RecommendationReviewResponse>(path, {
        method: 'POST',
        body: JSON.stringify({
          action,
          reviewerNotes: recommendationReviewNotes || undefined,
          modifiedRecommendation,
        }),
      });
      const updated = await api<RecommendationSetResponse>(
        `/api/v1/recommendations/${current.recommendationSetId}`,
      );
      setState((state) => ({ ...state, recommendations: updated }));
      if (action === 'EDIT') {
        setRecommendationEditJson(JSON.stringify({ options: updated.options }, null, 2));
      }
      setMessage(
        `Recommendation review ${review.status.toLowerCase()} by ${review.action.toLowerCase()}.`,
      );
    });
  }

  async function loadTrainingCandidates() {
    await run('Training review queue loaded.', async () => {
      const page = await api<{ content: TrainingCandidateResponse[] }>('/api/v1/learning/candidates?size=50');
      setState((current) => ({ ...current, trainingCandidates: page.content }));
    });
  }

  async function loadHumanEvaluations() {
    await run('Human evaluation queue loaded.', async () => {
      const queue = await api<HumanEvaluationQueue>('/api/v1/evaluation/human/examples');
      setState((current) => ({ ...current, humanEvaluation: queue }));
    });
  }

  async function submitHumanEvaluation() {
    await run('Human rubric review submitted.', async () => {
      const queue = state.humanEvaluation;
      const example = queue?.examples.find((item) => item.exampleId === humanEvaluationExampleId);
      if (!example) throw new Error('Select a held-out example before submitting a review.');
      const requiredScores: Array<[keyof HumanScoreState, string]> = [
        ['ragEvidenceQuality', 'RAG/evidence quality'],
        ['uncertaintyHandling', 'uncertainty handling'],
        ['practicalUsefulness', 'practical usefulness'],
      ];
      if (example.task === 'root-cause-analysis' || example.task === 'recommendation-generation') {
        requiredScores.push(['rootCauseQuality', 'root-cause quality']);
      }
      if (example.task === 'recommendation-generation') {
        requiredScores.push(['recommendationQuality', 'recommendation quality']);
      }
      const missing = requiredScores.find(([key]) => humanScores[key] === '');
      if (missing) throw new Error(`Select a score for ${missing[1]}.`);
      const evidenceReferencesUsed = humanEvidenceReferences
        .split(',')
        .map((value) => value.trim())
        .filter(Boolean);
      if (evidenceReferencesUsed.length === 0) {
        throw new Error('Enter at least one source ID used for the review.');
      }
      await api('/api/v1/evaluation/human/reviews', {
        method: 'POST',
        body: JSON.stringify({
          exampleId: example.exampleId,
          scores: {
            rootCauseQuality: scoreValue(humanScores.rootCauseQuality),
            recommendationQuality: scoreValue(humanScores.recommendationQuality),
            ragEvidenceQuality: scoreValue(humanScores.ragEvidenceQuality),
            uncertaintyHandling: scoreValue(humanScores.uncertaintyHandling),
            practicalUsefulness: scoreValue(humanScores.practicalUsefulness),
          },
          evidenceReferencesUsed,
          reviewerComments: humanReviewerComments || undefined,
        }),
      });
      setHumanEvaluationExampleId('');
      setHumanScores(emptyHumanScores());
      setHumanEvidenceReferences('');
      setHumanReviewerComments('');
      await loadHumanEvaluations();
    });
  }

  async function reviewTrainingCandidate(candidateId: string, decision: 'APPROVE' | 'CORRECT' | 'REJECT') {
    await run(`Training candidate ${decision.toLowerCase()} recorded.`, async () => {
      if (decision === 'CORRECT' && !trainingReviewOutput.trim()) {
        throw new Error('Enter the corrected output before selecting Correct.');
      }
      if (decision === 'REJECT' && !trainingReviewReason.trim()) {
        throw new Error('Enter a rejection reason before selecting Reject.');
      }
      await api(`/api/v1/learning/candidates/${candidateId}/review`, {
        method: 'POST',
        body: JSON.stringify({
          decision,
          correctedOutput: decision === 'CORRECT' ? trainingReviewOutput : undefined,
          comments: trainingReviewReason || undefined,
        }),
      });
      setTrainingReviewCandidateId('');
      setTrainingReviewOutput('');
      setTrainingReviewReason('');
      await loadTrainingCandidates();
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
    setIsBusy(true);
    try {
      await action();
      setMessage(success);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected workflow failure');
      setMessage('Workflow stopped.');
    } finally {
      setIsBusy(false);
    }
  }

  function fileChanged(event: ChangeEvent<HTMLInputElement>) {
    setSelectedFile(event.target.files?.[0] ?? null);
  }

  const workflowItems = [
    { label: 'Survey', detail: state.survey?.status ?? 'Not started', complete: Boolean(state.survey) },
    { label: 'Evidence', detail: state.evidence ? 'Uploaded' : 'Awaiting file', complete: Boolean(state.evidence) },
    { label: 'RAG', detail: state.rag ? `${state.rag.citations.length} citations` : 'Not run', complete: Boolean(state.rag) },
    { label: 'AI analysis', detail: state.llmAnalysis ? 'Qwen complete' : 'Not run', complete: Boolean(state.llmAnalysis) },
    { label: 'Root cause', detail: state.rootCauseAnalysis ? 'Structured' : 'Not run', complete: Boolean(state.rootCauseAnalysis) },
    { label: 'Recommendations', detail: state.recommendations ? `${state.recommendations.options.length} options` : 'Not run', complete: Boolean(state.recommendations) },
    { label: 'Review', detail: state.trainingCandidates?.length ? `${state.trainingCandidates.length} candidates` : 'Human gate', complete: false },
  ];
  const selectedHumanEvaluationExample = state.humanEvaluation?.examples.find(
    (example) => example.exampleId === humanEvaluationExampleId,
  );
  const sessionActive = Boolean(state.token && new Date(state.token.accessTokenExpiresAt).getTime() > Date.now());

  return (
    <main className="appShell">
      <aside className="sidebar">
        <div className="brandBlock">
          <div className="brandMark" aria-hidden="true">RI</div>
          <p className="eyebrow">Enterprise Rural Intelligence</p>
          <h1>Rural Intelligence</h1>
          <p className="brandSubline">Decision workspace</p>
        </div>
        <div className="sidebarSectionLabel">Workspace</div>
        <nav aria-label="Primary navigation">
          {navItems.map((item) => (
            <button
              aria-current={active === item ? 'page' : undefined}
              className={active === item ? 'active' : ''}
              key={item}
              onClick={() => setActive(item)}
              type="button"
            >
              {item}
            </button>
          ))}
        </nav>
        <div className="sidebarFooter">
          <span className="sidebarFooterLabel">Session</span>
          <strong>{state.profile?.fullName ?? 'Guest operator'}</strong>
          <span>{state.token ? 'Authenticated workspace' : 'Sign in to begin'}</span>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div className="topbarTitle">
            <p className="eyebrow">Operational workflow / {active}</p>
            <h2>{active}</h2>
            <p className="topbarHint">Evidence-led decisions for rural programs and services.</p>
          </div>
          <div className="topbarMeta">
            <span className={sessionActive ? 'status ok' : 'status'}>
              <span className="statusDot" aria-hidden="true" />
              {sessionActive ? 'Authenticated' : state.token ? 'Session expired' : 'Not signed in'}
            </span>
            <span className="modelChip">Qwen / Ollama</span>
          </div>
        </header>

        <section className={error ? 'notice noticeError' : 'notice'} aria-live="polite">
          <span className="noticeLabel">{isBusy ? 'Processing' : error ? 'Action needs attention' : 'System update'}</span>
          <span>{message}</span>
          {error ? <strong>{error}</strong> : null}
        </section>

        {active === 'Dashboard' ? (
          <section className="dashboardStack">
            <div className="pageIntro">
              <div>
                <p className="eyebrow">Village intelligence</p>
                <h3>Move from field evidence to an accountable decision.</h3>
                <p>Complete each stage, inspect the evidence, then send recommendations through human review.</p>
              </div>
                <button type="button" className="secondaryButton" onClick={() => setActive('Survey')}>
                Start a survey
              </button>
            </div>
            <div className="metricGrid">
              <MetricCard label="Survey status" value={state.survey?.status ?? 'Not created'} detail={state.survey?.name ?? 'Current session'} tone={state.survey ? 'good' : 'neutral'} />
              <MetricCard label="Submission" value={state.submission ? 'Submitted' : 'Pending'} detail={state.submission?.submittedAt ?? 'Awaiting response'} tone={state.submission ? 'good' : 'neutral'} />
              <MetricCard label="Evidence" value={state.evidence ? 'Uploaded' : 'Not uploaded'} detail={state.evidence?.originalFileName ?? 'Awaiting asset'} tone={state.evidence ? 'good' : 'neutral'} />
              <MetricCard
                label="AI confidence"
                value={state.llmAnalysis ? `${Math.round(state.llmAnalysis.output.confidence * 100)}%` : 'Pending'}
                detail={platformSnapshot.aiHealth?.status ?? 'Health not loaded'}
                tone={state.llmAnalysis ? 'good' : 'neutral'}
              />
              <MetricCard label="Review queue" value={platformSnapshot.pendingCandidateCount === undefined ? 'Not loaded' : String(platformSnapshot.pendingCandidateCount)} detail="Pending training candidates" tone={platformSnapshot.pendingCandidateCount ? 'warn' : 'neutral'} />
            </div>
            <article className="panel workflowPanel">
              <div className="sectionHeading">
                <div>
                  <p className="eyebrow">Decision pipeline</p>
                  <h3>Survey to recommendation review</h3>
                </div>
                <span className="sectionMeta">{workflowItems.filter((item) => item.complete).length}/7 complete</span>
              </div>
              <div className="workflowRail" aria-label="Decision workflow">
                {workflowItems.map((item, index) => (
                  <div className={`workflowStep ${item.complete ? 'complete' : ''}`} key={item.label}>
                    <span className="workflowIndex">{item.complete ? 'OK' : String(index + 1).padStart(2, '0')}</span>
                    <strong>{item.label}</strong>
                    <span>{item.detail}</span>
                  </div>
                ))}
              </div>
            </article>
            <div className="dashboardColumns">
              <article className="panel activityPanel">
                <div className="sectionHeading"><h3>Recent activity</h3><span className="sectionMeta">Live session</span></div>
                <ul className="activityList">
                  <ActivityItem label="Authentication" value={state.token ? 'Account connected' : 'Awaiting sign in'} complete={Boolean(state.token)} />
                  <ActivityItem label="Survey definition" value={state.survey?.name ?? 'No survey created'} complete={Boolean(state.survey)} />
                  <ActivityItem label="Evidence workspace" value={state.evidence?.originalFileName ?? 'No evidence attached'} complete={Boolean(state.evidence)} />
                  <ActivityItem label="Decision output" value={state.recommendations ? 'Recommendations ready for review' : 'Not generated'} complete={Boolean(state.recommendations)} />
                </ul>
              </article>
              <article className="panel nextActionPanel">
                <p className="eyebrow">Next action</p>
                <h3>{!state.token ? 'Connect an operator account' : !state.survey ? 'Create the first survey' : !state.evidence ? 'Attach supporting evidence' : 'Run the intelligence workflow'}</h3>
                <p>{!state.token ? 'Sign in to unlock survey, evidence, and review actions.' : 'The workspace keeps every decision linked to its source evidence and review state.'}</p>
                <button type="button" onClick={() => setActive(!state.token ? 'Login' : !state.survey ? 'Survey' : !state.evidence ? 'Evidence / RAG' : 'AI Analysis')}>
                  {!state.token ? 'Go to login' : !state.survey ? 'Open survey' : !state.evidence ? 'Upload evidence' : 'Open AI analysis'}
                </button>
              </article>
            </div>
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
            <button disabled={isBusy} type="submit">{isBusy ? 'Signing in...' : 'Login'}</button>
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
            <button disabled={isBusy || !state.token} onClick={createSurvey} type="button">
              {isBusy ? 'Creating...' : 'Create Survey'}
            </button>
            <label>
              Water source answer
              <input
                onChange={(event) => setSurveyAnswer(event.target.value)}
                value={surveyAnswer}
              />
            </label>
            <button
              disabled={isBusy || !state.token || !state.surveyQuestion}
              onClick={submitSurvey}
              type="button"
            >
              Submit Survey Response
            </button>
            <pre>{state.survey ? JSON.stringify(state.survey, null, 2) : 'No survey yet.'}</pre>
            <pre>
              {state.submission
                ? JSON.stringify(state.submission, null, 2)
                : 'Create the survey, then submit a response.'}
            </pre>
          </section>
        ) : null}

        {active === 'Evidence / RAG' ? (
          <section className="panel form wide">
            <div className="sectionHeading">
              <div>
                <p className="eyebrow">Evidence workspace</p>
                <h3>Evidence and retrieval</h3>
              </div>
              <span className="sectionMeta">Source-backed context</span>
            </div>
            <label>
              Evidence file
              <input onChange={fileChanged} type="file" />
            </label>
            <button disabled={isBusy || !state.token || !state.survey} onClick={uploadEvidence} type="button">
              {isBusy ? 'Uploading...' : 'Upload Evidence'}
            </button>
            <EvidenceRagView evidence={state.evidence} rag={state.rag} />
          </section>
        ) : null}

        {active === 'AI Analysis' ? (
          <section className="panel form wide">
            <div className="sectionHeading">
              <div>
                <p className="eyebrow">Decision support</p>
                <h3>AI analysis workspace</h3>
              </div>
              <span className="sectionMeta">Qwen / constrained output</span>
            </div>
            <label>
              Problem statement
              <textarea
                onChange={(event) => setProblemStatement(event.target.value)}
                value={problemStatement}
              />
            </label>
            <button disabled={isBusy || !state.token || !state.survey} onClick={runAiWorkflow} type="button">
              {isBusy ? 'Running analysis...' : 'Run AI Analysis'}
            </button>
            <AiAnalysisView rag={state.rag} llm={state.llmAnalysis} rootCause={state.rootCauseAnalysis} decision={state.decision} />
          </section>
        ) : null}

        {active === 'Root Cause Analysis' ? (
          <section className="panel form wide">
            <div className="sectionHeading"><div><p className="eyebrow">Explainability</p><h3>Root-cause analysis</h3></div><span className="sectionMeta">Facts, causes, uncertainty</span></div>
            {state.rootCauseAnalysis ? <RootCauseView rootCause={state.rootCauseAnalysis} /> : <EmptyState title="Root cause not available" body="Run AI analysis to generate structured root-cause intelligence." />}
          </section>
        ) : null}

        {active === 'Recommendations' ? (
          <section className="panel form wide">
            <RecommendationWorkspace
              recommendation={state.recommendations}
              actions={state.recommendations ? (
                <>
                <label htmlFor="recommendation-review-notes">Reviewer notes</label>
                <textarea
                  id="recommendation-review-notes"
                  value={recommendationReviewNotes}
                  onChange={(event) => setRecommendationReviewNotes(event.target.value)}
                  placeholder="Explain the review decision"
                  rows={3}
                />
                <label htmlFor="recommendation-edit-json">Edit options JSON</label>
                <textarea
                  id="recommendation-edit-json"
                  value={recommendationEditJson}
                  onChange={(event) => setRecommendationEditJson(event.target.value)}
                  placeholder='{"options":[]}'
                  rows={10}
                />
                <div className="actions" aria-label="Recommendation review actions">
                  <button disabled={isBusy} type="button" onClick={() => reviewRecommendations('EDIT')}>
                    Save Edit
                  </button>
                  <button disabled={isBusy} type="button" onClick={() => reviewRecommendations('APPROVE')}>
                    Approve
                  </button>
                  <button disabled={isBusy} type="button" onClick={() => reviewRecommendations('REJECT')}>
                    Reject
                  </button>
                </div>
                </>
              ) : undefined}
            />
          </section>
        ) : null}

        {active === 'Training Review' ? (
          <section className="panel form">
            <div className="actions">
              <button type="button" onClick={loadTrainingCandidates} disabled={isBusy || !state.token}>
                {isBusy ? 'Loading...' : 'Load Candidates'}
              </button>
            </div>
            <p>Reviewer identity is taken from the authenticated account. Pending, rejected, and synthetic candidates cannot enter the production dataset.</p>
            {state.trainingCandidates?.length ? (
              <div className="stack">
                {state.trainingCandidates.map((candidate) => (
                  <article className="analysis" key={candidate.id}>
                    <h3>{candidate.taskType} · {candidate.approvalStatus}</h3>
                    <dl>
                      <dt>Scenario</dt>
                      <dd>{candidate.scenarioGroup}</dd>
                      <dt>Model / prompt</dt>
                      <dd>{candidate.modelVersion} / {candidate.promptVersion}</dd>
                      <dt>Source</dt>
                      <dd>{candidate.sourceType}{candidate.synthetic ? ' (synthetic, not promotable)' : ''}</dd>
                    </dl>
                    <h4>Input</h4>
                    <p>{candidate.input}</p>
                    <h4>Context</h4>
                    <pre>{candidate.retrievedContext || 'No retrieved context recorded.'}</pre>
                    <h4>AI output</h4>
                    <pre>{candidate.aiOutput}</pre>
                    <h4>Evidence and provenance</h4>
                    <pre>{candidate.evidenceUsedJson}</pre>
                    <div className="actions" aria-label={`Review actions for ${candidate.id}`}>
                      <button type="button" disabled={isBusy || candidate.approvalStatus !== 'PENDING_APPROVAL'} onClick={() => reviewTrainingCandidate(candidate.id, 'APPROVE')}>
                        Approve
                      </button>
                      <button type="button" disabled={isBusy || candidate.approvalStatus !== 'PENDING_APPROVAL'} onClick={() => { setTrainingReviewCandidateId(candidate.id); setTrainingReviewOutput(candidate.acceptedOutput || candidate.aiOutput); }}>
                        Select Correction
                      </button>
                      <button type="button" disabled={isBusy || candidate.approvalStatus !== 'PENDING_APPROVAL'} onClick={() => reviewTrainingCandidate(candidate.id, 'REJECT')}>
                        Reject
                      </button>
                    </div>
                    {trainingReviewCandidateId === candidate.id ? (
                      <div className="form">
                        <label htmlFor={`training-correction-${candidate.id}`}>Corrected output</label>
                        <textarea id={`training-correction-${candidate.id}`} value={trainingReviewOutput} onChange={(event) => setTrainingReviewOutput(event.target.value)} rows={7} />
                        <label htmlFor={`training-review-reason-${candidate.id}`}>Review notes</label>
                        <textarea id={`training-review-reason-${candidate.id}`} value={trainingReviewReason} onChange={(event) => setTrainingReviewReason(event.target.value)} rows={3} placeholder="Explain the correction or decision" />
                        <div className="actions">
                          <button disabled={isBusy} type="button" onClick={() => reviewTrainingCandidate(candidate.id, 'CORRECT')}>Save Correction</button>
                          <button type="button" onClick={() => { setTrainingReviewCandidateId(''); setTrainingReviewOutput(''); }}>Cancel</button>
                        </div>
                      </div>
                    ) : null}
                  </article>
                ))}
              </div>
            ) : (
              <p>Load the authenticated training candidate queue to begin review.</p>
            )}
          </section>
        ) : null}

        {active === 'Human Evaluation' ? (
          <section className="panel form humanEvaluationWorkspace">
            <div className="sectionHeading">
              <div>
                <p className="eyebrow">Independent semantic review</p>
                <h3>BASE Qwen held-out evaluation</h3>
              </div>
              <span className="sectionMeta">
                {state.humanEvaluation ? `${state.humanEvaluation.scored}/${state.humanEvaluation.total} scored` : 'Not loaded'}
              </span>
            </div>
            <p>
              Score only against HUMAN-QUALITY-RUBRIC@1.0.0. This workspace is separate from Training Review and never approves training data.
            </p>
            <div className="actions">
              <button type="button" onClick={loadHumanEvaluations} disabled={isBusy || !state.token}>
                {isBusy ? 'Loading...' : 'Load Held-out Examples'}
              </button>
            </div>
            {!state.token ? <p className="emptyState">Sign in with an authorized evaluation reviewer account to load examples.</p> : null}
            {state.humanEvaluation ? (
              <div className="humanEvaluationLayout">
                <div className="humanEvaluationQueue" aria-label="Held-out evaluation examples">
                  {state.humanEvaluation.examples.map((example) => (
                    <button
                      className={`humanEvaluationQueueItem ${humanEvaluationExampleId === example.exampleId ? 'selected' : ''}`}
                      type="button"
                      key={example.exampleId}
                      onClick={() => {
                        setHumanEvaluationExampleId(example.exampleId);
                        setHumanEvidenceReferences('');
                        setHumanReviewerComments('');
                        setHumanScores(emptyHumanScores());
                      }}
                    >
                      <strong>{example.task}</strong>
                      <span>{example.scenarioGroup}</span>
                      <small>{example.reviewStatus === 'SCORED' ? `Scored (${example.reviewCount})` : 'Remaining'}</small>
                    </button>
                  ))}
                </div>
                {selectedHumanEvaluationExample ? (
                  <article className="humanEvaluationDetail">
                    <div className="sectionHeading">
                      <div>
                        <p className="eyebrow">{selectedHumanEvaluationExample.reviewStatus === 'SCORED' ? 'Submitted state' : 'Incomplete state'}</p>
                        <h3>{selectedHumanEvaluationExample.task}</h3>
                      </div>
                      <span className="sectionMeta">{selectedHumanEvaluationExample.reviewStatus}</span>
                    </div>
                    <dl>
                      <dt>Scenario</dt>
                      <dd>{selectedHumanEvaluationExample.scenarioGroup}</dd>
                      <dt>Evaluation set</dt>
                      <dd>{selectedHumanEvaluationExample.evaluationSetVersion}</dd>
                      <dt>Model / prompt</dt>
                      <dd>{selectedHumanEvaluationExample.modelVersion} / {selectedHumanEvaluationExample.promptVersion}</dd>
                      <dt>Output digest</dt>
                      <dd>{selectedHumanEvaluationExample.outputSha256}</dd>
                    </dl>
                    <h4>Problem input</h4>
                    <p>{selectedHumanEvaluationExample.input}</p>
                    <h4>Retrieved evidence and context</h4>
                    <div className="recordPanel"><p>{selectedHumanEvaluationExample.retrievedContext || 'No retrieved context recorded.'}</p></div>
                    <h4>Allowed citations</h4>
                    <div className="citationList">
                      {selectedHumanEvaluationExample.citations.map((citation, index) => (
                        <div className="citationRow" key={`${citation.source_id ?? citation.sourceId ?? 'source'}-${index}`}>
                          <strong className="citationId">{citation.source_id ?? citation.sourceId ?? 'Unidentified source'}</strong>
                          <span>{citation.excerpt ?? 'No excerpt recorded.'}</span>
                        </div>
                      ))}
                    </div>
                    <h4>BASE Qwen output</h4>
                    <div className="recordPanel outputPanel"><p>{selectedHumanEvaluationExample.output}</p></div>
                    <h4>Provenance and inference configuration</h4>
                    <div className="recordGrid">
                      <FieldList title="Provenance" value={selectedHumanEvaluationExample.provenance} />
                      <FieldList title="Inference configuration" value={selectedHumanEvaluationExample.inferenceConfiguration} />
                    </div>
                    <div className="scoreGrid">
                      {(selectedHumanEvaluationExample.task === 'root-cause-analysis' || selectedHumanEvaluationExample.task === 'recommendation-generation') ? (
                        <ScoreSelect id="human-root-cause-quality" label="Root-cause quality" value={humanScores.rootCauseQuality} onChange={(value) => setHumanScores((current) => ({ ...current, rootCauseQuality: value }))} />
                      ) : null}
                      {selectedHumanEvaluationExample.task === 'recommendation-generation' ? (
                        <ScoreSelect id="human-recommendation-quality" label="Recommendation quality" value={humanScores.recommendationQuality} onChange={(value) => setHumanScores((current) => ({ ...current, recommendationQuality: value }))} />
                      ) : null}
                      <ScoreSelect id="human-rag-quality" label="RAG / evidence quality" value={humanScores.ragEvidenceQuality} onChange={(value) => setHumanScores((current) => ({ ...current, ragEvidenceQuality: value }))} />
                      <ScoreSelect id="human-uncertainty" label="Uncertainty handling" value={humanScores.uncertaintyHandling} onChange={(value) => setHumanScores((current) => ({ ...current, uncertaintyHandling: value }))} />
                      <ScoreSelect id="human-usefulness" label="Practical usefulness" value={humanScores.practicalUsefulness} onChange={(value) => setHumanScores((current) => ({ ...current, practicalUsefulness: value }))} />
                    </div>
                    <label htmlFor="human-evidence-references">Evidence source IDs used</label>
                    <input id="human-evidence-references" value={humanEvidenceReferences} onChange={(event) => setHumanEvidenceReferences(event.target.value)} placeholder="CONTROLLED_PROJECT_PILOT" />
                    <small>Allowed source IDs: {selectedHumanEvaluationExample.citations.map((citation) => citation.source_id ?? citation.sourceId ?? 'unknown').join(', ')}</small>
                    <label htmlFor="human-reviewer-comments">Reviewer comments</label>
                    <textarea id="human-reviewer-comments" value={humanReviewerComments} onChange={(event) => setHumanReviewerComments(event.target.value)} rows={4} placeholder="Record evidence-based reasoning for the scores." />
                    <button type="button" disabled={isBusy} onClick={submitHumanEvaluation}>
                      {isBusy ? 'Submitting...' : 'Submit Human Scores'}
                    </button>
                  </article>
                ) : (
                  <p className="emptyState">Select an example to expose its input, evidence, BASE output, and rubric controls.</p>
                )}
              </div>
            ) : null}
          </section>
        ) : null}

        {active === 'Model Evaluation' ? (
          <section className="panel form wide">
            <div className="sectionHeading">
              <div>
                <p className="eyebrow">Independent model comparison</p>
                <h3>Model evaluation</h3>
              </div>
              <span className="sectionMeta">Existing evaluation records</span>
            </div>
            <p className="sectionLead">This view reports only persisted evaluation data. It does not infer a winner or replace the held-out human rubric.</p>
            {platformSnapshot.evaluations?.length ? (
              <div className="evaluationTable" role="table" aria-label="Persisted model evaluations">
                <div className="evaluationTableRow evaluationTableHeader" role="row"><span>Model</span><span>Status</span><span>Recommendation</span><span>Score</span></div>
                {platformSnapshot.evaluations.map((evaluation) => (
                  <div className="evaluationTableRow" role="row" key={evaluation.id}><strong>{evaluation.modelName}</strong><span>{evaluation.status}</span><span>{evaluation.recommendation || 'Not recorded'}</span><span>{evaluation.overallScore === null ? 'Not evaluated' : evaluation.overallScore}</span></div>
                ))}
              </div>
            ) : (
              <EmptyState title="No comparison is loaded" body="Run and persist an evaluation through the governed evaluation API before comparing model results here." />
            )}
            <div className="notAvailablePanel"><strong>Base Qwen versus fine-tuned Qwen</strong><span>Human-quality scores and detailed comparison metrics are not available through the current portal read API.</span></div>
          </section>
        ) : null}

        {active === 'Governance / Dataset' ? (
          <section className="panel form wide">
            <div className="sectionHeading">
              <div>
                <p className="eyebrow">Controlled data and review</p>
                <h3>Governance and dataset status</h3>
              </div>
              <span className="sectionMeta">No automatic promotion</span>
            </div>
            <div className="metricGrid governanceMetrics">
              <MetricCard label="Dataset registry" value={platformSnapshot.datasets ? `${platformSnapshot.datasets.length} records` : 'Not loaded'} detail="Backend registry" tone={platformSnapshot.datasets ? 'good' : 'neutral'} />
              <MetricCard label="Training review" value={platformSnapshot.pendingCandidateCount === undefined ? 'Not loaded' : `${platformSnapshot.pendingCandidateCount} pending`} detail={`${platformSnapshot.candidateCount ?? 0} total candidates`} tone={platformSnapshot.pendingCandidateCount ? 'warn' : 'neutral'} />
              <MetricCard label="Human evaluation" value={platformSnapshot.humanEvaluation ? `${platformSnapshot.humanEvaluation.remaining} remaining` : 'Not loaded'} detail={platformSnapshot.humanEvaluation?.evaluationSetVersion ?? 'Held-out set'} tone={platformSnapshot.humanEvaluation?.remaining ? 'warn' : 'neutral'} />
              <MetricCard label="Runtime status" value={platformSnapshot.deployment?.status ?? 'Not loaded'} detail={platformSnapshot.deployment?.environment ?? 'Environment unavailable'} tone={platformSnapshot.deployment?.status === 'SERVING' ? 'good' : 'neutral'} />
            </div>
            {platformSnapshot.datasets?.length ? (
              <div className="datasetList">
                {platformSnapshot.datasets.map((dataset) => <article className="datasetRow" key={dataset.name}><div><strong>{dataset.name}</strong><span>{dataset.type}</span></div><span className="status">{dataset.status}</span><span>{dataset.qualityScore === null ? 'Quality not recorded' : `Quality ${dataset.qualityScore}`}</span></article>)}
              </div>
            ) : <EmptyState title="Dataset registry unavailable" body="Sign in with a permitted account or start the backend services to load governed dataset records." />}
            <div className="notAvailablePanel"><strong>Dataset v0.3 release status</strong><span>The portal does not invent a static artifact status. Use the dataset and learning APIs for the current persisted state.</span></div>
          </section>
        ) : null}

        {active === 'Reports' ? (
          <section className="panel form">
            <div className="actions">
              <button
                disabled={isBusy || !state.decision}
                onClick={() => generateReport('EXECUTIVE')}
                type="button"
              >
                Executive Report
              </button>
              <button
                disabled={isBusy || !state.decision}
                onClick={() => generateReport('VILLAGE')}
                type="button"
              >
                Village Report
              </button>
              <button
                disabled={isBusy || !state.decision}
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
              <dd>{state.llmAnalysis?.model ?? 'Configured Qwen model through Ollama provider'}</dd>
            </dl>
          </section>
        ) : null}
      </section>
    </main>
  );
}

function readableApiError(body: string, status: number): string {
  try {
    const payload = JSON.parse(body) as { errorCode?: string; message?: string };
    if (payload.errorCode === 'AUTHENTICATION_REQUIRED' || status === 401) {
      return 'Your session expired or the access token was rejected. Sign in again.';
    }
    if (payload.message) {
      return payload.message;
    }
  } catch {
    // Fall through to stable text matching for framework/database errors.
  }
  if (body.includes('uq_surveys_org_name_active') || body.includes('duplicate key value')) {
    return 'A survey with this name already exists in this organization. Change the survey name and try again.';
  }
  return body || `Request failed with ${status}`;
}

function emptyHumanScores(): HumanScoreState {
  return {
    rootCauseQuality: '',
    recommendationQuality: '',
    ragEvidenceQuality: '',
    uncertaintyHandling: '',
    practicalUsefulness: '',
  };
}

function scoreValue(value: number | ''): number | undefined {
  return value === '' ? undefined : value;
}

function ScoreSelect({
  id,
  label,
  value,
  onChange,
}: Readonly<{
  id: string;
  label: string;
  value: number | '';
  onChange: (value: number | '') => void;
}>) {
  return (
    <label htmlFor={id}>
      {label}
      <select id={id} value={value} onChange={(event) => onChange(event.target.value === '' ? '' : Number(event.target.value))}>
        <option value="">Select 0-4</option>
        <option value="0">0 - Absent / unsafe</option>
        <option value="1">1 - Poor</option>
        <option value="2">2 - Mixed</option>
        <option value="3">3 - Acceptable</option>
        <option value="4">4 - Strong</option>
      </select>
    </label>
  );
}

function FieldList({ title, value }: Readonly<{ title: string; value: Record<string, unknown> }>) {
  const entries = Object.entries(value);
  return (
    <section className="fieldList">
      <h5>{title}</h5>
      {entries.length ? entries.map(([key, item]) => <div key={key}><span>{key}</span><strong>{formatFieldValue(item)}</strong></div>) : <p>Not recorded.</p>}
    </section>
  );
}

function formatFieldValue(value: unknown) {
  if (value === null || value === undefined || value === '') return 'Not recorded';
  if (typeof value === 'object') return Object.values(value as Record<string, unknown>).join(', ');
  return String(value);
}

function ActivityItem({ label, value, complete }: Readonly<{ label: string; value: string; complete: boolean }>) {
  return (
    <li className="activityItem">
      <span className={`activityMarker ${complete ? 'complete' : ''}`} aria-hidden="true">{complete ? 'OK' : '-'}</span>
      <span><strong>{label}</strong><small>{value}</small></span>
    </li>
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
