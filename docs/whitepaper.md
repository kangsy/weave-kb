# Derived, Not Imposed
### Self-Structuring Knowledge for the LLM Era

*A whitepaper. Version 0.2 — 2026-07-28.*

---

## Abstract

Written knowledge tends toward one of two fates. Left as prose, it is cheap to record and
nearly impossible to see across: the relationships between ideas stay locked inside
sentences, available only to whoever reads every document. Given explicit structure — a
classification, a thesaurus, a knowledge graph — it becomes navigable and queryable, but
only after a person has *built* that structure by hand, usually before the domain is well
understood, and only as long as someone keeps it aligned with a changing world. This is
the central trade-off of knowledge management, and it has held for a century: expressive
structure has always cost human authoring labor.

Large language models change what is possible on this trade-off. A model can read prose
and *derive* structure from it — the very labor that was, until now, unavoidably human.
This paper describes a knowledge-management architecture built on that shift. The human
writes natural prose; the machine derives and maintains the structure. Three layers carry
it: an append-only prose **journal** (the source of truth), a machine-readable
**projection** of facts derived from the journal (a regenerable view), and a versioned
**register** of concepts that grows from the bottom up rather than being fixed in advance.
Structure is *derived, not imposed* — and, because it is re-derivable, it can sharpen over
time instead of ossifying.

We ground the design in information science, event sourcing, terminology science, and
formal concept analysis, and we are candid about what remains unproven. **weave** is
presented as a working reference implementation of these ideas — not as their definition.

---

## 1. The cost of structure

To make a body of knowledge navigable — to move through it by relationship rather than by
keyword, to ask it questions, to see what connects to what — someone or something must
give it structure. The history of knowledge organization is, in large part, the history of
who paid for that structure and how much it cost.

### 1.1 A century of increasingly capable schemes

Classification began monohierarchical. Dewey's decimal system, and library classification
after it, gave every work exactly one place in one tree. The limitation was recognized
early: real subjects are *multidimensional*. A book on the economic history of Indian
textiles belongs at once under economics, history, India, and textiles, and a single tree
forces a choice among them. S. R. Ranganathan's answer, **faceted classification** (the
Colon Classification, 1933), decomposed subjects into orthogonal facets — his canonical
Personality, Matter, Energy, Space, Time — so that a subject could be *composed* from
several independent dimensions rather than filed under one. Faceted analysis remains the
state of the art in information organization for exactly this reason.

Controlled vocabularies and thesauri (broader/narrower/related terms, ISO 25964) added
managed synonymy and cross-references. The Semantic Web stack — RDF, RDFS, OWL — made
structure machine-readable and, crucially, allowed *many overlapping statements about the
same resource* rather than one classification. **Topic Maps** (ISO 13250) went furthest as
a model of a knowledge layer: typed topics standing in for subjects, **n-ary
role-based associations** between them, occurrences linking topics to information
resources, and scopes qualifying the validity of any statement.

The trajectory matters because it forecloses a lazy critique. The problem with prior
knowledge organization was *not* that it forced everything into a single rigid hierarchy.
The field diagnosed that limitation eighty years ago and moved past it. Facets, RDF, and
Topic Maps all let the same entity carry many structures at once.

### 1.2 The residue that never left

What every one of these schemes shares is the cost that motivates this paper: **a human
must author the structure, and keep it aligned.** Someone designs the facets, curates the
thesaurus, asserts the triples, draws the topic map — and someone maintains all of it as
the domain shifts under them. Three well-studied problems make that labor expensive:

- **The vocabulary problem.** Furnas, Landauer, Gomez & Dumais (1987) found that two
  people spontaneously favor the same term for a given object with probability less than
  0.20 — under one time in five. Any single controlled vocabulary therefore mismatches
  most of the people who will later search it, and a single designer-chosen keyword fails
  the majority of new users — an authoring cost that no amount of care fully removes.
- **Premature commitment.** A scheme must be designed *before* the domain is well
  understood, precisely when the designer knows least. The scheme then ossifies:
  restructuring it is the expensive, well-documented problem of ontology and schema
  evolution (Noy & Klein, 2004; Zablith et al., 2015).
