'use client';

/*
 * Purpose: Presents typed AI, evidence, recommendation, and governance data.
 * Why it exists: Decision-makers need scannable, source-aware views instead of raw API payloads.
 * Architecture fit: This is a pure web presentation adapter; it consumes existing API-shaped data and owns no business decisions.
 */

import { useEffect, useState, type ChangeEvent, type DragEvent, type ReactNode } from 'react';

export type EvidenceSnapshot = {
  originalFileName: string;
  mimeType: string;
  sizeBytes: number;
  sha256Checksum: string;
  title: string;
};

export type RagSnapshot = {
  answer: string;
  citations: Array<{
    sourceType: string;
    sourceId: string;
    excerpt: string;
    score: number;
    citationId?: string;
    documentId?: string;
    title?: string;
    publisher?: string;
    page?: number;
    section?: string;
  }>;
  supportStatus?: string;
  citationValidationStatus?: string;
  reasoningSummary?: string;
  promptVersion?: string;
  modelId?: string;
  inferenceLatencyMs?: number;
};

export type LlmSnapshot = {
  model: string;
  promptVersion: string;
  latencyMs: number;
  output: {
    summary: string;
    contributingFactors: string[];
    rootCauses: string[];
    evidence: string[];
    confidence: number;
    recommendations: string[];
    limitations: string[];
  };
};

export type RootCauseSnapshot = {
  problem: { village: string; domain: string; description: string; severity: string };
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
    supportingEvidence?: string[];
  }>;
  validatedRootCauses: Array<{ description: string; confidence: number; reasoningSummary: string }>;
  alternativeHypotheses: Array<{
    description: string;
    confidence: number;
    missingEvidence: string[];
  }>;
  uncertainties: Array<{ statement: string; severity: string; followUpQuestions: string[] }>;
  confidence: { overall: number; interpretation: string };
  limitations: string[];
};

export type RecommendationOption = {
  recommendationId: string;
  title: string;
  description: string;
  targetRootCause: string;
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
};

export type RecommendationSnapshot = {
  recommendationSetId: string;
  status: string;
  model: string;
  promptVersion: string;
  options: RecommendationOption[];
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
    relevantBenefit: string;
    status: string;
  }>;
};

