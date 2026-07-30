---
name: weave-ticket
description: Use when the user wants to throw a GitHub ticket (issue or PR) into weave and get a fully projected, isolated space for it in one go — fetch the ticket, scaffold its own space + register, and project its content into faithful facts.
---

# weave-ticket — a GitHub ticket → its own isolated weave space

You turn ONE GitHub ticket into ONE isolated weave space. Every ticket is standalone:
its own register, its own journal, its own facts. Nothing is shared or accumulated across
tickets.

## Iron rules
1. **Isolation.** One ticket ⇒ one space. Never project a second ticket into an existing
   ticket's space, and never do cross-ticket lookup-before-mint.
2. **Journal is truth.** Write the fetched ticket as prose into `journal/ticket.md` FIRST;
   facts are derived from that file (re-projectable; `:k/source` binds to it).
3. **Projection = weave-ingest, verbatim.** Once the journal exists, follow the
   `weave-ingest` skill's discipline for the projection: pure emergent, lookup-before-mint
   (predicates AND entities), faithfulness-not-truth, contradictions preserved. Apply the
   **sharpened tension test**: `:k/tension` only for a genuine TWO-sided contradiction in
   the source; single-sided/critical/provenance caveats are `:k/caution`.
4. **No engine changes, no network in the engine.** `gh` runs in your session; the engine
   only does `init`/`check`/views.

## Flow
1. **Parse the ticket ref.** Accept a URL (`.../issues/N` or `.../pull/N`),
   `OWNER/REPO#N`, or a bare `#N` (repo from `--repo` or the cwd git remote). Derive
   `owner`, `repo`, `number`, and whether it's an issue or PR.
2. **Derive the space path.** `<base>/<owner>-<repo>-<number>/`, where `<base>` is the
   user-given base dir or the default `./weave-tickets`. Example:
   `./weave-tickets/kangsy-weave-kb-42/`.
3. **Fetch via `gh`.** Auto-detect issue vs PR:
   - `gh issue view <N> --repo <owner>/<repo> --json title,body,author,state,labels,assignees,milestone,createdAt,url,comments`
   - if that fails, `gh pr view <N> --repo <owner>/<repo> --json title,body,author,state,labels,assignees,milestone,createdAt,url,comments`
   Stop and report the `gh` error if neither resolves (auth / not found / no access).
4. **Write the journal** to `<space>/journal/ticket.md` (create the space dir first if you
   are about to `init`). Shape:
   ```
   # #<N> <title>

   - <createdAt>: opened by <author> · state=<state> · labels=[…] · assignees=[…] · <url>

   <body>

   ## Thread
   - <comment.createdAt> — <comment.author>: <comment.body>
   - …
   ```
5. **Scaffold the space if new.** If `<space>/space.edn` is absent: `weave init <space>`
   (seeds the register). If present (a re-throw), reuse it and re-project.
6. **Project.** Follow `weave-ingest` over `journal/ticket.md`, writing
   `<space>/projection/{register.edn,facts.edn}`. Entities emerge from the ticket: the
   issue/PR itself, people (author/assignees/commenters), referenced components, labels and
   state as facts, decisions/blockers stated in the thread. Bind `:k/source` to
   `journal/ticket.md`.
7. **Check.** `weave --space <space> check` MUST be green. Fix findings before reporting
   success (a red check is a broken extraction, operational — not "done").
8. **Report + offer insight.** Print `#facts`, `#entities`, `#tensions`, register version,
   and the space path. Then ASK: "Run weave-insight on this ticket's space now?" — only run
   `weave-insight` if the user says yes.

## Re-throw
Re-running the same ticket resolves to the same derived path, re-fetches, overwrites
`journal/ticket.md`, and re-projects. lookup-before-mint keeps ids stable within the space.
