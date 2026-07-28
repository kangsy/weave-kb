# Weave

A self-structuring knowledge substrate. You write natural prose; Weave derives and
maintains a living, refining structure you can query — no top-down taxonomy, no manual
tags or status.

Weave is the **engine + method**, domain-agnostic. It operates on a **space**: a
directory holding your prose journal, the derived projection (register + facts) and a
`space.edn` config. One engine, many spaces; the engine carries no user data.

## Concept

- **Journal** — append-only prose, the truth source (event sourcing).
- **Projection** — machine-readable fact triples `[entity concept-id value]`, derived
  by an LLM from the journal, regenerable (replay).
- **Register** — the concepts: stable id + mutable label + definition; versioned and
  itself part of the fact set. Grows bottom-up (`lookup-before-mint`).
- **Tools** — `weave overview | check | divergence`.

See [`docs/whitepaper.md`](docs/whitepaper.md) for the concepts and their scientific
grounding, and [`docs/operating.md`](docs/operating.md) for the session runbook.

## Usage

```
weave init <dir>                            # scaffold a fresh space (from the seed register)
weave register /path/to/your/space          # once per machine: set the default space
weave where                                 # show the resolved space
weave overview                              # context-aware "what can I do now"
weave topics                                # subject-centric: what's tracked (hierarchy)
weave journal [--brief]                     # backward-looking: last activity per project
weave check                                 # coherence / invariants
weave divergence <dirA> <dirB>              # contrast two projections

# KB exploration views (read-only, generic over any space):
weave view <entity>                         # everything about one entity + backlinks
weave neighbors <entity> [--depth N]        # local graph / cluster
weave find <text>                           # search labels + values
weave tensions                              # preserved contradictions
weave insights                              # AI-inferred insights (survivors + rejects)
weave hubs [--top N]                        # entities by connectivity (centrality)
weave predicates                            # register vocabulary + usage counts
```

Requires [babashka](https://babashka.org) (`bb`) on the PATH. Install with `./install.sh`
— it symlinks `weave` into `~/.local/bin` (override with `WEAVE_BIN`) **and** the skills
into `~/.claude/skills`. The symlinks mean `git pull` in this repo keeps both current.

## Quickstart: weave as a knowledge base

For Claude Code users. The engine is deterministic; the *intelligence* is your own Claude
Code, driven by two shipped skills.

```
weave init ~/my-kb                          # scaffold a fresh space
# in Claude Code: run the weave-ingest skill on a source (e.g. a docs/ folder)
weave --space ~/my-kb insights              # generated, grounded insights
weave --space ~/my-kb tensions              # preserved contradictions
weave --space ~/my-kb view <entity>         # everything about an entity + backlinks
```

Two skills ship with weave and install into your Claude Code:
- **`weave-ingest`** — project a data source into faithful facts (`lookup-before-mint`,
  contradictions preserved, source tags kept as content not trust).
- **`weave-insight`** — full-sweep inference with adversarial grounding: a generator
  proposes connections no single document states, a *separate skeptic* keeps only the
  grounded ones (survivors and reasoned rejects both persist).

**Self-demo:** a ready-made example space lives in [`examples/self-demo/`](examples/self-demo/)
— weave projected onto its own whitepaper. Run `weave --space examples/self-demo tensions`
or `weave --space examples/self-demo insights` to read weave reasoning about itself.

The engine is **stateless and location-independent**: it resolves the space, never
assumes it from the current directory. Precedence: `--space <path>` → `$WEAVE_SPACE` →
the machine-local default in `~/.config/weave/config.edn` (set via `weave register`) →
the nearest `space.edn` walking up from the cwd. The space is treated as an opaque
store (a directory today; a DB backend could slot in behind the load functions).

## Starting a new space

`weave init <dir>` scaffolds it for you — `journal/`, `projection/` (register seeded from
[`seed/register.edn`](seed/register.edn)) and a `space.edn`. `init` does not set the
default space; address a new space via `--space` / `$WEAVE_SPACE`, or `weave register` it.

## Two surfaces, one substrate

The same EAV substrate supports two ways of working, differing only at the surface:

- **Knowledge base** — project any corpus and explore via `view` / `neighbors` /
  `tensions` / `insights`. See [`docs/whitepaper.md`](docs/whitepaper.md) for the concept.
- **Project tracker** — recency-ranked, action-focused: `overview`, `topics`, `recap`,
  `journal`.

## Status

Prototype. Same EAV substrate underneath both uses; the KB views and the two ingest/
insight skills are the v1 knowledge-base product surface.