- **Standing maintenance.** Structure decays as the world changes and as the person who
  built it moves on. The map drifts from the territory.

These costs explain the actual fate of most knowledge organization efforts. Where
authoring was strongly incentivized, structured knowledge succeeded — schema.org (rewarded
by search engines), Wikidata, and the large biomedical ontologies are real, thriving
knowledge structures. Where the payoff was diffuse or deferred, the structure was never
built: the universal "web of data" stalled, general-purpose Topic Maps saw only niche
uptake, and the overwhelming majority of personal and organizational knowledge lives, to
this day, as unstructured prose in documents, wikis, and chat logs — searchable by
keyword, but with its relationships invisible to any tool.

So the trade-off is not prose *versus* structure as a matter of taste. It is that structure
has always demanded a human to build and maintain it, and for most knowledge that price is
simply too high to pay. People default to prose not because they prefer chaos, but because
the alternative costs more than it returns.

---

## 2. Derived, not imposed

A language model can read prose and produce structure from it. That single capability moves
the trade-off of §1, because the authoring labor that was always human can now be done by
the machine.

> **The human writes naturally. The machine derives and maintains the structure.**

This is not a plea to abolish structure — structure is what makes knowledge navigable, and
we want more of it, not less. It is a change in *who builds it and when*. In the old
arrangement, a person paid the structuring cost up front, in the currency of classification,
before understanding the domain. In the new arrangement, the person pays only the cost of
writing clearly, and the machine pays the structuring cost continuously, *at read time*,
where it has the most context — and can revise as understanding deepens rather than freezing
a scheme in place.

Two clarifications guard against misreading.

**The target is not a single hierarchy.** As §1 established, the sophisticated schemes
already permit many structures over one entity. The novelty here is orthogonal to the shape
of the structure: it is that the structure is *derived from the prose by a machine and kept
current by re-derivation*, rather than authored by a person in advance. A derived structure
can be faceted, associative, or graph-shaped; what has changed is its provenance and its
mutability.

**Description over classification.** Where the approach does take a stance on shape, it
prefers *description by relation* to *classification into a scheme*. Compare two ways of
adding structure to a document:

> *Classification:* "Memo-2024-03 **is a** StrategyDocument." — Under which scheme? Decided
> when, by whom? A commitment to a category system that must already exist.
>
> *Description:* "Memo-2024-03 **supersedes** the Q2 roadmap and **is cited by** the board
> deck." — Specific, true statements about specific entities, asserted when known, needing
> no prior scheme.

Both are structure. The second states what is actually true and useful for navigation
without first committing to a classification. In this architecture, classification is
allowed to *emerge* as one more derived, revisable fact (§3.3, §4.1) — never as a
precondition for recording a thought.

The remainder of the paper makes this concrete (§3), grounds it (§4), shows it running as
a reference implementation (§5), and states plainly what has and has not been demonstrated
(§6).

---

## 3. Architecture

Three layers.

```
┌─────────────────────────────────────────────────────────────┐
│ 1. JOURNAL — truth about content                            │
│    append-only prose, dated, written by human or AI         │
│    → an event log                                           │
├─────────────────────────────────────────────────────────────┤
│ 2. PROJECTION — truth about structure                       │
│    machine-readable facts: triples (entity, concept-id,     │
│    value), derived by the model from the journal,           │
│    regenerable by replay                                    │
│    ── contains, as a special role, the versioned REGISTER ──│
├─────────────────────────────────────────────────────────────┤
│ 3. VIEWS over the projection                                │
│    view · neighbors · find · tensions · insights ·          │
│    hubs · check · divergence                                │
└─────────────────────────────────────────────────────────────┘
```

### 3.1 Journal — the source of truth (event sourcing)

The journal is append-only natural language. The human enters no structure. This is a
classical **event-sourcing / CQRS** pattern: the journal is the immutable event log, and
the fact base is a *materialized view* derived from it. Because the truth is the prose and
the structure is derived, the structure can be **rebuilt at any time** by replaying the
journal — the property that lets structure sharpen instead of ossifying.

