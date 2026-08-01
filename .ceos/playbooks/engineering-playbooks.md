# Engineering Playbooks

## Purpose

This playbook defines the standard execution loop for implementation and documentation work.

## Why

Consistent execution reduces missed context, scope drift, and unverified changes.

## When

Use this for every milestone, bug fix, audit remediation, documentation update, and production hardening task.

## How

1. Read the request, attachments, related CEOS documents, and relevant repository docs.
2. Check `git status -sb`.
3. Inspect existing patterns before editing.
4. Identify the smallest complete change.
5. Implement with existing module boundaries.
6. Run focused validation first, then broader validation.
7. Review diffs and staged scope.
8. Commit and push when required.
9. Report what changed, what passed, what could not run, and why.

## Tradeoffs

The loop adds upfront reading and final shipping checks. It prevents rework and incomplete delivery.

## Best Practices

- Use parallel reads for independent files.
- Keep user updates short and factual.
- Fix validation failures before moving to staging.
- Avoid unrelated refactors.
- Leave a clean working tree after milestone completion.

## Anti-Patterns

- Implementing before reading nearby code.
- Creating a plan but not executing when implementation is requested.
- Committing generated or cache files.
- Stopping after the first successful command when other gates are relevant.
- Reporting success without push confirmation.

## Related Documents

See [Workflow Standards](../standards/workflow-standards.md), [Testing Standards](../standards/testing-standards.md), [Release Playbook](release-playbook.md), and [Project Memory](../memory/PROJECT_MEMORY.md).
