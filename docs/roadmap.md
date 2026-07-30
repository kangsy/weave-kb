# weave — roadmap

Forward-looking threads. Nothing here is a commitment.

## Near-duplicate consolidation (shipped)
Views cluster near-identical values of the same (subject, predicate) into one
representative + `(+N near-identical)`; `--verbatim` shows the raw list. A §3.4 view-time
lens — the fact base is never mutated. Similarity is deterministic trigram-Jaccard.

## Assertion reification + assertion-level lookup-before-mint (future)
Today faithful prose assertions are inline string-valued triples with no identity, so the
same assertion re-stated across sources duplicates. Reify a prose assertion as a
first-class node (as inferred insights `:i-*` already are) with a canonical statement, a
subject link, and its own multi-valued `:k/source`; extend the ingest discipline so that
before minting a tension the projector looks up existing assertion nodes for that subject
and adds a `:k/source` instead of a duplicate. "Flagged in 4 docs" becomes one node with
four sources — clean, provenance-preserving, and the source-count becomes a first-class
salience signal. Needs the retrieval/embedding layer that entity lookup-before-mint also
wants at scale. Composes with the view-time lens above.