### 3.2 Projection — derived facts

Facts are triples — `(entity, predicate/concept-id, value)` — deliberately **without a
fixed predicate taxonomy** (no OWL, no description logic). When the concept vocabulary
improves, the projection is **re-derived** (replayed): the facts are extracted afresh from
the journal under the current, refined register. The projection is a view, not a second
source of truth; it is regenerable and therefore disposable. This is the level at which the
"description over classification" preference of §2 lives: a triple *is* a described
relation, atomic and addressable, asserted without a governing scheme.

### 3.3 The register — concept, term, definition

Predicates and concepts are **not their labels**. Each is an entry in a register with:

- a **stable, opaque ID** (never reused, never broken) — the identity;
- a **mutable label / gloss** — a mirror of the current definition, renamable at any time
  without breaking a single reference;
- a **definition** and examples;
- **`refines` / `replaces` links** — the genealogy of its evolution.

This is exactly the **concept–term–definition** separation of terminology science
(Wüster; ISO 704/1087) and the **semiotic triangle** (Ogden & Richards, 1923). Its
practical payoff is a discipline we call **lookup-before-mint**: before assigning a
predicate, the model searches the register and *reuses* an existing handle rather than
minting a duplicate. Symbol and meaning are decoupled, and vocabulary drift is prevented
*by construction* rather than by convention. This is also the architecture's answer to the
vocabulary problem (§1.2): the register is a growing, machine-maintained reconciliation of
the many terms that refer to one concept — the curation labor a thesaurus demanded of a
person, done continuously by the machine.

The same discipline applies to **entities**: stable opaque ID, mutable label,
lookup-before-mint (so the same thing does not fracture into two IDs). Crucially,
**hierarchy is a detachable fact** (`part-of`), derived from prose — never baked into the
ID or a storage path. Restructuring is therefore a fact edit, not an identity break, drawn
from a small operator set: *mint · relabel · re-parent · split · merge*. Genealogy links
(`split-of`, `merged-into`) keep ancestors immortal: a split ancestor is not deleted but
*superseded* — filtered out of current views, still queryable for history.

### 3.4 Truth versus view — the fact base never encodes "hidden"

A strict separation: the projection holds **structural truth** (relations like `part-of`,
`cites`, `contradicts`, `source`). **No fact means "hidden."** *Visibility* is always a
**view-time computation** over truth plus context. Views are **default-inclusive**, they
mark relevance rather than delete, and every narrowing is a **named, reversible lens** —
never a silent side effect. "Superseded," for instance, is a default-collapsed lens
(revealed with `--all`), not a deletion. The machine maintains the structure; it does not
unilaterally decide what the human is *not* allowed to see.

---

## 4. Foundations

### 4.1 Meaning without a fixed scheme

The stable-ID / mutable-label split (§3.3) is not an implementation convenience; it is the
mechanism by which the system can have *meaning* without a pre-committed *classification
scheme*. Terminology science and the semiotic triangle both insist that a concept, the term
that names it, and its definition are three distinct things. Conflate concept with term — as
a tag system does, where the string *is* the category — and every rename is a schema
migration and every synonym a fork. Keep them separate, anchor identity to an opaque ID,
and the label becomes free to change as understanding sharpens while the register of
concepts grows *bottom-up* from what the prose actually says, rather than being declared
top-down before the prose exists.

### 4.2 Fidelity over reproducibility

A deliberate, load-bearing decision: **reproducibility is not a goal.** The model is not
deterministic; two projections of the same journal may differ, and that is acceptable —
as several faithful translations of one text are all valid. The right standard is
**admissibility / fidelity**: every projection must be a faithful, internally coherent
reading of the journal. Multiple admissible readings may coexist.

**Correctness and coherence** are the real requirements. When a projection violates them,
that is an *operational* failure (a broken extraction), not a conceptual one — and it is
**caught, not assumed**: a `check` layer verifies invariants (unknown predicates, dangling
references) and flags staleness as a hint. Coherence is verified, not hoped for. This directly answers the
best-documented failure modes of LLM-driven knowledge-graph construction — schema drift,
non-determinism, and hallucinated predicates (2025 survey, arXiv:2510.20345).

