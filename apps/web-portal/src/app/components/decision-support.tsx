'use client';

/*
 * Purpose: Presents typed AI, evidence, recommendation, and governance data.
 * Why it exists: Decision-makers need scannable, source-aware views instead of raw API payloads.
 * Architecture fit: This is a pure web presentation adapter; it consumes existing API-shaped data and owns no business decisions.
 */

import type { ReactNode } from 'react';

export type EvidenceSnapshot = {
  originalFileName: string;
  mimeType: string;
  sizeBytes: number;
  sha256Checksum: string;
  title: string;
};

export type RagSnapshot = {
  answer: string;
  citations: Array<{ sourceType: string; sourceId: string; excerpt: string; score: number }>;
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
