#!/usr/bin/env bash
# Install weave: symlink the CLI into a PATH directory AND the skills into
# Claude Code, so a Claude-Code user can run the full KB flow.
# Symlink (not copy) so `git pull` in this repo keeps CLI + skills up to date.
# Idempotent — safe to re-run.
#
#   ./install.sh                 # links CLI into ~/.local/bin, skills into ~/.claude/skills
#   WEAVE_BIN=/usr/local/bin ./install.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/weave"
BIN="${WEAVE_BIN:-$HOME/.local/bin}"
SKILLS_DST="${CLAUDE_SKILLS_DIR:-$HOME/.claude/skills}"

# --- CLI on PATH ---
chmod +x "$SRC"
mkdir -p "$BIN"
ln -sf "$SRC" "$BIN/weave"
echo "linked  $BIN/weave -> $SRC"

case ":$PATH:" in
  *":$BIN:"*) echo "OK      $BIN is on PATH" ;;
  *)          echo "WARN    $BIN is not on PATH — add it to your shell profile" ;;
esac

# --- Skills into Claude Code ---
mkdir -p "$SKILLS_DST"
for skill in "$ROOT"/skills/*/; do
  [ -f "$skill/SKILL.md" ] || continue
  name="$(basename "$skill")"
  ln -sfn "${skill%/}" "$SKILLS_DST/$name"
  echo "linked  $SKILLS_DST/$name -> ${skill%/}"
done

if command -v bb >/dev/null 2>&1; then
  echo "OK      babashka found: $(command -v bb)"
else
  echo "WARN    babashka (bb) not found — install it, e.g. brew install borkdude/brew/babashka"
fi

echo
echo "Installed. Next:"
echo "  weave init ~/my-kb                      # scaffold a fresh space"
echo "  # in Claude Code: run the weave-ingest skill on a source (e.g. a docs/ folder)"
echo "  weave --space ~/my-kb insights          # grounded insights"
echo "  weave --space ~/my-kb tensions          # preserved contradictions"