### 4.3 Divergence as an epistemic fact

Because the register is versioned and is *itself part of the fact set*, the system is
reflexive — related to RDF (self-describing), Datomic (schema as queryable data), and
Lisp homoiconicity. Two consequences follow.

First, **self-reference damps divergence.** On replay, the versioned register is present
in context as facts, so `lookup-before-mint` becomes a **feedback loop**: the output of
past projections constrains future ones, biasing toward a fixed point. It damps; it does
not eliminate. Meta-drift stays possible — but versioning makes it *observable and
reversible*.

Second, **a divergence is itself an interesting fact.** Contrast two projections of one
journal and the differences are signal:

- a new concept *split* on re-projection → the journal held a distinction the old register
  was blind to — the engine of refinement;
- two projections disagreeing on a value → an ambiguity or tension *in the journal
  itself* — a pointer back to the source.

The ID/label split makes divergence **classifiable**: same concept-ID with a different
label is *cosmetic* (model jitter, ignore); a newly minted ID or a flipped assertion is
*semantic* (signal, surface). That divergence is visible only through contrast is the same
principle as Saussure's differential value and FCA's incidence contrast.

### 4.4 Formal concept analysis as an optional substrate

Formal Concept Analysis (Ganter & Wille) derives concept hierarchies mathematically and
data-drivenly from object×attribute data, with a track record in ontology learning
(Cimiano, Hotho & Staab, 2005) and class-model reengineering (Snelting & Tip; Godin et
al.). The division of labor is clean:

> **FCA supplies the lattice structure. The model supplies the label, the definition, and
> the relevance judgment.**

FCA lattices carry no world knowledge — they cannot judge whether a derived concept is
meaningful or name it (Guenoune et al., 2025). That is precisely the gap a language model
fills, as *explainer* of FCA output rather than *extractor* feeding it (Gutierrez et al.,
2025). We therefore treat FCA as an **offline suggestion layer** for concept split/merge,
not a mandatory runtime lattice — mindful of its scaling limits (concept count is
exponential; counting is #P-complete, Kuznetsov 2001) and the known mitigations (iceberg
lattices, incremental algorithms, FCA2VEC embeddings).

---

## 5. Reference implementation: weave

The concepts above stand on their own. **weave** is one implementation of them — a working
prototype, deliberately small, that lets us test the claims against reality rather than
assert them.

### 5.1 Engine and space

