# Development container

## 1 Purpose

The development container provides the Rust toolchain, Codex CLI and VS Code
extension, Rust extensions, and GitHub CLI needed to work on this repository and
prepare pull requests. It does not mount the host Docker socket into the agent
container.

## 2 Start the environment

1. Install Docker and VS Code with the Dev Containers extension.
2. Open this repository in VS Code and run **Dev Containers: Reopen in Container**.
3. Wait for the Rust tools and locked Cargo dependencies to finish installing.
4. Start `codex` in the integrated terminal and sign in when prompted, or sign in
   through the Codex VS Code extension.

Codex reads the repository's root `AGENTS.md` and the more specific `docs/AGENTS.md`
when applicable. Follow those instructions and inspect existing changes before
editing.

## 3 Database services

Start the repository databases from a host terminal when needed:

```sh
docker compose -f docker-compose/compose.yaml up -d
```

The Compose database ports are published by Docker on the host. If database
clients in the development container need access, configure them to connect via
the host gateway (`host.docker.internal`) on port `5431` for core PostgreSQL or
port `5433` for telemetry PostgreSQL. Host firewall and Docker settings may affect
that route. The Compose data directories persist on the host. Do not remove them
or run destructive operations against non-test data during agent work.

Stop the services from the host terminal when finished:

```sh
docker compose -f docker-compose/compose.yaml down
```

Use isolated test databases and apply migrations before database integration tests.
Unit tests must not require these services.

## 4 Agent changes and pull requests

Use a dedicated branch for each task. Ask the agent to inspect the applicable
requirements, architecture, decisions, and current Git changes before making
focused edits. Run the checks required by `CONTRIBUTING.md`, inspect the final diff,
and resolve any failures before preparing a pull request.

Use a dedicated GitHub account for the agent. Do not authenticate the agent as
the upstream repository owner or as any identity with upstream write or admin
access.

Create a separate account for agent work, then create a fork of
`pondem87/iot-hub` under that account. Give the account no direct access to the
upstream repository. It can push work to its fork and propose changes by pull
request; because it has no upstream write permission, it cannot merge to upstream
`main`. GitHub allows a contributor to open a public-repository PR when they have
write access to the source branch in their fork.

For the dedicated account, create a short-lived classic personal access token
with only the `public_repo` scope. Keep that account limited to its fork; this
scope applies to public repositories the account can access. Do not grant the
agent upstream collaborator access. GitHub documents that classic tokens are
needed for write access to public repositories the account does not own, while
repository access still depends on the account's role. See
[GitHub token guidance](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

Authenticate GitHub CLI inside the container with that token (paste it at the
hidden prompt; do not put it in shell history or in this repository):

```sh
read -rsp 'Agent GitHub token: ' AGENT_GH_TOKEN
printf '\n'
printf '%s' "$AGENT_GH_TOKEN" | gh auth login \
  --hostname github.com --git-protocol https --with-token
unset AGENT_GH_TOKEN
gh auth status
```

After forking, add the fork as a separate push remote. Keep `origin` as the
upstream read remote:

```sh
git remote add agent https://github.com/your-agent-account/iot-hub.git
git remote -v
```

Push the task branch to the fork, then open a draft PR against upstream `main`:

```sh
AGENT_ACCOUNT=your-agent-account
BRANCH=agent/short-task
git switch -c "$BRANCH"
git push agent "HEAD:refs/heads/$BRANCH"
gh pr create --repo pondem87/iot-hub \
  --head "$AGENT_ACCOUNT:$BRANCH" --base main --draft \
  --template .github/pull_request_template.md
```

Review the staged changes and generated PR description before submission. Do not
commit credentials or copy host credentials into the repository. The Codex
extension and GitHub CLI authenticate separately; complete each sign-in only in
the trusted development environment. Do not share the token in chat or log output.
