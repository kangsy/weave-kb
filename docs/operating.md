# Weave — Betriebs-Protokoll (Runbook für Sessions)

Wie eine Claude-Session einen Weave-**Space** bedient. Setzt das Konzept aus
[`whitepaper.md`](whitepaper.md) voraus. Annahme (bewusst, Phase 1):
**alle Journal-Änderungen passieren durch Claude-Sessions.**

## Voraussetzungen

- `babashka` (`bb`) und die `weave`-CLI im Pfad (aus dem Weave-Engine-Repo).
- Ein Space-Verzeichnis mit `journal/`, `projection/` und `space.edn`.
- Space auflösen: **einmal pro Rechner** `weave register /pfad/zum/space` (schreibt die
  maschinen-lokale Config `~/.config/weave/config.edn`). Danach findet `weave` den Space
  von überall — keine cwd-Abhängigkeit. Override: `--space <pfad>` oder `$WEAVE_SPACE`.

## Kommandos

```
weave overview                       # kontextbewusst: was geht jetzt
weave topics                         # themenzentriert: was wird getrackt (Hierarchie)
weave journal [--brief]              # rückblickend: letzter Journal-Eintrag je Projekt
weave check                          # Kohärenz gegen das Register
weave divergence <dirA> <dirB>       # zwei Projektionen kontrastieren
```

## Ablauf einer Session, die an einem getrackten Projekt arbeitet

1. **Stand holen & Kontext aufnehmen.** Im Space zuerst `git pull` (Stand der anderen
   Rechner holen). Dann `weave overview` — zeigt, was auf *diesem* Rechner machbar ist
   und was wartet. Dann die reale Arbeit tun.

