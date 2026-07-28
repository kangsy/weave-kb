# Self-demo space

weave projected onto **its own whitepaper**. This is the concrete, reproducible evidence
behind §5.4 and §6.1 of [`docs/whitepaper.md`](../../docs/whitepaper.md) — a public corpus
with no private data.

- `journal/` — a copy of weave's whitepaper (`whitepaper.md`), used here as the source prose.
- `projection/` — the derived fact base (`facts.edn`) and concept register
  (`register.edn`), produced by the `weave-ingest` and `weave-insight` skills.

## Reproduce the views

```
weave --space examples/self-demo check       # ✓ coherent
weave --space examples/self-demo tensions     # weave's own unresolved tensions, from its prose
weave --space examples/self-demo insights     # inferred connections: 3 survived, 2 rejected (kept)
weave --space examples/self-demo hubs          # Weave, register, projection = centre of gravity
weave --space examples/self-demo view :topic/register
```

The `:i-*` entities in `facts.edn` are the **inferred** layer (`:k/inferred-across` /
`:k/rejected-inference`) — generated across the map and adversarially grounded. They are
readings, never asserted as truth.
