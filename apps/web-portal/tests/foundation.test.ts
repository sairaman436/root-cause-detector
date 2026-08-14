// Purpose: Verifies that the web portal health route is wired.
// Why it exists: Gives frontend CI a meaningful foundation test before product UI exists.
// Architecture fit: Supports container and orchestrator readiness checks for the frontend.
import { describe, expect, it } from 'vitest';

import { GET } from '../src/app/api/health/route';
import {
  buildVisionRetrievalQuery,
  buildVisionRetrievalRepresentation,
  collectTrainingCandidatePages,
  MULTIMODAL_REVIEW_API_PATH,
  MULTIMODAL_TRACE_API_PATH,
  navItems,
  readableApiError,
} from '../src/app/page';

describe('web portal foundation', () => {
  it('returns a healthy service contract', async () => {
    const body = await GET().json();

    expect(body).toEqual({
      service: 'web-portal',
      status: 'ok',
    });
  });

  it('collects pending candidates that are on later API pages', async () => {
    const pages = [
      Array.from({ length: 50 }, (_, index) => ({ id: `historical-${index}` })),
      Array.from({ length: 11 }, (_, index) => ({ id: `remediation-${index}` })),
    ];

    const candidates = await collectTrainingCandidatePages(
      async (page, size) => ({
        content: pages[page] ?? [],
        totalElements: 61,
        totalPages: 2,
        number: page,
        size,
      }),
    );

    expect(candidates).toHaveLength(61);
    expect(candidates.slice(-11).map((candidate) => candidate.id)).toEqual(
      Array.from({ length: 11 }, (_, index) => `remediation-${index}`),
    );
  });

  it('exposes the evaluation lab in the primary navigation', () => {
    expect(navItems).toContain('Multimodal Evaluation Lab');
    expect(MULTIMODAL_TRACE_API_PATH).toBe('/api/v1/evaluation/multimodal/traces');
    expect(MULTIMODAL_REVIEW_API_PATH).toBe('/api/v1/evaluation/multimodal/reviews');
  });

  it('preserves actual visual concepts and maps the user label to the corpus domain', () => {
    const representation = buildVisionRetrievalRepresentation(
      'Water & sanitation',
      'What visible sanitation infrastructure is present?',
      [{ description: 'A water tank and a dirt road are visible.', type: 'SCENE' }],
      'The image does not establish whether the tank is operational.',
    );

    expect(representation.domainContext).toMatchObject({
      label: 'Water & sanitation',
      retrievalDomain: 'sanitation',
      source: 'USER_PROVIDED_CONTEXT',
      imageDomainAssumed: false,
    });
    expect(representation.observedConcepts).toEqual(expect.arrayContaining(['water tank', 'dirt road', 'tank', 'road']));
    expect(buildVisionRetrievalQuery(representation)).toContain('image domain not assumed');
    expect(buildVisionRetrievalQuery(representation)).toContain('operational');
  });

  it('keeps uncertain healthcare observations distinct from a forced facility classification', () => {
    const representation = buildVisionRetrievalRepresentation(
      'Healthcare',
      'What visible facility characteristics should be noted?',
      [{ description: 'The building appears to be an old schoolhouse beside a dirt road.', type: 'FACILITY' }],
      'The model cannot confirm whether the building is a healthcare facility.',
    );
    const query = buildVisionRetrievalQuery(representation);

    expect(representation.observedConcepts).toEqual(expect.arrayContaining(['schoolhouse', 'building', 'dirt road', 'road']));
    expect(query).toContain('Governed retrieval domain: healthcare');
    expect(query).toContain('cannot confirm whether the building is a healthcare facility');
    expect(query).not.toContain('Observed concepts: clinic');
  });

  it('does not invent observed concepts when the model returns only uncertainty', () => {
    const representation = buildVisionRetrievalRepresentation(
      'Energy',
      'What visible energy infrastructure is present?',
      [{ description: 'A village scene is visible, but no energy equipment is identifiable.', type: 'SCENE' }],
      'Energy infrastructure is not established by this image.',
    );

    expect(representation.observedConcepts).toEqual(expect.arrayContaining(['village', 'energy']));
    expect(representation.observedConcepts).not.toContain('electricity');
    expect(representation.observedConcepts).not.toContain('transformer');
  });

  it('surfaces access denial without presenting stale downstream results', () => {
    expect(readableApiError(JSON.stringify({ errorCode: 'ACCESS_DENIED', message: 'Access denied' }), 403)).toBe('Access denied');
    expect(readableApiError(JSON.stringify({ errorCode: 'AUTHENTICATION_REQUIRED' }), 401)).toBe(
      'Your session expired or the access token was rejected. Sign in again.',
    );
  });
});