2. **Journal fortschreiben (Prosa, append-only).** Beim Abschluss substanzieller Arbeit
   einen Eintrag anhängen — natürliche Sprache, datiert:
   `- YYYY-MM-DD HH:MM: …` (lokale Zeit; via `date '+%Y-%m-%d %H:%M'`). Der Zeitstempel
   macht `journal` intraday-genau.
   **Eintrag lesbar strukturieren** (so viel Struktur wie Punkte, *kein* starres Schema):
   die **erste Zeile** ist die Schlagzeile in *einem* Satz — sie steht auch allein für
   `journal --brief`. Gibt es mehrere Punkte, darunter **Unterpunkte als `-`-Bullets**
   (z. B. Ursache / Fix / nächster Schritt / Blocker); Absätze durch **Leerzeilen**.
   `journal` rendert Schlagzeile, Bullets und Absätze terminal-lesbar (Inline-Markdown
   wird im echten Terminal als ANSI dargestellt — `**fett**`, `` `code` ``, `[Text](url)`;
   gepipt/`NO_COLOR`/`WEAVE_STYLE=0` bleibt das Markdown roh, Zeilen werden umgebrochen) —
   ein einziger dichter Block bleibt aber ein Block. Fließtext ist ok, wo es keine Teilpunkte gibt: Struktur folgt dem Inhalt, sie
   zwingt ihn nicht.

   ```
   - 2026-07-30 09:00: Import-Bug behoben, Version 1.2 freigegeben.
     - Ursache: fehlende Feldvalidierung
     - Fix: Validierung ergänzt, Release gebaut
     - Nächster Schritt: auf Freigabe warten (blockiert)
   ```

   Das Journal ist ein opaker, maschinell verwalteter
   Event-Log; das Verzeichnis-Layout ist eine **neu aufbaubare Sicht**, keine Identität.
   **Entitäten aus der Prosa ableiten** (nicht der Mensch): welche Projekte/Subjekte betrifft
   der Eintrag? Hierarchie („ein **Board** in Projekt A und ein gleichnamiges **Board**
   in Projekt B" — gleicher Name, zwei Entitäten) ist der ablösbare Fakt `:k/part-of` —
   niemals im ID- oder Pfadnamen codiert.
   **First-Touch = kein Sonderfall:** auch ein *neues* Projekt (auch eine neue Top-Level-
   Domäne) wird beim ersten substanziellen Fortschritt autonom aufgenommen — **ohne
   Rückfrage, ohne gesonderten Hinweis**. Ein Projekt muss nicht vorab „erfasst" sein;
   Tracking emergiert aus der Prosa. Nur wenn etwas bewusst *nicht* getrackt werden soll,
   ist das die ausdrückliche (seltene) Ausnahme.

3. **Re-projizieren (Fakten aus dem Journal ableiten).** `projection/facts.edn`
   aktualisieren. Zwingend:
   - **lookup-before-mint (Prädikate UND Entitäten)**: vor dem Vergeben eines Prädikats
     *oder* dem Anlegen einer Entität zuerst `projection/register.edn` bzw. die bestehende
     Faktenbasis prüfen und eine bestehende `:id` wiederverwenden; nur wenn nichts passt,
     neu minten. IDs sind **opak und hierarchie-frei**; der lesbare Name lebt als
     `:k/label`-Fakt. Das verhindert, dass dieselbe „Komponente" in zwei Entitäten zerfällt —
     es ist **Duplikat-Schutz, kein Argument gegen das Anlegen neuer Projekte.**
     Brauchst du ein Prädikat, das der Space noch **nicht** kennt, greift dieselbe Regel:
     erst nach einem **bestehenden Äquivalent** suchen und es wiederverwenden; nur wenn
     keins existiert, ins Register minten (Version erhöhen). Ein Fakt mit register-fremdem
     Prädikat macht `check` rot — genau das ist das Signal, zu minten *oder* umzuentscheiden.
   - **Art per Namespace, Hierarchie per Fakt.** Reservierte Namespaces markieren die
     *Art*: `:action/…`, `:ctx/…`, `:artifact/…`. Eine Projekt-/Themen-Entität ist jede
     andere Keyword-ID — nackt (`:thema`) oder mit beliebigem, nicht-reserviertem
     Namespace; der Namespace codiert **nie** die Elternschaft. **Dach-Entitäten explizit
     deklarieren** (mind. ein `:k/label`-Fakt): ein `:k/part-of`-Ziel, das nur als Objekt
     auftaucht, ist keine bekannte Entität und `check` meldet es (Dangling part-of).
   - **Alt-IDs sind harmlos.** Bestehende hierarchie-codierende IDs (`:proj/legacy-child`)
     bleiben unangetastet: die ID ist ein opaker Handle, ihr Präfix wird nie als Hierarchie
     gelesen. IDs nie umbenennen; ein echtes Um-IDen wäre ein `merge` (`:k/merged-into`).
   - **Prosa-Bindung folgt der Space-Konvention, nicht dem Runbook.** *Wie* die Projektion
     ihre Prosa/Herkunft bindet, ist **emergent im Space-Register** — kein vom Runbook
     vorgeschriebener Prädikatname. Der Space nutzt dafür `:projected-from`; per
     `lookup-before-mint` **wiederverwenden**, kein konkurrierendes Prädikat minten.
     (Der Engine-Seed *bietet* ein per-Entität-`:k/source` an — nur ein Vorschlag für
     *neue* Spaces, bestehenden Konventionen nie aufgezwungen.) Sichtbarkeit hängt an
     keiner Bindung (foundations §3.3c); ungebundene Journal-Dateien bleiben sichtbar.
   - **Evolution protokollieren.** Wird ein Projekt zum Dach oder umgekehrt umstrukturiert,
     die Operation als Genealogie festhalten: `re-parent` (Fakt `:k/part-of` ändern, ID
     bleibt), `split` (neue Entitäten + `:k/split-of` auf den Vorfahren; Vorfahr bleibt
     unsterblich abfragbar), `merge` (`:k/merged-into`). „Zum Dach befördern" ist entweder
     re-parent-mit-Kind *oder* split — die Prosa entscheidet.
   - **Struktur-Urteil fällen (bei substanzieller Änderung der Faktenlage eines Projekts).**
     Zurücktreten und *bewerten*: passt die aktuelle Struktur noch zu den angesammelten
     Fakten — **beibehalten oder ändern?** Das ist ein **echtes Urteil, keine Regel** (kein
     „≥N Actions → Dach"). Leitplanken, nicht Automatik: Tickets/Arbeitsitems bleiben
     **Actions** (vergänglich, werden erledigt entfernt); **Subprojekte sind dauerhafte
     Flächen**; ein Projekt wird zum **Dach**, wenn seine Fäden zu solchen Flächen clustern.
     Das Verdikt festhalten: die **Begründung ins Journal** (Prosa, Historie), das kurze
     Ergebnis als `:k/structure-review` („`<Datum>` · `beibehalten|geändert` · `<knapp>`").
     **„Beibehalten" ist genauso wertvoll wie „ändern"** — es macht die Struktur zu einer
     *bewerteten*, nicht bloß angesammelten. `topics` zeigt das Verdikt; ein altes Prüf-
     Datum stupst beim nächsten Mal an.
   - **Stabile IDs behalten.** Labels dürfen sich ändern, IDs nie.
   - Kontexte (`:ctx/...`) ebenfalls im Register definieren; das Ambient-Mapping steht
     in `space.edn` (`:ambient`).
   - `:k/last-touched` der berührten Entität auf das heutige Datum setzen.
   - **Stand-Digest (optional, empfohlen bei Mehr-Faden-Projekten).** Je berührter
     Entität einen kurzen `:k/digest`-Satz pflegen („wo steht es" — aktueller Stand, kein
     Verlauf). Das ist die Prosa, die `weave recap` zeigt; fehlt sie, fällt recap
     mechanisch auf `:k/blocked-by`/`:k/next-action` zurück (bei vielen Actions als Liste).

4. **Prüfen.** `weave check` — muss `✓` liefern. Findings vor dem Commit beheben.

5. **Committen und sofort pushen.** Journal + Projektion zusammen, sprechende Message,
   dann `git push`. **Ein nicht gepushter Commit ist kein Sync** — nie ungepusht lassen.

## Optional: Replay + Divergenz (epistemische Sicht)

Eine **frische** Projektion in einen Schnappschuss-Ordner schreiben und kontrastieren:

```
weave divergence projection snapshots/<datum>
```

Kosmetische Divergenz (gleiche ID, anderes Label) ist Rauschen; semantische (neues
Konzept, verfeinerte/aufgetauchte Fakten) ist Signal.

## Regeln (aus den Foundations)

- **Reproduzierbarkeit ist kein Ziel** — Treue + Kohärenz schon. Divergenz ist ok.
- **Keine Top-down-Taxonomie**, keine manuellen Tags/Status durch den Nutzer.
- **Register ist versioniert** und Teil der Faktenmenge; bei Verfeinerung
  `:supersedes`/`:refines` am Konzept pflegen.
- **FCA** nur optional/offline als Split/Merge-Vorschlag — kein Laufzeit-Verband.

## Wachpunkte

- Nichtdeterminismus/Konvergenz der Re-Projektion (Foundations §9.1) — beobachten.
- Bei großem Register: Full-Replay vermeiden, inkrementell re-projizieren.
