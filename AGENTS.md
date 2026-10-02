# Instructions for coding agents

These instructions apply throughout this repository. Read any nested `AGENTS.md`
that applies to the files you work on and follow its more specific instructions.

## Read and inspect before changing

Read the project's required standards before editing:

- [STYLE_GUIDE.md](STYLE_GUIDE.md): file organization, Rust documentation,
  typestate, error handling, and test conventions.
- [ARCHITECTURE.md](ARCHITECTURE.md): component responsibilities, service
  boundaries, repository contracts, and error translation.
- [CONTRIBUTING.md](CONTRIBUTING.md): development workflow, compatibility,
  validation, and review requirements.
- [docs/decisions/README.md](docs/decisions/README.md): how to find, interpret,
  and maintain Architecture Decision Records (ADRs).

Use those documents as the detailed sources of truth; this file directs the
agent workflow. Before a material architectural or domain-model change, inspect
the relevant ADRs in `docs/decisions/` and follow accepted decisions unless the
task explicitly revisits them. Read the requirements and architecture documents
in `docs/` relevant to the task. Inspect affected code, callers, dependencies,
tests, and current Git changes before deciding how to implement it. Use repository
evidence to resolve discoverable questions rather than asking the user to locate
code.

Follow the documented standards even when existing code differs. Do not treat
placeholders, missing implementations, or noncompliant examples as requirements.

## Implement focused changes

Follow the [issue tracking workflow](CONTRIBUTING.md#issue-tracking) when working
from an issue. Read its acceptance criteria and linked requirements before making
changes; report unresolved criteria and reference the issue in the PR. Do not
claim an issue is resolved while its acceptance criteria remain unmet.

Complete the work authorized by the task, including related tests and
documentation. Resolve routine implementation choices independently. Ask for
clarification when missing business rules or conflicting requirements materially
affect behavior; do not invent lifecycle transitions or access policies.

Preserve unrelated user changes. Keep edits focused, and avoid broad refactors,
unrelated formatting, or incidental dependency upgrades. Do not revert work you
did not make. Keep generated artifacts, database contents, credentials, and
sensitive user information out of commits and tool output. Do not run destructive
database operations against shared or non-test data as part of validation.

When changing implementation, verify these project invariants using the guides:

- Each service and repository implementation has its own file; domains with
  multiple implementations use the corresponding module directories.
- Structs and traits have Rustdoc, and publicly accessible services implement
  meaningful traits covering their business operations.
- Stateful domain objects enforce lifecycle rules through typestate and controlled
  construction and transitions.
- Repositories validate state after deserialization and return validated typed
  objects. An existing row in the wrong state produces an error; normal absence
  alone may produce `None`.
- Files containing behavior have colocated test modules whose opening Rustdoc
  describes the subject and lists the tests and their purpose.
- Errors are structured and translated at component boundaries. Technical causes
  stay internal, logging is not duplicated, and external responses are safe.

Update affected requirements, contracts, examples, and architecture documentation
alongside behavior changes. Justify dependency changes and include corresponding
manifest and lockfile updates. Follow the contribution guide for migrations and
compatibility; do not rewrite migrations already applied to shared environments.

## Validate the result

Write documentation for source editing as well as rendering. Follow the
[Markdown source style](STYLE_GUIDE.md#markdown-source-readability): requirements,
business architecture, and use cases use labeled records and numbered flows,
including in templates, rather than tables. Preserve IDs, anchors, and meaning
when changing document presentation.

Use the commands and acceptance requirements in
[CONTRIBUTING.md](CONTRIBUTING.md#validate-and-report-results). Run focused checks
during development and the required checks before declaring code ready to merge.
Use isolated, migrated databases for integration tests; unit tests must not depend
on live services. Do not disable tests, weaken assertions, or suppress diagnostics
merely to obtain a passing result.

For documentation-only changes, validate links, examples, and consistency with
the project standards. Do not expand a prose change into an unrelated code repair.

Inspect the final diff for scope, accidental data exposure, and unintended edits.
Report failed or unavailable checks with their cause and impact. An existing
failure does not make a required check optional: keep unrelated repairs separately
scoped and do not describe the change as ready to merge while required checks
remain unresolved. Never claim a check passed unless it ran and passed.

## Prepare pull requests

When drafting or updating a pull request, read and use
[.github/pull_request_template.md](.github/pull_request_template.md). Follow its
sections and prompts, describing the final change, motivation, design decisions,
verification, compatibility impact, documentation, reviewer focus, and follow-up
work. Link relevant issues, requirements, or use cases.

Replace placeholders with concrete information and keep detail proportional to
the change. Omit optional sections only as the template permits; use `None` or
`Not applicable` where appropriate. Check checklist items only when supported by
the work and verification actually performed. Explain failed, unrun, or inapplicable
checks in the verification notes rather than marking them as passed.

## Report the outcome

Summarize the behavior or documentation changed, the validation actually
performed, and any unresolved issues. Identify migration, deployment, and
compatibility implications when relevant. Distinguish completed implementation
from standards or follow-up work that still need implementation. Keep the handoff
concise and reference the relevant files.
