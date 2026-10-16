# Weave — Operating Protocol (Runbook for Sessions)

How a Claude session operates on a Weave **Space**. Assumes the concept from
[`whitepaper.md`](whitepaper.md). Assumption (deliberate, Phase 1):
**all journal changes happen through Claude sessions.**

## Prerequisites

- `babashka` (`bb`) and the `weave` CLI on the path (from the Weave engine repo).
- A space directory with `journal/`, `projection/` and `space.edn`.
- Resolve the space: **once per machine** run `weave register /path/to/space` (writes the
  machine-local config `~/.config/weave/config.edn`). After that `weave` finds the space
  from anywhere — no cwd dependency. Override: `--space <path>` or `$WEAVE_SPACE`.

## Commands

```
weave overview                       # context-aware: what can be done now
weave topics                         # topic-centric: what is being tracked (hierarchy)
weave journal [--brief]              # retrospective: latest journal entry per project
weave check                          # coherence against the register
weave divergence <dirA> <dirB>       # contrast two projections
```

## Flow of a session working on a tracked project

1. **Get the current state & take in context.** In the space, first `git pull` (fetch the
   state from the other machines). Then `weave overview` — shows what is actionable on
   *this* machine and what is waiting. Then do the actual work.

2. **Extend the journal (prose, append-only).** On completing substantial work, append an
   entry — natural language, dated:
   `- YYYY-MM-DD HH:MM: …` (local time; via `date '+%Y-%m-%d %H:%M'`). The timestamp
   makes `journal` intraday-precise.
   **Structure the entry readably** (as much structure as there are points, *no* rigid schema):
   the **first line** is the headline in *one* sentence — it also stands alone for
   `journal --brief`. If there are multiple points, put them below as **`-` bullets**
   (e.g. cause / fix / next step / blocker); paragraphs separated by **blank lines**.
   `journal` renders headline, bullets and paragraphs terminal-readably (inline Markdown
   is displayed as ANSI in a real terminal — `**bold**`, `` `code` ``, `[text](url)`;
   piped/`NO_COLOR`/`WEAVE_STYLE=0` leaves the Markdown raw, lines are wrapped) —
   but a single dense block stays a block. Flowing text is fine where there are no sub-points: structure follows the content, it
   does not force it.

   ```
   - 2026-07-30 09:00: Import bug fixed, version 1.2 released.
     - Cause: missing field validation
     - Fix: added validation, built release
     - Next step: wait for approval (blocked)
   ```

   The journal is an opaque, machine-managed
   event log; the directory layout is a **rebuildable view**, not an identity.
   **Derive entities from the prose** (not the human): which projects/subjects does the
   entry concern? Hierarchy ("a **board** in project A and an identically named **board**
   in project B" — same name, two entities) is the detachable fact `:k/part-of` —
   never encoded in the ID or path name.
   **First-touch = not a special case:** even a *new* project (even a new top-level
   domain) is taken on autonomously at the first substantial progress — **without
   asking, without a separate note**. A project need not be "registered" in advance;
   tracking emerges from the prose. Only when something is deliberately *not* to be tracked
   is that the explicit (rare) exception.

3. **Re-project (derive facts from the journal).** Update `projection/facts.edn`.
   Mandatory:
   - **lookup-before-mint (predicates AND entities)**: before assigning a predicate
     *or* creating an entity, first check `projection/register.edn` or the existing
     fact base and reuse an existing `:id`; only mint anew if nothing fits. IDs are
     **opaque and hierarchy-free**; the readable name lives as a
     `:k/label` fact. This prevents the same "component" from splitting into two entities —
     it is **duplicate protection, not an argument against creating new projects.**
     If you need a predicate the space does **not** yet know, the same rule applies:
     first search for an **existing equivalent** and reuse it; only if none exists,
     mint it into the register (bump the version). A fact with a register-foreign
     predicate turns `check` red — that is exactly the signal to mint *or* to reconsider.
   - **Kind via namespace, hierarchy via fact.** Reserved namespaces mark the
     *kind*: `:action/…`, `:ctx/…`, `:artifact/…`. A project/topic entity is any
     other keyword ID — bare (`:topic`) or with any non-reserved
     namespace; the namespace **never** encodes parenthood. **Declare umbrella entities
     explicitly** (at least one `:k/label` fact): a `:k/part-of` target that appears
     only as an object is not a known entity and `check` reports it (dangling part-of).
   - **Legacy IDs are harmless.** Existing hierarchy-encoding IDs (`:proj/legacy-child`)
     stay untouched: the ID is an opaque handle, its prefix is never
     read as hierarchy. Never rename IDs; a real re-ID would be a `merge` (`:k/merged-into`).
   - **Prose binding follows the space convention, not the runbook.** *How* the projection
     binds its prose/provenance is **emergent in the space register** — not a predicate name
     prescribed by the runbook. This space uses `:projected-from` for it; **reuse** it via
     `lookup-before-mint`, do not mint a competing predicate.
     (The engine seed *offers* a per-entity `:k/source` — merely a suggestion for
     *new* spaces, never imposed on existing conventions.) Visibility depends on
     no binding (foundations §3.3c); unbound journal files stay visible.
   - **Log evolution.** When a project is restructured into an umbrella or vice versa,
     record the operation as genealogy: `re-parent` (change the `:k/part-of` fact, ID
     stays), `split` (new entities + `:k/split-of` on the ancestor; the ancestor stays
     immortally queryable), `merge` (`:k/merged-into`). "Promote to umbrella" is either
     re-parent-with-child *or* split — the prose decides.
   - **Make a structure judgment (on substantial change to a project's fact situation).**
     Step back and *assess*: does the current structure still fit the accumulated
     facts — **keep or change?** This is a **real judgment, not a rule** (no
     "≥N actions → umbrella"). Guardrails, not automatics: tickets/work items stay
     **actions** (transient, removed when done); **subprojects are permanent
     surfaces**; a project becomes an **umbrella** when its threads cluster into such surfaces.
     Record the verdict: the **rationale into the journal** (prose, history), the short
     result as `:k/structure-review` ("`<date>` · `kept|changed` · `<brief>`").
     **"Keep" is just as valuable as "change"** — it makes the structure an
     *assessed* one, not merely an accumulated one. `topics` shows the verdict; an old review
     date nudges next time.
   - **Keep stable IDs.** Labels may change, IDs never.
   - Contexts (`:ctx/...`) are likewise defined in the register; the ambient mapping is
     in `space.edn` (`:ambient`).
   - Set `:k/last-touched` of the touched entity to today's date.
   - **State digest (optional, recommended for multi-thread projects).** For each touched
     entity, maintain a short `:k/digest` sentence ("where it stands" — current state, no
     history). This is the prose that `weave recap` shows; if it is missing, recap falls
     back mechanically to `:k/blocked-by`/`:k/next-action` (as a list when there are many actions).

4. **Check.** `weave check` — must return `✓` (exit 0). Fix findings before the commit.
   Hints (`ℹ`, e.g. stale entities) do not block: they only go away through real work.

5. **Commit and push immediately.** Journal + projection together, a meaningful message,
   then `git push`. **An unpushed commit is not a sync** — never leave it unpushed.

## Optional: Replay + divergence (epistemic view)

Write a **fresh** projection into a snapshot folder and contrast:

```
weave divergence projection snapshots/<date>
```

Cosmetic divergence (same ID, different label) is noise; semantic divergence (new
concept, refined/surfaced facts) is signal.

## Rules (from the foundations)

- **Reproducibility is not a goal** — fidelity + coherence are. Divergence is fine.
- **No top-down taxonomy**, no manual tags/status by the user.
- **The register is versioned** and part of the fact set; on refinement maintain
  `:supersedes`/`:refines` on the concept.
- **FCA** only optional/offline as a split/merge suggestion — no runtime lattice.

## Watch points

- Nondeterminism/convergence of the re-projection (foundations §9.1) — observe it.
- With a large register: avoid full replay, re-project incrementally.
