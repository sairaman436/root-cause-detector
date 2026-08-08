# Human Approval

## Purpose

Defines the approval model for consequential recommendation outputs.

## Workflow

1. Recommendation set is generated in `AI_GENERATED`.
2. Reviewer records `ACCEPT`, `EDIT`, `REQUEST_MORE_EVIDENCE`, `REJECT`, or `APPROVE`.
3. `REJECT` moves the set to `REJECTED`.
4. `REQUEST_MORE_EVIDENCE` moves the set to `MORE_EVIDENCE_REQUESTED`.
5. `APPROVE` moves the set to `APPROVED`.
6. `regenerate` supersedes the previous set and creates a new versioned set.

## Review Records

Reviews preserve reviewer id, action, notes, optional modified recommendation JSON, correction text, and timestamp. Review history is append-only and does not delete generated outputs.

## Safety Boundary

Approval means a human has accepted the recommendation set for human-led implementation planning. It does not trigger procurement, assignment, service delivery, field execution, notifications, or automated intervention.