weave separates the **engine + method** (domain-agnostic, carries no user data) from a
**space** (a directory holding one corpus's journal, projection, and a `space.edn` config).
One engine, many spaces. The engine is stateless and location-independent: it resolves the
target space explicitly (`--space` → `$WEAVE_SPACE` → a machine default → the nearest
`space.edn`). A space is an opaque store — a directory today, a database behind the same
load functions tomorrow.

### 5.2 Ingesting a corpus

The engine is deterministic; the *intelligence* is a language model driving it through two
shipped skills:

- **ingest** — project a source (a folder of documents, a repository, pasted text) into
  faithful facts under lookup-before-mint. Contradictions in the source are *preserved*,
  not resolved; source tags ("verified", "assumption") are kept as *content*, not as system
  trust.
- **insight** — a generator proposes connections no single document states; a *separate
  skeptic* keeps only the ones grounded in the corpus. Survivors and reasoned rejects both
  persist, so the record shows not just what was inferred but how the model judged.

### 5.3 Exploring the derived structure

A read-only view surface makes the derived structure navigable over any space:

- `view <entity>` — everything about an entity, with backlinks;
- `neighbors <entity> [--depth N]` — the local graph around an entity;
- `find <text>` — search over labels and values;
- `tensions` — preserved contradictions; `insights` — inferred connections (survivors and rejects);
- `hubs` — entities ranked by connectivity (centrality);
- `predicates` — the register vocabulary and its usage counts;
- `check` — coherence and invariants; `divergence <A> <B>` — contrast two projections of one journal.

This is, in effect, the knowledge layer of a Topic Map — typed subjects, associations
between them, links back to source occurrences — with the decisive difference that the layer
Topic Maps needed a person to author is here derived and maintained by the machine.

### 5.4 Self-demonstration

Because weave is domain-agnostic, it can be pointed at *this very whitepaper* — a public
corpus, projected in an isolated space. The paper about weave, read by weave: the ingest
produces a fact base that `check` reports coherent, and the `hubs` view ranks
*lookup-before-mint*, *Weave*, and the *register* as the most connected concepts — the
argument's own centre of gravity, recovered from prose.

Two views show the substrate doing what §4 promised. `tensions` surfaces the unresolved
contradictions the text itself carries — for instance that the classification burden is
*not removed but only displaced* into predicate choice, or that self-reference is said to
damp divergence while convergence is admitted to be unproven. The tool does not assert
these; it extracts them faithfully from the prose and attaches each to the concept it
concerns.

`insights` shows the *inferred* layer (§5.2) — connections stated in no single passage,
each generated and then adversarially grounded, kept with a confidence gradient:

```
Insights — survived (3):
● lookup-before-mint carries a double load: anti-drift damper AND displaced-classification residue
    conf: high — [lookup-before-mint addresses convergence] ∩ the classification-burden
          tension ('displaced … reappears at scale as lookup-before-mint over thousands')
● Topic Maps' fatal weakness is precisely weave's value proposition
    conf: high — [topic-maps: 'historically hand-built'] + [weave: 'the layer TM needed a
          human to author is here derived by the machine']
● The stable-ID split is the hinge for BOTH divergence and convergence   (conf: plausible)
— rejected, kept as judgment artifacts (2):
● Rejected: FCA could replace the coherence check
    REJECTED — no fact links FCA (offline split/merge suggestion) to check's invariant layer
```

The survivors are genuine cross-section syntheses; the rejects are retained, with reasons,
as a record of how the model judged. Nothing here is offered as truth — only as a grounded
reading of the paper's own corpus.

---

## 6. Evaluation and honest limits

### 6.1 What the prototype shows

Two capabilities hold at the substrate level, independent of any corpus:

- **The coherence guard bites.** Against a clean fact base, `check` reports coherent.
  Against a deliberately drifted fixture it returned six findings — a dangling `part-of`
  target, two `part-of` cycles, a genealogy link into the void, a superseded entity still
  carrying an open action, and a fact citing a non-existent journal source — the operational
  detector for the "broken extraction" of §4.2.
- **The ID/label filter works.** `divergence` contrasted two projections of one journal and
  automatically separated cosmetic label drift (same ID → filtered as noise) from semantic
  change (a newly minted concept; a relation refined from a loose string to a resolved
  entity; a fact dropped in one projection and present in the other). Both projections were
  faithful; one was strictly more precise — *more expressive and more precise*, measurably.
  Without the stable-ID/label split, cosmetic and semantic change would be
  indistinguishable.

The full pipeline was then run on a public, reproducible corpus: weave projected onto *this
very whitepaper* (§5.4). Three things held.

1. **Faithful projection is coherent and traceable.** The prose produced a fact base that
   `check` passes, every fact bound to its source passage, and the `hubs` view recovered
   the argument's own centre of gravity (*lookup-before-mint*, *Weave*, the *register*) from
   running text. No model-prior noise crept in.
2. **Insight is grounded recombination, not divination.** Each surviving inference joins
   facts that no single passage places together — that `lookup-before-mint` is at once the
   anti-drift damper and the residue of the displaced classification burden, or that Topic
   Maps' fatal weakness is exactly weave's value proposition — while an adversarial verifier,
   told to refute by default, discarded the two candidates that leaned on world knowledge or
   an unstated link. The output is a confidence gradient (*high → plausible → rejected*),
   surfaced rather than flattened.
3. **The judgment is itself an artifact.** Rejected inferences are kept, with their reasons,
   as visible knowledge about how the model judged — not silently discarded.

An earlier field test against a private ~25-document corpus not written to be projected
reached the same conclusions; we note it for breadth but do not rely on it here.

### 6.2 Honest limits

We are early, and we decline to overclaim.

- **Convergence is not proven.** Whether repeated re-projection reaches a fixed point or
  oscillates is not formally guaranteed. The bias toward convergence (stable-ID anchoring,
  split-before-relabel, versioning) is designed in, not demonstrated.
- **"More precise *and* more expressive" is shown once, not measured at scale.** The
  vocabulary could instead drift or fragment into concept sprawl. We do not yet have the
  longitudinal metric.
- **The second projection in the divergence demo was hand-built**, not blindly re-derived;
  the true non-determinism test is still outstanding.
- **Scale bites at entity identity.** `lookup-before-mint` over thousands of entities will
  need a retrieval layer (embedding search over the register); predicates likely stay few
  and context-portable, but entities are the scaling case.
- **The inferred-insight layer has no truth criterion**, by design — its standard is
  *groundedness*, not truth. The open guardrail is self-bootstrap: inferred facts must
  remain readable but quarantined from feeding back as their own evidence.

None of these is a conceptual flaw in "derived, not imposed"; each is an operational
frontier.

---

## 7. Related work

weave's contribution is the *combination* of several established lines, not any single
piece.

- **Information organization.** Faceted classification (Ranganathan, 1933) and its modern
  treatments (Broughton; Svenonius, 2000) establish that expressive knowledge structure is
  multidimensional and, until now, hand-built. The vocabulary problem (Furnas et al., 1987)
  quantifies why any single authored vocabulary underperforms. Vannevar Bush's Memex (1945)
  anticipated associative structure over indexed hierarchy.
- **Ontology evolution** (Noy & Klein, 2004; Flouris et al., 2006/2008; Zablith et al.,
  2015) distinguishes evolving the schema from migrating the instances. Event sourcing with
  replay dissolves instance migration: there are no instances to migrate, only a journal to
  re-project. No prior work connects ontology evolution *directly* to LLM re-extraction; we
  rely on a "fact-centered evolution" analogy and are explicit that it is an analogy.
- **Formal Concept Analysis** (Ganter & Wille; Cimiano, Hotho & Staab, 2005) — the optional
  lattice substrate of §4.4, now paired with LLMs as explainers (Gutierrez et al., 2025).
- **Meaning and representation** — the distributional hypothesis (Firth; Harris), meaning-
  as-use (Wittgenstein), differential value (Saussure), and terminology science (Wüster; ISO
  704/1087) with the semiotic triangle (Ogden & Richards, 1923).
- **Emergent semantics and self-building knowledge bases** (Aberer et al.; *The Knowledge
  Base That Builds Itself*), and the failure modes catalogued for LLM knowledge-graph
  construction — schema drift, non-determinism, hallucinated predicates (2025 survey,
  arXiv:2510.20345).

A full, cited reference list with caveats is in the Appendix.

---

## 8. Conclusion

For a century, making knowledge navigable meant a person building its structure by hand —
choosing the categories, curating the vocabulary, asserting the relationships — in advance
of understanding and forever after. The field grew steadily better at *what* structure to
build, from single hierarchies to facets to associative topic maps. What it could never
remove was *who* had to build it. That labor is why most knowledge is never structured at
all.

Language models remove the constraint that made the labor human. The shift is small to
state and large in consequence: **let the human write, and let the machine derive and
maintain the structure.** Keep the prose as the single source of truth. Derive the facts,
and regenerate them freely as understanding sharpens. Give concepts stable identities and
mutable names, so meaning can refine without breaking references. Prefer description to
classification, and let classification emerge as a revisable fact rather than a
precondition. Treat divergence not as error but as a pointer to where the source itself is
ambiguous or newly distinguished. Verify coherence rather than assuming it.

weave demonstrates that this is buildable today, and — just as usefully — shows exactly
where it is not yet proven. The concepts do not depend on weave; weave depends on the
concepts.

---

## Appendix: scientific anchors

> Verified during research. Honest caveats are listed at the end of this appendix.

**Information organization & classification**
- Ranganathan, S. R. (1933), *Colon Classification* — faceted classification.
- Broughton, V. (2006), *The need for a faceted classification as the basis of all methods of information retrieval*, Aslib Proceedings 58(1/2).
- Svenonius, E. (2000), *The Intellectual Foundation of Information Organization*, MIT Press.
- Furnas, Landauer, Gomez & Dumais (1987), *The vocabulary problem in human-system communication*, CACM 30(11):964–971.
- Bush, V. (1945), *As We May Think*, The Atlantic — the Memex, associative trails.
- ISO 25964 (thesauri); ISO 13250 (Topic Maps); Pepper, S., *The TAO of Topic Maps*.

**Ontology evolution**
- Noy & Klein (2004), *Ontology Evolution: Not the Same as Schema Evolution*, KAIS 6(4):428–440.
- Flouris, Manakanatas, Kondylakis, Plexousakis & Antoniou (2008), *Ontology change: classification and survey*, Knowledge Engineering Review 23(2).
- Flouris, Plexousakis & Antoniou (2006), *Evolving Ontology Evolution*, SOFSEM 2006 — "modification-centered" vs. "fact-centered."
- Zablith et al. (2015), *Ontology evolution: a process-centric survey*, KER.
- Javed, Abgaz & Pahl (2009), OTM — four levels of intent-carrying change operators.
- da Silva et al. (2022), KER — ontology evolution incl. propagation to dependent artifacts.

**Formal concept analysis**
- Ganter & Wille, *Formal Concept Analysis: Mathematical Foundations*.
- Cimiano, Hotho & Staab (2005), *Learning Concept Hierarchies from Text Corpora using FCA*, JAIR 24.
- Cimiano & Völker (2005), *Text2Onto*, NLDB — incremental probabilistic ontology model.
- Kuznetsov (2001) — counting formal concepts is #P-complete.
- Stumme et al. (2002), *TITANIC / Iceberg concept lattices*, DKE 42(2).
- Nehmé, Valtchev, Rouane-Hacene & Godin (2005), ICFCA — incremental lattice maintenance.
- Rouane-Hacene et al. (2008) — Relational Concept Analysis (RCA).
- Snelting & Tip (1998/2000); Godin et al. (1993) — FCA-based class-hierarchy reengineering.
- Guenoune et al. (2025), *LLM-Assisted Relational Concept Analysis for Class Model Restructuring*, CONCEPTS 2025 (Cluj-Napoca), Springer LNAI, doi:10.1007/978-3-032-03364-2_7 — FCA/RCA lacks common-sense knowledge for naming/relevance.
- Gutierrez et al. (2025), *Empowering Relational Concept Analysis Using Large Language Model Knowledge Delivery*, CONCEPTS 2025 (Cluj-Napoca), Springer LNAI, doi:10.1007/978-3-032-03364-2_8 — LLM-as-explainer for RCA artifacts.
- Dürrschnabel, Hanika & Stubbemann (2021), *FCA2VEC*.

**Theory of meaning / representation**
- Firth (1957), distributional hypothesis; Harris (1954) — "company it keeps."
- Quine (semantic holism); Wittgenstein (meaning-as-use); Saussure (differential value).
- Wüster / ISO 704, ISO 1087 (terminology science); Ogden & Richards (1923), semiotic triangle.

**Emergence / LLM-KG / practice**
- Aberer et al., *Emergent Semantics*.
- *LLM-empowered Knowledge Graph Construction: A Survey* (2025), arXiv:2510.20345 — failure modes: schema drift, non-determinism, hallucinated predicates.
- *The Knowledge Base That Builds Itself* (vanja.io) — practitioner precedent for the three-layer architecture.

**Key caveats from the research**
- The characterization of Semantic Web / Topic Map adoption is our synthesis, not a single cited survey; adoption was uneven and multi-causal, and structured-knowledge successes (schema.org, Wikidata, biomedical ontologies) are real.
- No paper connects ontology evolution *directly* to LLM re-extraction / event sourcing; the support via "fact-centered evolution" is a defensible **analogy** (the source means AGM belief revision), not a direct citation.
- LLM+FCA exists in 2025 only as *LLM-explains-FCA*, not *LLM-feeds-FCA*.
