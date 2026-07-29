---
name: weave-ingest
description: Use when the user wants to ingest/project a data source (folder of docs, a repo, pasted text) INTO a weave space as faithful facts. Turns prose into an EAV fact base via weave's projection discipline.
---

# weave-ingest — data source → faithful projection

You project a data source into a weave space (`journal/` = source prose,
`projection/register.edn` + `facts.edn` = derived facts).

## Iron rules
1. **Fidelity, not truth.** A fact `[subject :k/pred value]` means *"the source
   claims this"* — NOT that it is true. Extract faithfully; invent nothing.
2. **lookup-before-mint (predicates AND entities).** Before every new predicate/every new
   entity: check `register.edn` or the existing `facts.edn` and reuse an existing
   handle. Only if nothing fits: mint anew. Entity = **kebab-case keyword** with
   `:k/label`; the *kind* is marked by the namespace — reserved are `:action/`, `:ctx/`,
   `:artifact/`, everything else (bare `:x` or a custom namespace `:doc/x`) is a topic.
   Predicate = into the register with a one-sentence definition; bump `:version` **once per ingest pass**
   (not per predicate).
3. **Preserve contradictions, do not resolve them.** Where the source contradicts itself or
   flags a tension, hold both sides; mark it as `[:e :k/tension "…"]`. The schema
   is binary — attach the tension to *one* representative "owner" entity (which the
   `tensions` view then shows as the title).
4. **Source tags are content, not trust.** Confidence/status markers from the source
   (`[verified]`, `[assumption]`, `[conflict]` …) go into `:k/caution`/the fact text,
   NEVER into weave's own trust metadata.
5. **Bind provenance.** Set `[:e :k/source "journal/<path>"]` per entity — a
   **real, existing** path (not free text; `check` enforces its existence).
   Multi-valued: a cross-document concept may carry several `:k/source` facts.
   These faithful ingest facts form the "projected" layer (everything except later
   `:i-*` insight entities).

## Flow
1. Resolve the space (`--space` / argument). `weave --space <dir> check` as a baseline.
2. Read the source. With many/large files: read in parallel (each reader returns PROPOSALS
   — entities, facts, new predicates), but **you** merge them sequentially into
   ONE coherent state (coherence is a merge discipline, not an extraction one).
3. Write `register.edn` + `facts.edn`.
4. `weave --space <dir> check` MUST be green. Fix findings first.
5. Report briefly: #facts, #entities, #predicates, #tensions.