export type DecisionSnapshot = {
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

export type VisionAnalysisSnapshot = {
  model: string;
  provider: string;
  observations: Array<{ description: string; type: string }>;
  question: string;
  uncertainty: string;
  latency_ms: number;
  gpu_memory?: Record<string, unknown> | null;
};

export type VisionInspectionResult = {
  vision: VisionAnalysisSnapshot;
  retrievalQuery: string;
  rag?: RagSnapshot;
  rootCause?: RootCauseSnapshot;
  recommendations?: RecommendationSnapshot;
  totalLatencyMs?: number;
};

type InspectionStage = 'pending' | 'processing' | 'completed' | 'failed';

type InspectionStageDefinition = {
  label: string;
  status: InspectionStage;
  detail: string;
};

/**
 * Provides the image inspection surface while keeping orchestration in the page API adapter.
 * The image stays in browser memory and downstream panels render only validated API responses.
 */
export function MultimodalInspectionLab({
  authenticated,
  busy = false,
  result,
  onAnalyze,
  onFileSelected,
  onQuestionChange,
}: Readonly<{
  authenticated: boolean;
  busy?: boolean;
  result?: VisionInspectionResult;
  onAnalyze: (file: File, question: string) => Promise<void>;
  onFileSelected?: (file: File | null) => void;
  onQuestionChange?: (question: string) => void;
}>) {
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [question, setQuestion] = useState('');
  const [analysisState, setAnalysisState] = useState<'idle' | 'ready' | 'processing' | 'completed' | 'failed'>('idle');
  const [error, setError] = useState('');

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  function selectImage(candidate: File | undefined) {
    if (!candidate) return;
    if (!candidate.type.startsWith('image/')) {
      setFile(null);
      setPreviewUrl('');
      onFileSelected?.(null);
      setAnalysisState('idle');
      setError('Select an image file. No non-image file was sent or stored.');
      return;
    }
    setFile(candidate);
    setPreviewUrl(URL.createObjectURL(candidate));
    onFileSelected?.(candidate);
    setAnalysisState('ready');
    setError('');
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    selectImage(event.target.files?.[0]);
  }

  function handleDrop(event: DragEvent<HTMLLabelElement>) {
    event.preventDefault();
    selectImage(event.dataTransfer.files?.[0]);
  }

  function removeImage() {
    setFile(null);
    setPreviewUrl('');
    onFileSelected?.(null);
    setAnalysisState('idle');
    setError('');
  }

  async function analyzeImage() {
    if (!file || !authenticated) return;
    setAnalysisState('processing');
    setError('');
    try {
      await onAnalyze(file, question);
      setAnalysisState('completed');
    } catch (cause) {
      setAnalysisState('failed');
      setError(cause instanceof Error ? cause.message : 'Vision analysis unavailable.');
    }
  }

  const stages: InspectionStageDefinition[] = [
    { label: 'Upload', status: file ? 'completed' : 'pending', detail: file ? 'Selected locally' : 'Waiting for image' },
    { label: 'Vision', status: analysisState === 'failed' ? 'failed' : analysisState === 'processing' ? 'processing' : result ? 'completed' : 'pending', detail: analysisState === 'failed' ? 'Unavailable or invalid' : result ? `${result.vision.model} returned observations` : analysisState === 'processing' ? 'Analyzing image' : 'Not run' },
    { label: 'Observations', status: result ? 'completed' : analysisState === 'failed' ? 'failed' : 'pending', detail: result ? `${result.vision.observations.length} visible observations` : 'Requires vision output' },
    { label: 'Retrieval', status: result?.rag ? 'completed' : result ? 'failed' : 'pending', detail: result?.rag ? 'Governed evidence retrieved' : result ? 'No governed evidence' : 'Requires observations' },
    { label: 'Evidence validation', status: result?.rag?.citations.length ? 'completed' : result ? 'failed' : 'pending', detail: result?.rag?.citations.length ? 'Citations validated' : result ? 'Insufficient governed evidence' : 'Requires retrieval' },
    { label: 'Root cause', status: result?.rootCause ? 'completed' : result ? 'failed' : 'pending', detail: result?.rootCause ? 'Evidence-supported analysis' : result ? 'Stopped upstream' : 'Blocked upstream' },
    { label: 'Recommendation', status: result?.recommendations ? 'completed' : result?.rootCause ? 'failed' : 'pending', detail: result?.recommendations ? 'Options generated' : result?.rootCause ? 'No validated result' : 'Blocked upstream' },
    { label: 'Source trace', status: result?.recommendations ? 'completed' : 'pending', detail: result?.recommendations ? 'Recommendation to source links available' : 'Blocked upstream' },
  ];

  return (
    <section className="inspectionLab">
      <div className="pageIntro">
        <div>
          <p className="eyebrow">AI inspection lab</p>
          <h3>Inspect what the platform can actually see.</h3>
          <p>
            Select an image to receive model observations, then inspect how those observations are
            converted into a governed retrieval query and evidence-backed decision.
          </p>
        </div>
        <span className={`status ${result ? 'statusGood' : 'statusWarning'}`}>{result ? 'Analysis available' : 'Awaiting image'}</span>
      </div>

      <div className="inspectionGrid">
        <article className="panel inspectionUploadPanel">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Input</p>
              <h3>Image inspection</h3>
            </div>
            <span className="sectionMeta">Image only or image + question</span>
          </div>
          <label
            className={`inspectionDropzone ${file ? 'hasFile' : ''}`}
            onDragOver={(event) => event.preventDefault()}
            onDrop={handleDrop}
          >
            <input type="file" accept="image/*" onChange={handleFileChange} />
            <strong>{file ? 'Replace image' : 'Drop an image here'}</strong>
            <span>{file ? 'Choose another image or drag it over this area.' : 'PNG, JPEG, WEBP, or another browser-recognized image type.'}</span>
          </label>
          {file ? (
            <div className="inspectionFileMeta">
              <span>{file.name}</span>
              <span>{formatBytes(file.size)}</span>
              <button type="button" className="secondaryButton" onClick={removeImage}>
                Remove image
              </button>
            </div>
          ) : null}
          <label htmlFor="inspection-question">
            Question <span className="fieldHint">Optional</span>
            <textarea
              id="inspection-question"
              value={question}
              onChange={(event) => {
                setQuestion(event.target.value);
                onQuestionChange?.(event.target.value);
              }}
              placeholder="What visible crop condition should the model inspect?"
              rows={4}
            />
          </label>
          <button type="button" disabled={!file || !authenticated || busy || analysisState === 'processing'} onClick={() => void analyzeImage()}>
            {analysisState === 'processing' || busy ? 'Analyzing...' : 'Analyze Image'}
          </button>
          {!authenticated ? <p className="mutedCopy">Sign in with an AI operator account before analyzing an image.</p> : null}
          {error ? <p className="inspectionError" role="alert">{error}</p> : null}
        </article>

        <article className="panel inspectionPreviewPanel">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Image</p>
              <h3>Local preview</h3>
            </div>
            <span className="sectionMeta">No permanent upload</span>
          </div>
          {previewUrl ? (
            <div className="inspectionPreviewFrame">
              <img src={previewUrl} alt={file ? `Selected inspection image: ${file.name}` : 'Selected inspection image'} />
            </div>
          ) : (
            <div className="inspectionEmptyPreview">
              <strong>No image selected</strong>
              <span>The preview appears here before any analysis attempt.</span>
            </div>
          )}
        </article>
      </div>

      <article className="panel inspectionPipelinePanel">
        <div className="sectionHeading">
          <div>
            <p className="eyebrow">Live pipeline</p>
            <h3>Image to decision trace</h3>
          </div>
          <span className="sectionMeta">Observations are not evidence</span>
        </div>
        <div className="inspectionPipeline" aria-label="Multimodal inspection pipeline">
          {stages.map((stage) => (
            <div className={`inspectionStage inspectionStage-${stage.status}`} key={stage.label}>
              <span className="inspectionStageMarker" aria-hidden="true">
                {stage.status === 'completed' ? 'OK' : stage.status === 'failed' ? '!' : '.'}
              </span>
              <strong>{stage.label}</strong>
              <span>{stage.detail}</span>
            </div>
          ))}
        </div>
      </article>

      <div className="inspectionOutputGrid">
        <article className="panel inspectionOutputPanel">
          <p className="eyebrow">MODEL OBSERVATIONS</p>
          <h3>{result ? `${result.vision.observations.length} observations` : analysisState === 'failed' ? 'Unavailable' : 'Not produced'}</h3>
          {result ? <ul>{result.vision.observations.map((item, index) => <li key={`${item.type}-${index}`}><strong>{item.type}</strong>: {item.description}</li>)}</ul> : <p>{analysisState === 'failed' ? 'No validated observations were returned.' : 'No observation is shown until the real vision model returns one.'}</p>}
          {result ? <p className="mutedCopy">Uncertainty: {result.vision.uncertainty}</p> : null}
        </article>
        <article className="panel inspectionOutputPanel">
          <p className="eyebrow">RETRIEVED EVIDENCE</p>
          <h3>{result?.rag ? `${result.rag.citations.length} sources` : 'Not retrieved'}</h3>
          {result?.rag ? <div className="detailStack">{result.rag.citations.map((citation) => <details key={citation.citationId ?? citation.sourceId}><summary>{citation.sourceId} {citation.title ? `· ${citation.title}` : ''}</summary><p>{citation.excerpt}</p><small>Score: {citation.score} · {citation.citationId ?? 'No citation ID'}</small></details>)}</div> : <p>Governed retrieval starts only after validated observations.</p>}
        </article>
        <article className="panel inspectionOutputPanel">
          <p className="eyebrow">ROOT CAUSE</p>
          <h3>{result?.rootCause ? 'Evidence-supported finding' : 'Blocked upstream'}</h3>
          {result?.rootCause ? <RootCauseView rootCause={result.rootCause} compact /> : <p>Root cause generation stops when governed evidence is insufficient.</p>}
        </article>
        <article className="panel inspectionOutputPanel">
          <p className="eyebrow">RECOMMENDATION</p>
          <h3>{result?.recommendations ? 'Grounded options' : 'Blocked'}</h3>
          {result?.recommendations ? <RecommendationWorkspace recommendation={result.recommendations} /> : (
            <>
              <p>Recommendations require an evidence-supported root cause and scenario-specific supporting evidence. No fallback output is used.</p>
              {result?.rootCause ? <div className="traceFailure" role="status"><strong>Grounding link blocked</strong><span>The root cause or intervention options did not pass semantic evidence grounding.</span></div> : null}
            </>
          )}
        </article>
      </div>

      {result ? <div className="inspectionOutputGrid"><article className="panel inspectionOutputPanel"><p className="eyebrow">RETRIEVAL QUERY</p><pre>{result.retrievalQuery}</pre></article><article className="panel inspectionOutputPanel"><p className="eyebrow">SOURCE TRACE</p><p>Recommendation → Root Cause → Evidence → Source IDs</p><p className="mutedCopy">{result.recommendations ? result.recommendations.options.flatMap((option) => option.evidence).join(', ') || 'No option evidence recorded.' : 'Not available until a recommendation is generated.'}</p></article></div> : null}

      <details className="panel inspectionTracePanel">
        <summary>Technical Trace</summary>
        <dl>
          <dt>Model</dt><dd>{result?.vision.model ?? 'moondream:1.8b'}</dd>
          <dt>Model capability</dt><dd>Local image observation model</dd>
          <dt>Endpoint</dt><dd>Backend /api/v1/ai/vision/analyze</dd>
          <dt>Image input</dt><dd>{file ? `${file.type}, ${formatBytes(file.size)}` : 'Not selected'}</dd>
          <dt>Question</dt><dd>{question || 'None provided'}</dd>
          <dt>Retrieval query</dt><dd>{result?.retrievalQuery ?? 'Not produced'}</dd>
          <dt>Source IDs</dt><dd>{result?.rag?.citations.map((citation) => citation.sourceId).join(', ') || 'None'}</dd>
          <dt>Validation</dt><dd>{result ? 'Vision observations and governed evidence validated' : analysisState === 'failed' ? 'Vision request failed before downstream inference' : 'Not run'}</dd>
          <dt>Latency</dt><dd>{result?.totalLatencyMs ? `${result.totalLatencyMs} ms total; ${result.vision.latency_ms} ms vision` : 'Not measured'}</dd>
        </dl>
      </details>
    </section>
  );
}

export type MultimodalEvaluationScoreKey =
  | 'observationQuality'
  | 'evidenceRelevance'
  | 'rootCauseQuality'
  | 'recommendationQuality'
  | 'grounding'
  | 'overallUsefulness';

export type MultimodalEvaluationArtifact = {
  id: string;
  createdAt: string;
  domain: string;
  question: string;
  image: { name: string; type: string; size: number };
  result: VisionInspectionResult;
  reviewStatus: 'UNSCORED' | 'REVIEWED';
  scores: Record<MultimodalEvaluationScoreKey, number | null>;
  failureClass: string;
  flags: Record<'observation' | 'evidence' | 'rootCause' | 'recommendation', boolean>;
  comments: string;
};

/** Server-owned immutable multimodal trace exposed to an authenticated reviewer. */
export type MultimodalTrace = {
  traceId: string;
  artifactVersion: string;
  evaluationRound: string;
  domain: string;
  question: string;
  imageName: string;
  imageType: string;
  imageSize: number;
  artifact: Record<string, unknown>;
  reviewStatus: 'REMAINING' | 'SCORED';
  reviewCount: number;
};

/** Server-backed multimodal queue and dashboard summary. */
export type MultimodalTraceQueue = {
  artifactVersion: string;
  evaluationRound: string;
  rubricVersion: string;
  total: number;
  scored: number;
  remaining: number;
  reviewerCount: number;
  traces: MultimodalTrace[];
  domainSummaries: Array<{
    domain: string;
    scored: number;
    observationAverage: number | null;
    evidenceAverage: number | null;
    rootCauseAverage: number | null;
    recommendationAverage: number | null;
    groundingAverage: number | null;
    usefulnessAverage: number | null;
    recommendationSampleSize: number;
    failureClassifications: Record<string, number>;
  }>;
};

/** Payload accepted by the authenticated multimodal review endpoint. */
export type MultimodalReviewPayload = {
  traceId: string;
  artifactVersion: string;
  evaluationRound: string;
  rubricVersion: string;
  scores: Record<MultimodalEvaluationScoreKey, number | null>;
  failureClassification: string;
  unsupportedClaimFlags: Record<'observation' | 'evidence' | 'rootCause' | 'recommendation', boolean>;
  reviewerComments?: string;
};

export const MULTIMODAL_TRACE_API_PATH = '/api/v1/evaluation/multimodal/traces';
export const MULTIMODAL_REVIEW_API_PATH = '/api/v1/evaluation/multimodal/reviews';

const MULTIMODAL_EVALUATION_STORAGE_KEY = 'rural-intelligence.multimodal-evaluation-artifacts';
const multimodalEvaluationDomains = [
  'Agriculture',
  'Healthcare',
  'Energy',
  'Education',
  'Livelihoods',
  'Water & sanitation',
];
const multimodalEvaluationScoreFields: Array<{ key: MultimodalEvaluationScoreKey; label: string }> = [
  { key: 'observationQuality', label: 'Observation quality' },
  { key: 'evidenceRelevance', label: 'Evidence relevance' },
  { key: 'rootCauseQuality', label: 'Root-cause quality' },
  { key: 'recommendationQuality', label: 'Recommendation quality' },
  { key: 'grounding', label: 'Grounding' },
  { key: 'overallUsefulness', label: 'Overall usefulness' },
];
const multimodalFailureClasses = [
  ['NONE', 'No failure classified'],
  ['VISION_OBSERVATION_FAILURE', 'Vision observation failure'],
  ['RETRIEVAL_FAILURE', 'Retrieval failure'],
  ['EVIDENCE_RELEVANCE_FAILURE', 'Evidence relevance failure'],
  ['ROOT_CAUSE_REASONING_FAILURE', 'Root-cause reasoning failure'],
  ['RECOMMENDATION_GROUNDING_FAILURE', 'Recommendation grounding failure'],
  ['INSUFFICIENT_EVIDENCE', 'Insufficient evidence'],
  ['OTHER', 'Other'],
] as const;
const multimodalUnsupportedFlags = [
  ['observation', 'Unsupported observation'],
  ['evidence', 'Unsupported evidence claim'],
  ['rootCause', 'Unsupported root-cause claim'],
  ['recommendation', 'Unsupported recommendation'],
] as const;

const emptyMultimodalScores = (): Record<MultimodalEvaluationScoreKey, number | null> => ({
  observationQuality: null,
  evidenceRelevance: null,
  rootCauseQuality: null,
  recommendationQuality: null,
  grounding: null,
  overallUsefulness: null,
});

const emptyMultimodalFlags = (): Record<'observation' | 'evidence' | 'rootCause' | 'recommendation', boolean> => ({
  observation: false,
  evidence: false,
  rootCause: false,
  recommendation: false,
});

function formatEvaluationScore(score: number | null) {
  return score === null ? 'Not scored' : `${score}/5`;
}

function createEvaluationArtifactId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID();
  return `multimodal-${Date.now()}`;
}

