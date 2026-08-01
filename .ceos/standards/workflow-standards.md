# Workflow Standards

## Purpose

This standard defines how engineering work moves from request to implementation, validation, commit, push, and release.

## Why

The project uses milestone-driven execution. Workflow discipline prevents scope drift, partial validation, unpushed work, and ambiguous status.

## When

Apply this standard to every milestone, fix, audit, hardening task, and documentation update.

## How

- Read the current request and attachments before editing.
- Validate the current branch and working tree.
- Preserve approved prior modules unless the task explicitly requires changes.
- Implement only the requested scope.
- Run relevant validation commands.
- Stage intentionally, commit with a clear message, and push when the user requires milestone pushes.
- Report exact validation status and any unavailable tools.

## Tradeoffs

This workflow spends time on shipping hygiene. It prevents incomplete handoffs and confusion about whether work reached the remote repository.

## Best Practices

- Use `rg` for file discovery.
- Review staged diffs before committing.
- Keep final summaries concise and evidence-based.
- Distinguish local validation from CI proof.
- Push each milestone when requested.

## Anti-Patterns

- Ending with uncommitted work after a milestone request.
- Claiming CI passed when only local checks ran.
- Mixing unrelated cleanup into feature work.
- Reverting user changes without approval.
- Ignoring failed validation because the change “should work.”

## Related Documents

See [Engineering Constitution](../constitutions/engineering-constitution.md), [Release Constitution](../constitutions/release-constitution.md), [Engineering Playbooks](../playbooks/engineering-playbooks.md), and [Project Memory](../memory/PROJECT_MEMORY.md).
