---
name: weave-ingest
description: Use when the user wants to ingest/project a data source (folder of docs, a repo, pasted text) INTO a weave space as faithful facts. Turns prose into an EAV fact base via weave's projection discipline.
---

# weave-ingest — Datenquelle → treue Projektion

Du projizierst eine Datenquelle in einen weave-Space (`journal/` = Quell-Prosa,
`projection/register.edn` + `facts.edn` = abgeleitete Fakten).

## Eiserne Regeln
1. **Treue, nicht Wahrheit.** Ein Fakt `[subject :k/pred value]` bedeutet *„die Quelle
   behauptet das"* — NICHT dass es wahr ist. Extrahiere getreu; erfinde nichts.
2. **lookup-before-mint (Prädikate UND Entitäten).** Vor jedem neuen Prädikat/jeder neuen
   Entität: prüfe `register.edn` bzw. bestehende `facts.edn` und verwende ein bestehendes
   Handle wieder. Nur wenn nichts passt: neu minten. Entität = **kebab-case-Keyword** mit
   `:k/label`; die *Art* markiert der Namespace — reserviert sind `:action/`, `:ctx/`,
   `:artifact/`, alles andere (nackt `:x` oder eigener Namespace `:doc/x`) ist ein Thema.
   Prädikat = ins Register mit Ein-Satz-Definition; `:version` **einmal pro Ingest-Pass**
   erhöhen (nicht pro Prädikat).
3. **Widersprüche bewahren, nicht auflösen.** Wo die Quelle sich widerspricht oder eine
   Spannung flaggt, halte beide Seiten; markiere sie als `[:e :k/tension "…"]`. Das Schema
   ist binär — hänge die Spannung an *eine* repräsentative „Besitzer"-Entität (die die
   `tensions`-Sicht dann als Titel zeigt).
4. **Quell-Tags sind Inhalt, kein Trust.** Konfidenz-/Status-Marker der Quelle
   (`[verified]`, `[assumption]`, `[conflict]` …) wandern in `:k/caution`/den Fakt-Text,
   NIE in weave-eigene Vertrauens-Metadaten.
5. **Herkunft binden.** Setze `[:e :k/source "journal/<pfad>"]` je Entität — ein
   **realer, existierender** Pfad (kein Freitext; `check` erzwingt die Existenz).
   Mehrwertig: ein dokument-übergreifendes Konzept darf mehrere `:k/source`-Fakten tragen.
   Diese treuen Ingest-Fakten bilden die „projizierte" Schicht (alles außer späteren
   `:i-*`-Insight-Entitäten).

## Ablauf
1. Space auflösen (`--space` / Argument). `weave --space <dir> check` als Ausgangslage.
2. Quelle lesen. Bei vielen/großen Dateien: parallel lesen (je Leser gibt VORSCHLÄGE
   zurück — Entitäten, Fakten, neue Prädikate), aber **du** merged sie sequenziell in
   EINEN kohärenten Stand (Kohärenz ist eine Merge-, keine Extraktions-Disziplin).
3. `register.edn` + `facts.edn` schreiben.
4. `weave --space <dir> check` MUSS grün sein. Findings vorher beheben.
5. Kurz berichten: #Fakten, #Entitäten, #Prädikate, #tensions.
