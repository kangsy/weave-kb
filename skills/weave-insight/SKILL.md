---
name: weave-insight
description: Use when the user wants weave to GENERATE insights/tensions across a projected fact base — connections no single document states. Full-sweep generation + adversarial grounding.
---

# weave-insight — full-sweep inference with adversarial grounding

You generate `:inferred-across` insights ACROSS the fact base (connections that appear in
NO document) and ground each one adversarially. Value = grounded recombination, not
truth; NEVER present as truth.

## Flow (two separate roles)
1. **Generator (optimistic).** Sweep the ENTIRE `facts.edn` — but read only the
   **projected layer** (all ingest facts), NEVER earlier insight entities
   (`:i-*` with `:k/inferred-across`/`:k/rejected-inference`). This is the quarantine against
   self-bootstrap: speculation must not become its own evidence. Propose candidates:
   tensions, bridges (connects distant regions), gaps (A→B, B→C, but never A→C),
   latent clusters, same-as/merge candidates.
2. **Verifier (skeptical, INDEPENDENT of the generator).** A separate sub-agent with the
   mandate to REFUTE, default = reject (a *bundled* agent that checks all candidates
   *independently* satisfies "separate" — not necessarily one per candidate). Per
   candidate: can every premise be shown on a concrete fact, WITHOUT importing external
   knowledge? Only then keep it. `:k/grounded-in` points to **entity handles**
   (not a triple type); name the concrete triples in the `:k/confidence` text. Assign confidence
   (`high` = pure recombination / `plausible` = one mild added premise).
3. **Persist (everything, including rejects).** Reify each insight as an `:i-*` entity:
   - Survives: `[:i-x :k/label "…"] [:i-x :k/inferred-across "<claim>"] [:i-x :k/grounded-in :a] … [:i-x :k/confidence "<level> — <rationale>"]`
   - Rejected: `[:i-x :k/label "Rejected: …"] [:i-x :k/rejected-inference "<claim>"] [:i-x :k/grounded-in …] [:i-x :k/confidence "REJECTED: <reason>"]`
4. `weave --space <dir> check` MUST be green.
5. Report: #survived (with confidence distribution), #rejected (with reasons).

## Why this way
- Separate roles prevent self-approval. Groundedness ≠ truth — the verifier
  checks ONLY traceability. Rejects are judgment artifacts (keep them). Two runs may
  diverge; that is signal, not a defect.