/**
 * Provides an evaluation workspace around the existing live inspection pipeline.
 * Trace drafts may remain browser-local, while human review is persisted through the authenticated governance API below.
 */
export function MultimodalEvaluationLab({
  authenticated,
  busy = false,
  result,
  domain,
  onDomainChange,
  onAnalyze,
  image,
  onFileSelected,
  question,
  onQuestionChange,
  serverQueue,
  reviewerIdentity,
  onLoadServerTraces,
  onSubmitServerReview,
}: Readonly<{
  authenticated: boolean;
  busy?: boolean;
  result?: VisionInspectionResult;
  domain: string;
  onDomainChange: (value: string) => void;
  onAnalyze: (file: File, question: string) => Promise<void>;
  image?: { name: string; type: string; size: number };
  onFileSelected: (file: File | null) => void;
  question: string;
  onQuestionChange: (question: string) => void;
  serverQueue?: MultimodalTraceQueue;
  reviewerIdentity?: string;
  onLoadServerTraces: () => Promise<void>;
  onSubmitServerReview: (payload: MultimodalReviewPayload) => Promise<void>;
}>) {
  const [artifacts, setArtifacts] = useState<MultimodalEvaluationArtifact[]>([]);
  const [scores, setScores] = useState(emptyMultimodalScores);
  const [failureClass, setFailureClass] = useState('NONE');
  const [flags, setFlags] = useState(emptyMultimodalFlags);
  const [comments, setComments] = useState('');
  const [saveMessage, setSaveMessage] = useState('');
  const [selectedTraceId, setSelectedTraceId] = useState('');
  const [serverScores, setServerScores] = useState(emptyMultimodalScores);
  const [serverFailureClass, setServerFailureClass] = useState('NONE');
  const [serverFlags, setServerFlags] = useState(emptyMultimodalFlags);
  const [serverComments, setServerComments] = useState('');
  const [serverMessage, setServerMessage] = useState('');

  useEffect(() => {
    try {
      const stored = window.localStorage.getItem(MULTIMODAL_EVALUATION_STORAGE_KEY);
      if (!stored) return;
      const parsed = JSON.parse(stored) as unknown;
      if (Array.isArray(parsed)) setArtifacts(parsed as MultimodalEvaluationArtifact[]);
    } catch {
      setSaveMessage('Saved evaluation artifacts could not be loaded from this browser.');
    }
  }, []);

  function persist(next: MultimodalEvaluationArtifact[]) {
    setArtifacts(next);
    try {
      window.localStorage.setItem(MULTIMODAL_EVALUATION_STORAGE_KEY, JSON.stringify(next));
    } catch {
      setSaveMessage('Artifact created in memory, but this browser could not persist it.');
    }
  }

  function saveArtifact() {
    if (!result || !image) {
      setSaveMessage('Run a real image analysis before saving an evaluation artifact.');
      return;
    }
    const reviewed = Object.values(scores).some((score) => score !== null)
      || failureClass !== 'NONE'
      || Object.values(flags).some(Boolean)
      || comments.trim().length > 0;
    const artifact: MultimodalEvaluationArtifact = {
      id: createEvaluationArtifactId(),
      createdAt: new Date().toISOString(),
      domain,
      question,
      image,
      result,
      reviewStatus: reviewed ? 'REVIEWED' : 'UNSCORED',
      scores,
      failureClass,
      flags,
      comments: comments.trim(),
    };
    persist([artifact, ...artifacts]);
    setSaveMessage(`Saved ${artifact.id} locally. No training or governance record was created.`);
  }

  function exportArtifact(artifact: MultimodalEvaluationArtifact) {
    const blob = new Blob([JSON.stringify(artifact, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `multimodal-evaluation-${artifact.id}.json`;
    link.click();
    URL.revokeObjectURL(url);
  }

  const selectedServerTrace = serverQueue?.traces.find((trace) => trace.traceId === selectedTraceId);

  async function submitServerReview() {
    if (!selectedServerTrace || !serverQueue) {
      setServerMessage('Load the server traces and select one before submitting a review.');
      return;
    }
    const required = multimodalEvaluationScoreFields
      .filter(({ key }) => key !== 'recommendationQuality')
      .find(({ key }) => serverScores[key] === null);
    if (required) {
      setServerMessage(`Select a score for ${required.label}.`);
      return;
    }
    await onSubmitServerReview({
      traceId: selectedServerTrace.traceId,
      artifactVersion: selectedServerTrace.artifactVersion,
      evaluationRound: selectedServerTrace.evaluationRound,
      rubricVersion: serverQueue.rubricVersion,
      scores: serverScores,
      failureClassification: serverFailureClass,
      unsupportedClaimFlags: serverFlags,
      reviewerComments: serverComments.trim() || undefined,
    });
    setServerMessage('Review saved to the authenticated evaluation workflow.');
  }

  return (
    <section className="inspectionLab evaluationLab">
      <div className="pageIntro">
        <div>
          <p className="eyebrow">Evaluation only</p>
          <h3>Multimodal Evaluation Lab</h3>
          <p>
            Run real images through the existing vision, retrieval, root-cause, and recommendation
            path. Trace drafts remain local to this browser; human scores are saved only through the authenticated server workflow.
          </p>
        </div>
        <span className="status statusGood">No automatic scoring</span>
      </div>

      <article className="panel evaluationControlPanel">
        <div className="sectionHeading">
          <div>
            <p className="eyebrow">Evaluation context</p>
            <h3>Record the scenario without assuming the image domain</h3>
          </div>
          <span className="sectionMeta">Human-entered metadata</span>
        </div>
        <div className="evaluationControlGrid">
          <label>
            Evaluation domain
            <select value={domain} onChange={(event) => onDomainChange(event.target.value)}>
              {multimodalEvaluationDomains.map((option) => <option key={option}>{option}</option>)}
            </select>
          </label>
          <label>
            Evaluation question <span className="fieldHint">Optional</span>
            <input
              value={question}
              onChange={(event) => onQuestionChange(event.target.value)}
              placeholder="What should the model inspect?"
            />
          </label>
        </div>
        <p className="mutedCopy">The selected domain is an evaluation label only. The model must derive observations from the uploaded image.</p>
      </article>

      <MultimodalInspectionLab
        authenticated={authenticated}
        busy={busy}
        result={result}
        onAnalyze={onAnalyze}
        onFileSelected={onFileSelected}
        onQuestionChange={onQuestionChange}
      />

      <article className="panel evaluationReviewPanel">
        <div className="sectionHeading">
          <div>
            <p className="eyebrow">Local trace draft</p>
            <h3>Prepare a local draft</h3>
          </div>
          <span className="sectionMeta">Not a governance record</span>
        </div>
        <div className="evaluationScoreGrid">
          {multimodalEvaluationScoreFields.map(({ key, label }) => (
            <label key={key}>
              {label}
              <select
                value={scores[key] ?? ''}
                onChange={(event) => setScores((current) => ({
                  ...current,
                  [key]: event.target.value ? Number(event.target.value) : null,
                }))}
              >
                <option value="">Not scored</option>
                {[1, 2, 3, 4, 5].map((score) => <option key={score} value={score}>{score}/5</option>)}
              </select>
            </label>
          ))}
        </div>
        <div className="evaluationReviewGrid">
          <label>
            Failure classification
            <select value={failureClass} onChange={(event) => setFailureClass(event.target.value)}>
              {multimodalFailureClasses.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>
          <fieldset className="evaluationFlags">
            <legend>Unsupported claim flags</legend>
            {multimodalUnsupportedFlags.map(([key, label]) => (
              <label key={key}>
                <input
                  type="checkbox"
                  checked={flags[key]}
                  onChange={(event) => setFlags((current) => ({ ...current, [key]: event.target.checked }))}
                />
                {label}
              </label>
            ))}
          </fieldset>
        </div>
        <label>
          Reviewer comments
          <textarea value={comments} onChange={(event) => setComments(event.target.value)} rows={4} placeholder="Record what was useful, unsupported, missing, or unsafe." />
        </label>
        <div className="evaluationActions">
          <button type="button" onClick={saveArtifact} disabled={!result || !image}>Save Local Trace Draft</button>
          <span className="mutedCopy">{image ? `${image.name} · ${formatBytes(image.size)}` : 'Select an image to create an artifact.'}</span>
        </div>
        {saveMessage ? <p className="formMessage" role="status">{saveMessage}</p> : null}
      </article>

      <article className="panel evaluationReviewPanel serverEvaluationPanel">
        <div className="sectionHeading">
          <div>
            <p className="eyebrow">Server-backed human evaluation</p>
            <h3>Review immutable multimodal traces</h3>
          </div>
          <span className="sectionMeta">Reviewer: {reviewerIdentity ?? 'Not signed in'}</span>
        </div>
        <p className="mutedCopy">
          Reviews are stored by the authenticated API. The underlying trace is immutable and reviewer identity is derived from the JWT.
        </p>
        <div className="evaluationActions">
          <button type="button" onClick={onLoadServerTraces} disabled={!authenticated || busy}>
            {busy ? 'Loading...' : 'Load Server Traces'}
          </button>
          {serverQueue ? <span className="mutedCopy">{serverQueue.scored}/{serverQueue.total} scored · {serverQueue.remaining} remaining · {serverQueue.reviewerCount} reviewer(s)</span> : null}
        </div>
        {!authenticated ? <p className="emptyState">Sign in with an authorized evaluation reviewer account to load server traces.</p> : null}
        {serverQueue ? (
          <>
            <div className="humanEvaluationLayout">
              <div className="humanEvaluationQueue" aria-label="Server-backed multimodal traces">
                {serverQueue.traces.map((trace) => (
                  <button
                    className={`humanEvaluationQueueItem ${selectedTraceId === trace.traceId ? 'selected' : ''}`}
                    type="button"
                    key={trace.traceId}
                    onClick={() => {
                      setSelectedTraceId(trace.traceId);
                      setServerScores(emptyMultimodalScores());
                      setServerFailureClass('NONE');
                      setServerFlags(emptyMultimodalFlags());
                      setServerComments('');
                      setServerMessage('');
                    }}
                  >
                    <strong>{trace.domain}</strong>
                    <span>{trace.traceId}</span>
                    <small>{trace.reviewStatus === 'SCORED' ? `Scored (${trace.reviewCount})` : 'Remaining'}</small>
                  </button>
                ))}
              </div>
              {selectedServerTrace ? (
                <article className="humanEvaluationDetail">
                  <div className="sectionHeading">
                    <div><p className="eyebrow">Immutable trace</p><h3>{selectedServerTrace.domain}</h3></div>
                    <span className="sectionMeta">{selectedServerTrace.reviewStatus}</span>
                  </div>
                  <dl>
                    <dt>Trace</dt><dd>{selectedServerTrace.traceId}</dd>
                    <dt>Artifact version</dt><dd>{selectedServerTrace.artifactVersion}</dd>
                    <dt>Evaluation round</dt><dd>{selectedServerTrace.evaluationRound}</dd>
                    <dt>Question</dt><dd>{selectedServerTrace.question || 'None provided'}</dd>
                    <dt>Image</dt><dd>{selectedServerTrace.imageName} · {selectedServerTrace.imageType} · {formatBytes(selectedServerTrace.imageSize)}</dd>
                  </dl>
                  <h4>Trace artifact</h4>
                  <pre className="recordPanel outputPanel">{JSON.stringify(selectedServerTrace.artifact, null, 2)}</pre>
                  <h4>HUMAN-QUALITY-RUBRIC@1.0.0</h4>
                  <div className="evaluationScoreGrid">
                    {multimodalEvaluationScoreFields.map(({ key, label }) => (
                      <label key={key}>
                        {label}
                        <select value={serverScores[key] ?? ''} onChange={(event) => setServerScores((current) => ({ ...current, [key]: event.target.value ? Number(event.target.value) : null }))}>
                          <option value="">{key === 'recommendationQuality' ? 'Not scored' : 'Select score'}</option>
                          {[1, 2, 3, 4, 5].map((score) => <option key={score} value={score}>{score}/5</option>)}
                        </select>
                      </label>
                    ))}
                  </div>
                  <div className="evaluationReviewGrid">
                    <label>
                      Failure classification
                      <select value={serverFailureClass} onChange={(event) => setServerFailureClass(event.target.value)}>
                        {multimodalFailureClasses.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                      </select>
                    </label>
                    <fieldset className="evaluationFlags"><legend>Unsupported claim flags</legend>
                      {multimodalUnsupportedFlags.map(([key, label]) => <label key={key}><input type="checkbox" checked={serverFlags[key]} onChange={(event) => setServerFlags((current) => ({ ...current, [key]: event.target.checked }))} />{label}</label>)}
                    </fieldset>
                  </div>
                  <label>Reviewer comments<textarea value={serverComments} onChange={(event) => setServerComments(event.target.value)} rows={4} placeholder="Record evidence-based review comments." /></label>
                  <button type="button" disabled={busy || selectedServerTrace.reviewStatus === 'SCORED'} onClick={submitServerReview}>{busy ? 'Saving...' : 'Save Human Review'}</button>
                </article>
              ) : <p className="emptyState">Select a trace to inspect the image observations, retrieval, evidence, root cause, and recommendation outcome.</p>}
            </div>
            {serverQueue.domainSummaries.length ? <div className="recordGrid">{serverQueue.domainSummaries.map((summary) => <div className="recordPanel" key={summary.domain}><strong>{summary.domain}</strong><span>{summary.scored} scored · Observation {formatEvaluationScore(summary.observationAverage === null ? null : Math.round(summary.observationAverage))} · Evidence {formatEvaluationScore(summary.evidenceAverage === null ? null : Math.round(summary.evidenceAverage))} · Usefulness {formatEvaluationScore(summary.usefulnessAverage === null ? null : Math.round(summary.usefulnessAverage))}</span></div>)}</div> : <p className="mutedCopy">Per-domain averages appear only after actual human scores exist.</p>}
          </>
        ) : null}
        {serverMessage ? <p className="formMessage" role="status">{serverMessage}</p> : null}
      </article>

      <article className="panel evaluationArtifactsPanel">
        <div className="sectionHeading">
          <div>
            <p className="eyebrow">Local artifacts</p>
            <h3>Saved runs and comparison</h3>
          </div>
          <span className="sectionMeta">{artifacts.length} saved run{artifacts.length === 1 ? '' : 's'}</span>
        </div>
        {artifacts.length ? (
          <div className="evaluationArtifactTable" role="table" aria-label="Saved multimodal evaluation runs">
            <div className="evaluationArtifactRow evaluationArtifactHeader" role="row">
              <strong>Run</strong><strong>Domain / image</strong><strong>Status</strong><strong>Scores</strong><strong>Latency</strong><span />
            </div>
            {artifacts.map((artifact) => (
              <div className="evaluationArtifactRow" role="row" key={artifact.id}>
                <div><strong>{artifact.id.slice(0, 12)}</strong><small>{new Date(artifact.createdAt).toLocaleString()}</small></div>
                <div><strong>{artifact.domain}</strong><small>{artifact.image.name}</small></div>
                <span className={`status ${artifact.reviewStatus === 'REVIEWED' ? 'statusGood' : 'statusWarning'}`}>{artifact.reviewStatus}</span>
                <div><small>Obs {formatEvaluationScore(artifact.scores.observationQuality)} · Grounding {formatEvaluationScore(artifact.scores.grounding)}</small><small>Usefulness {formatEvaluationScore(artifact.scores.overallUsefulness)}</small></div>
                <span>{artifact.result.totalLatencyMs ? `${artifact.result.totalLatencyMs} ms` : 'Not measured'}</span>
                <button type="button" className="secondaryButton" onClick={() => exportArtifact(artifact)}>Export JSON</button>
              </div>
            ))}
          </div>
        ) : (
          <div className="evaluationEmptyState"><strong>No evaluation artifacts saved</strong><span>Run a real image, inspect the trace, then save it for comparison.</span></div>
        )}
      </article>
    </section>
  );
}

type TraceStage = 'complete' | 'processing' | 'blocked' | 'pending';

/**
 * Presents one auditable view of the existing RAG-to-decision workflow.
 * The component only renders API responses; it does not retrieve evidence or infer business facts.
 */
export function LiveDecisionTrace({
  problem,
  domain,
  onDomainChange,
  onProblemChange,
  onRun,
  busy,
  rag,
  llm,
  rootCause,
  recommendations,
  error,
  authenticated,
  canRun,
  processingStage = 'idle',
}: Readonly<{
  problem: string;
  domain: string;
  onDomainChange: (value: string) => void;
  onProblemChange: (value: string) => void;
  onRun: () => void;
  busy: boolean;
  rag?: RagSnapshot;
  llm?: LlmSnapshot;
  rootCause?: RootCauseSnapshot;
  recommendations?: RecommendationSnapshot;
  error?: string;
  authenticated: boolean;
  canRun: boolean;
  processingStage?: string;
}>) {
  const hasCitations = Boolean(rag?.citations.length);
  const citationFieldsValid = Boolean(
    rag?.citations.length && rag.citations.every((citation) => citation.sourceId && citation.excerpt),
  );
  const rootCauseBlocked = Boolean(error && !rootCause && hasCitations);
  const recommendationBlocked = Boolean(error && rootCause && !recommendations);
  const ragBlocked = Boolean(error && !rag);
  const stageStatus = (stage: string, complete: boolean, blocked = false): TraceStage => {
    if (blocked) return 'blocked';
    if (processingStage === stage) return 'processing';
    if (complete) return 'complete';
    return 'pending';
  };
  const stages: Array<{ label: string; detail: string; status: TraceStage }> = [
    {
      label: 'Problem submitted',
      detail: problem.trim() ? 'Current problem statement' : 'Awaiting a problem statement',
      status: problem.trim() ? 'complete' : 'pending',
    },
    {
      label: 'Evidence retrieval',
      detail: rag ? `${rag.citations.length} citation(s) returned` : ragBlocked ? 'Unavailable' : 'Not run',
      status: stageStatus('retrieving', Boolean(rag && hasCitations), Boolean(rag && !hasCitations) || ragBlocked),
    },
    {
      label: 'Evidence validation',
      detail: rag
        ? rag.citationValidationStatus ?? (citationFieldsValid ? 'Citation fields present' : 'No valid citation fields')
        : ragBlocked ? 'Stopped before validation' : 'Waiting for retrieval',
      status: stageStatus('validating', Boolean(rag && citationFieldsValid), Boolean(rag && !citationFieldsValid) || ragBlocked),
    },
    {
      label: 'Root-cause analysis',
      detail: rootCause ? `${rootCause.validatedRootCauses.length} validated cause(s)` : 'Not run',
      status: stageStatus('analyzing', Boolean(rootCause), rootCauseBlocked),
    },
    {
      label: 'Recommendation generation',
      detail: recommendations ? `${recommendations.options.length} option(s)` : 'Not run',
      status: stageStatus('recommending', Boolean(recommendations), recommendationBlocked),
    },
    {
      label: 'Recommendation validation',
      detail: recommendations ? 'Grounding metadata returned' : recommendationBlocked ? 'Blocked' : 'Awaiting recommendations',
      status: stageStatus('validating-recommendation', Boolean(recommendations), recommendationBlocked),
    },
  ];

  return (
    <section className="liveTraceWorkspace" aria-labelledby="live-trace-title">
      <div className="traceIntro">
        <div>
          <p className="eyebrow">Live decision trace</p>
          <h3 id="live-trace-title">From a rural problem to evidence-backed actions</h3>
          <p>
            This workspace follows the existing backend pipeline. It displays retrieved evidence,
            persisted analysis, and recommendation metadata without exposing private model reasoning.
          </p>
        </div>
        <span className="modelChip">Qwen / governed RAG</span>
      </div>

      <div className="traceInputPanel">
        <label htmlFor="live-trace-problem">Describe a rural problem</label>
        <textarea
          id="live-trace-problem"
          value={problem}
          onChange={(event) => onProblemChange(event.target.value)}
          placeholder="Describe a rural problem..."
          rows={4}
        />
        <label htmlFor="live-trace-domain">
          Problem domain
          <select id="live-trace-domain" value={domain} onChange={(event) => onDomainChange(event.target.value)}>
            {[
              { label: 'Agriculture', value: 'Agriculture' },
              { label: 'Healthcare', value: 'Healthcare' },
              { label: 'Energy', value: 'energy' },
              { label: 'Education', value: 'Education' },
              { label: 'Livelihoods', value: 'livelihood' },
              { label: 'Water / sanitation', value: 'sanitation' },
            ].map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </label>
        <p className="traceExample">Example: Small farmers in our area are experiencing declining crop yields because soil quality has deteriorated over several growing seasons.</p>
        <div className="traceActionRow">
          <button type="button" onClick={onRun} disabled={busy || !problem.trim() || !authenticated || !canRun}>
            {busy ? 'Analyzing problem...' : 'Analyze Problem'}
          </button>
          {busy ? <span className="traceProcessing" aria-live="polite">Processing live evidence and decision stages</span> : null}
          {!authenticated ? <span className="mutedCopy">Sign in to run the governed pipeline.</span> : null}
          {authenticated && !canRun ? <span className="mutedCopy">Create a survey before running this existing workflow.</span> : null}
        </div>
      </div>

      <div className="traceRail" aria-label="Live RAG decision stages">
        {stages.map((stage, index) => (
          <div className={`traceStage traceStage-${stage.status}`} key={stage.label}>
            <span className="traceStageIndex">{stage.status === 'complete' ? 'OK' : stage.status === 'processing' ? '...' : String(index + 1).padStart(2, '0')}</span>
            <strong>{stage.label}</strong>
            <span>{stage.detail}</span>
          </div>
        ))}
      </div>

      {error ? (
        <div className="traceFailure" role="alert">
          <strong>Pipeline stopped</strong>
          <span>{error}</span>
          <small>No fallback evidence or fabricated decision was shown.</small>
        </div>
      ) : null}

      <section className="traceSection">
        <TraceSectionHeading eyebrow="Evidence retrieval" title="What the system retrieved" status={rag ? 'Returned' : 'Waiting'} />
        {rag ? (
          <>
            <div className="traceSummaryGrid">
              <MetricCard label="Sources" value={String(rag.citations.length)} detail="Returned by RAG API" tone={hasCitations ? 'good' : 'warn'} />
              <MetricCard label="Citation contract" value={rag.citationValidationStatus ?? (citationFieldsValid ? 'Fields present' : 'Not valid')} detail="Response metadata" tone={citationFieldsValid ? 'good' : 'warn'} />
              <MetricCard label="RAG status" value={rag.supportStatus ?? 'Returned'} detail={rag.reasoningSummary ?? 'Status field not exposed by gateway'} tone={hasCitations ? 'good' : 'warn'} />
            </div>
            <p className="traceAnswer">{rag.answer}</p>
            <div className="traceEvidenceList">
              {rag.citations.map((citation) => (
                <article className="traceEvidenceRow" key={`${citation.sourceType}-${citation.sourceId}`}>
                  <div>
                    <span className="traceEvidenceBadge">VERIFIED EVIDENCE</span>
                    <span className="traceEvidenceId">Source ID: {citation.sourceId}</span>
                    <strong>{citation.title ?? citation.documentId ?? 'Source title unavailable'}</strong>
                  </div>
                  <p>{citation.excerpt}</p>
                  <div className="traceEvidenceMeta">
                    <span>{citation.sourceType}</span>
                    {citation.citationId ? <span>Citation ID: {citation.citationId}</span> : null}
                    {citation.publisher ? <span>{citation.publisher}</span> : null}
                    {citation.page !== undefined ? <span>Page {citation.page}</span> : null}
                    {citation.score !== undefined ? <span className="confidenceValue">Score {Math.round(citation.score * 100)}%</span> : null}
                  </div>
                </article>
              ))}
            </div>
            <div className="traceCheckGrid">
              <TraceCheck label="Source ID present" result={citationFieldsValid ? 'Verified from response' : 'Blocked'} tone={citationFieldsValid ? 'good' : 'bad'} />
              <TraceCheck label="Excerpt present" result={citationFieldsValid ? 'Verified from response' : 'Blocked'} tone={citationFieldsValid ? 'good' : 'bad'} />
              <TraceCheck label="Scenario relevance / PII / synthetic filtering" result="Authoritative gate metadata is not exposed by this response" tone="neutral" />
            </div>
          </>
        ) : (
          <EmptyState title={ragBlocked ? 'RAG service unavailable' : 'Evidence retrieval has not run'} body={ragBlocked ? 'The live request stopped before a retrieval response was returned. No fallback evidence was shown.' : 'Submit a problem to retrieve the governed knowledge context.'} />
        )}
      </section>

      <section className="traceSection">
        <TraceSectionHeading eyebrow="Root cause" title="Auditable analysis" status={rootCause ? 'Persisted' : rootCauseBlocked ? 'Blocked' : 'Waiting'} />
        {rootCause ? (
          <>
            <div className="traceSummaryGrid">
              <MetricCard label="Confidence" value={`${Math.round(rootCause.confidence.overall * 100)}%`} detail={rootCause.confidence.interpretation} tone="good" />
              <MetricCard label="Observed facts" value={String(rootCause.observedFacts.length)} detail="Returned fact records" tone="good" />
              <MetricCard label="Uncertainty" value={String(rootCause.uncertainties.length)} detail="Open uncertainty records" tone={rootCause.uncertainties.length ? 'warn' : 'neutral'} />
            </div>
            <div className="traceDecisionGrid">
              <TraceList title="Observed facts" items={rootCause.observedFacts.map((fact) => `${fact.category}: ${fact.statement} (${fact.sourceType})`)} />
              <TraceList title="Validated root causes" items={rootCause.validatedRootCauses.map((cause) => `${cause.description} (${Math.round(cause.confidence * 100)}%)`)} />
              <TraceList title="Uncertainty" items={rootCause.uncertainties.map((item) => `${item.severity}: ${item.statement}`)} />
            </div>
            {llm ? <p className="traceMeta">Model output: {llm.model} / prompt {llm.promptVersion} / {llm.latencyMs} ms.</p> : null}
          </>
        ) : (
          <EmptyState title={rootCauseBlocked ? 'Root-cause analysis blocked' : 'Root-cause analysis is waiting'} body={rootCauseBlocked ? 'The backend did not return a supported analysis for this trace.' : 'Validated retrieval is required before analysis can be displayed.'} />
        )}
      </section>

      <section className="traceSection">
        <TraceSectionHeading eyebrow="Recommendations" title="Ranked actions with traceable support" status={recommendations ? `${recommendations.options.length} options` : recommendationBlocked ? 'Blocked' : 'Waiting'} />
        {recommendations ? (
          <div className="traceRecommendationList">
            {recommendations.options.slice().sort((left, right) => left.priority - right.priority).map((option) => (
              <details className="traceRecommendation" key={option.recommendationId}>
                <summary>
                  <span className="priorityBadge">P{option.priority}</span>
                  <strong>{option.title}</strong>
                  <span className="sectionMeta">{option.feasibility.rating}</span>
                </summary>
                <div className="traceRecommendationBody">
                  <p>{option.description}</p>
                  <dl>
                    <dt>Target root cause</dt><dd>{option.targetRootCause}</dd>
                    <dt>Feasibility / effort</dt><dd>{option.feasibility.rating} / {option.estimatedEffort}</dd>
                    <dt>Timeframe</dt><dd>{option.estimatedTimeframe}</dd>
                    <dt>Confidence</dt><dd>{Math.round(option.confidence.recommendationConfidence * 100)}%</dd>
                  </dl>
                  <div className="traceDecisionGrid">
                    <TraceList
                      title="Why this recommendation?"
                      items={[
                        ...option.evidence,
                        ...(option.confidence.interpretation ? [option.confidence.interpretation] : []),
                      ]}
                    />
                    <TraceList title="Risks" items={option.risks.map((risk) => `${risk.severity}: ${risk.description}. ${risk.mitigation}`)} />
                    <TraceList title="Dependencies" items={option.dependencies} />
                  </div>
                  <div className="traceSourceChain">
                    Recommendation <span>to</span> {option.targetRootCause} <span>via</span> {option.evidence.join(', ') || 'No evidence returned'}
                  </div>
                </div>
              </details>
            ))}
          </div>
        ) : (
          <EmptyState title={recommendationBlocked ? 'Recommendations blocked' : 'Recommendations are waiting'} body={recommendationBlocked ? 'Recommendations were not displayed because the existing backend workflow did not return a valid set.' : 'Recommendations appear only after the root-cause stage completes.'} />
        )}
      </section>

      <section className="traceFinalSummary">
        <p className="eyebrow">Decision summary</p>
        <h3>Problem to validated evidence to root cause to recommended actions</h3>
        <p>{problem || 'No problem submitted.'}</p>
        <div className="traceSummaryLine">
          <span>{hasCitations ? `${rag?.citations.length} source(s) returned` : 'Evidence pending'}</span>
          <span>{rootCause ? `${rootCause.validatedRootCauses.length} validated cause(s)` : 'Root cause pending'}</span>
          <span>{recommendations ? `${recommendations.options.length} action(s) returned` : 'Actions pending'}</span>
        </div>
      </section>
    </section>
  );
}

function TraceSectionHeading({ eyebrow, title, status }: Readonly<{ eyebrow: string; title: string; status: string }>) {
  return (
    <div className="sectionHeading">
      <div><p className="eyebrow">{eyebrow}</p><h3>{title}</h3></div>
      <span className="sectionMeta">{status}</span>
    </div>
  );
}

function TraceCheck({ label, result, tone }: Readonly<{ label: string; result: string; tone: 'good' | 'bad' | 'neutral' }>) {
  return <div className={`traceCheck traceCheck-${tone}`}><strong>{label}</strong><span>{result}</span></div>;
}

function TraceList({ title, items }: Readonly<{ title: string; items: string[] }>) {
  return <div className="traceList"><h4>{title}</h4>{items.length ? <ul>{items.map((item, index) => <li key={`${title}-${index}`}>{item}</li>)}</ul> : <p className="mutedCopy">Not returned.</p>}</div>;
}

export function MetricCard({
  label,
  value,
  detail,
  tone = 'neutral',
}: Readonly<{
  label: string;
  value: string;
  detail?: string;
  tone?: 'neutral' | 'good' | 'warn';
}>) {
  return (
    <article className={`metric metric-${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
      {detail ? <small>{detail}</small> : null}
    </article>
  );
}

export function EmptyState({
  title,
  body,
  action,
}: Readonly<{ title: string; body: string; action?: ReactNode }>) {
  return (
    <div className="emptyStateBlock">
      <span className="emptyStateIcon" aria-hidden="true">
        --
      </span>
      <div>
        <strong>{title}</strong>
        <p>{body}</p>
      </div>
      {action}
    </div>
  );
}

export function EvidenceRagView({
  evidence,
  rag,
}: Readonly<{ evidence?: EvidenceSnapshot; rag?: RagSnapshot }>) {
  if (!evidence && !rag) {
    return (
      <EmptyState
        title="No evidence or retrieval yet"
        body="Upload supporting material and run the RAG step to establish a source-backed decision record."
      />
    );
  }
  return (
    <div className="evidenceWorkspace">
      <div className="evidenceSummaryGrid">
        <MetricCard
          label="Evidence asset"
          value={evidence?.originalFileName ?? 'Not attached'}
          detail={evidence ? evidence.mimeType : 'Awaiting upload'}
          tone={evidence ? 'good' : 'neutral'}
        />
        <MetricCard
          label="Retrieval"
          value={rag ? `${rag.citations.length} sources` : 'Not run'}
          detail={rag ? 'Citations returned' : 'Awaiting query'}
          tone={rag ? 'good' : 'neutral'}
        />
        <MetricCard
          label="Integrity"
          value={evidence ? 'SHA-256 recorded' : 'Not available'}
          detail={evidence?.sha256Checksum ?? 'No checksum'}
          tone={evidence ? 'good' : 'neutral'}
        />
      </div>
      {evidence ? (
        <section className="evidenceBlock">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Asset record</p>
              <h3>{evidence.title}</h3>
            </div>
            <span className="status ok">Stored</span>
          </div>
          <dl>
            <dt>File</dt>
            <dd>{evidence.originalFileName}</dd>
            <dt>Type / size</dt>
            <dd>
              {evidence.mimeType} / {formatBytes(evidence.sizeBytes)}
            </dd>
            <dt>Checksum</dt>
            <dd>{evidence.sha256Checksum}</dd>
          </dl>
        </section>
      ) : null}
      {rag ? (
        <section className="evidenceBlock">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Knowledge retrieval</p>
              <h3>Grounding context</h3>
            </div>
            <span className="sectionMeta">{rag.citations.length} citations</span>
          </div>
          <p className="answerLead">{rag.answer}</p>
          <div className="citationList">
            {rag.citations.map((citation) => (
              <article className="citationRow" key={`${citation.sourceType}-${citation.sourceId}`}>
                <div className="citationId">{citation.sourceId}</div>
                <div>
                  <strong>{citation.sourceType}</strong>
                  <p>{citation.excerpt}</p>
                </div>
                <span className="confidenceValue">{Math.round(citation.score * 100)}%</span>
              </article>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  );
}

export function AiAnalysisView({
  rag,
  llm,
  rootCause,
  decision,
}: Readonly<{
  rag?: RagSnapshot;
  llm?: LlmSnapshot;
  rootCause?: RootCauseSnapshot;
  decision?: DecisionSnapshot;
}>) {
  if (!rag && !llm && !rootCause && !decision) {
    return (
      <EmptyState
        title="No analysis in this session"
        body="Run the governed workflow after creating a survey and attaching evidence."
      />
    );
  }
  return (
    <div className="decisionWorkspace">
      <EvidenceRagView rag={rag} />
      {llm ? (
        <section className="decisionSection">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Model interpretation</p>
              <h3>Qwen analysis</h3>
            </div>
            <span className="sectionMeta">
              {llm.model} / {llm.latencyMs} ms
            </span>
          </div>
          <p className="analysisLead">{llm.output.summary}</p>
          <div className="decisionGrid">
            <InsightList title="Contributing factors" items={llm.output.contributingFactors} />
            <InsightList title="Root-cause signals" items={llm.output.rootCauses} />
            <InsightList title="Evidence used" items={llm.output.evidence} />
            <InsightList title="Recommended direction" items={llm.output.recommendations} />
          </div>
          <div className="uncertaintyBar">
            <strong>Confidence {Math.round(llm.output.confidence * 100)}%</strong>
            <span>{llm.output.limitations.join(' ') || 'No limitations recorded.'}</span>
          </div>
        </section>
      ) : null}
      {rootCause ? <RootCauseView rootCause={rootCause} compact /> : null}
      {decision ? (
        <section className="decisionSection decisionOutcome">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Decision record</p>
              <h3>{decision.finalDecision}</h3>
            </div>
            <span className="confidenceBadge">
              {Math.round(decision.confidence * 100)}% confidence
            </span>
          </div>
          <div className="decisionGrid">
            <InsightList
              title="Root causes"
              items={decision.rootCauses.map((cause) => `${cause.title}: ${cause.description}`)}
            />
            <InsightList
              title="Next actions"
              items={decision.recommendations.map(
                (item) => `P${item.priority} ${item.title}: ${item.description}`,
              )}
            />
          </div>
        </section>
      ) : null}
    </div>
  );
}

export function RootCauseView({
  rootCause,
  compact = false,
}: Readonly<{ rootCause: RootCauseSnapshot; compact?: boolean }>) {
  return (
    <section className={`decisionSection rootCauseView ${compact ? 'compact' : ''}`}>
      <div className="sectionHeading">
        <div>
          <p className="eyebrow">Root-cause analysis</p>
          <h3>{rootCause.problem.description}</h3>
        </div>
        <span className="confidenceBadge">
          {Math.round(rootCause.confidence.overall * 100)}% confidence
        </span>
      </div>
      <dl>
        <dt>Location</dt>
        <dd>{rootCause.problem.village}</dd>
        <dt>Domain</dt>
        <dd>{rootCause.problem.domain}</dd>
        <dt>Severity</dt>
        <dd>{rootCause.problem.severity}</dd>
        <dt>Interpretation</dt>
        <dd>{rootCause.confidence.interpretation}</dd>
      </dl>
      <div className="decisionGrid">
        <InsightList
          title="Observed facts"
          items={rootCause.observedFacts.map((fact) => `${fact.category}: ${fact.statement}`)}
        />
        <InsightList
          title="Validated root causes"
          items={
            rootCause.validatedRootCauses.length
              ? rootCause.validatedRootCauses.map(
                  (cause) => `${cause.description} (${Math.round(cause.confidence * 100)}%)`,
                )
              : ['No validated root cause recorded.']
          }
        />
        {!compact ? (
          <InsightList
            title="Alternative hypotheses"
            items={rootCause.alternativeHypotheses.map(
              (hypothesis) =>
                `${hypothesis.description} (${Math.round(hypothesis.confidence * 100)}%)`,
            )}
          />
        ) : null}
        {!compact ? (
          <InsightList
            title="Uncertainty and follow-up"
            items={rootCause.uncertainties.map((item) => `${item.severity}: ${item.statement}`)}
          />
        ) : null}
      </div>
      {!compact && rootCause.limitations.length ? (
        <div className="uncertaintyBar">
          <strong>Limitations</strong>
          <span>{rootCause.limitations.join(' ')}</span>
        </div>
      ) : null}
    </section>
  );
}

export function RecommendationWorkspace({
  recommendation,
  actions,
}: Readonly<{ recommendation?: RecommendationSnapshot; actions?: ReactNode }>) {
  if (!recommendation) {
    return (
      <EmptyState
        title="No recommendations yet"
        body="Complete the AI analysis workflow to compare interventions grounded in the validated root-cause record."
      />
    );
  }
  return (
    <div className="recommendationWorkspace">
      <section className="decisionSection">
        <div className="sectionHeading">
          <div>
            <p className="eyebrow">Action selection</p>
            <h3>Recommendation set</h3>
          </div>
          <div className="topbarMeta">
            <span className="status">{recommendation.status}</span>
            <span className="sectionMeta">{recommendation.options.length} options</span>
          </div>
        </div>
        <dl>
          <dt>Model / prompt</dt>
          <dd>
            {recommendation.model} / {recommendation.promptVersion}
          </dd>
          <dt>Set ID</dt>
          <dd>{recommendation.recommendationSetId}</dd>
        </dl>
        {actions ? <div className="reviewBar">{actions}</div> : null}
      </section>
      <div className="recommendationGrid">
        {recommendation.options
          .slice()
          .sort((left, right) => left.priority - right.priority)
          .map((option) => (
            <RecommendationCard key={option.recommendationId} option={option} />
          ))}
      </div>
      {recommendation.options[0]?.implementationPlan.length ? (
        <ImplementationTimeline phases={recommendation.options[0].implementationPlan} />
      ) : null}
      {recommendation.comparison.length ? (
        <section className="decisionSection">
          <div className="sectionHeading">
            <div>
              <p className="eyebrow">Trade-offs</p>
              <h3>Compare interventions</h3>
            </div>
          </div>
          <div className="comparisonList">
            {recommendation.comparison.map((item) => (
              <div className="comparisonRow" key={item.recommendationId}>
                <strong>{item.recommendationId}</strong>
                <span>{Math.round(item.priorityScore * 100)}% priority</span>
                <span>
                  {item.effortCategory} effort / {item.complexity}
                </span>
                <p>{item.advantages.join('; ') || 'No advantages recorded.'}</p>
              </div>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  );
}

function RecommendationCard({ option }: Readonly<{ option: RecommendationOption }>) {
  return (
    <article className="recommendationCard">
      <div className="recommendationCardHeader">
        <span className="priorityBadge">P{option.priority}</span>
        <span className="status">{option.feasibility.rating}</span>
      </div>
      <h3>{option.title}</h3>
      <p>{option.description}</p>
      <div className="tagRow">
        <span>{option.domain}</span>
        <span>{option.interventionType}</span>
        <span>{option.estimatedTimeframe}</span>
      </div>
      <dl>
        <dt>Addresses</dt>
        <dd>{option.targetRootCause}</dd>
        <dt>Effort</dt>
        <dd>{option.estimatedEffort}</dd>
        <dt>Resources</dt>
        <dd>{option.requiredResources.join(', ') || 'Not recorded'}</dd>
      </dl>
      <div className="confidenceGrid">
        <span>
          Evidence <strong>{Math.round(option.confidence.evidenceStrength * 100)}%</strong>
        </span>
        <span>
          Confidence{' '}
          <strong>{Math.round(option.confidence.recommendationConfidence * 100)}%</strong>
        </span>
        <span>
          Feasibility{' '}
          <strong>{Math.round(option.confidence.implementationFeasibility * 100)}%</strong>
        </span>
      </div>
      <details>
        <summary>Risks, evidence, and assumptions</summary>
        <div className="detailStack">
          <InsightList title="Evidence" items={option.evidence} />
          <InsightList
            title="Risks"
            items={option.risks.map(
              (risk) => `${risk.severity}: ${risk.description}. Mitigation: ${risk.mitigation}`,
            )}
          />
          <InsightList title="Assumptions" items={option.assumptions} />
        </div>
      </details>
    </article>
  );
}

function ImplementationTimeline({
  phases,
}: Readonly<{ phases: RecommendationOption['implementationPlan'] }>) {
  return (
    <section className="decisionSection">
      <div className="sectionHeading">
        <div>
          <p className="eyebrow">Delivery path</p>
          <h3>Implementation plan</h3>
        </div>
        <span className="sectionMeta">{phases.length} phases</span>
      </div>
      <ol className="implementationTimeline">
        {phases.map((phase, index) => (
          <li key={`${phase.phase}-${index}`}>
            <span className="timelineMarker">{String(index + 1).padStart(2, '0')}</span>
            <div>
              <strong>{phase.phase}</strong>
              <span>{phase.responsibleRole}</span>
              <p>{phase.actions.join('; ')}</p>
              {phase.successIndicators.length ? (
                <small>Success: {phase.successIndicators.join('; ')}</small>
              ) : null}
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}

function InsightList({ title, items }: Readonly<{ title: string; items: string[] }>) {
  return (
    <div className="insightList">
      <h4>{title}</h4>
      {items.length ? (
        <ul>
          {items.map((item, index) => (
            <li key={`${title}-${index}`}>{item}</li>
          ))}
        </ul>
      ) : (
        <p className="mutedCopy">Not recorded.</p>
      )}
    </div>
  );
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
