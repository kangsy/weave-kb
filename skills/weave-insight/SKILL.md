---
name: weave-insight
description: Use when the user wants weave to GENERATE insights/tensions across a projected fact base — connections no single document states. Full-sweep generation + adversarial grounding.
---

# weave-insight — Full-Sweep-Inferenz mit adversariellem Grounding

Du erzeugst `:inferred-across`-Einsichten QUER über die Faktenbasis (Verbindungen, die in
KEINEM Dokument stehen) und erdest jede adversariell. Wert = geerdete Rekombination, nicht
Wahrheit; NIE als Wahrheit ausgeben.

## Ablauf (zwei getrennte Rollen)
1. **Generator (optimistisch).** Sweep die GANZE `facts.edn` — aber lies nur die
   **projizierte Schicht** (alle Ingest-Fakten), NIE frühere Insight-Entitäten
   (`:i-*` mit `:k/inferred-across`/`:k/rejected-inference`). Das ist die Quarantäne gegen
   Selbst-Bootstrap: Spekulation darf nicht zur eigenen Evidenz werden. Schlage Kandidaten
   vor: Spannungen, Brücken (verbindet ferne Regionen), Lücken (A→B, B→C, aber nie A→C),
   latente Cluster, same-as/Merge-Kandidaten.
2. **Verifizierer (skeptisch, UNABHÄNGIG vom Generator).** Ein separater Sub-Agent mit dem
   Auftrag zu WIDERLEGEN, Default = ablehnen (ein *gebündelter* Agent, der alle Kandidaten
   *unabhängig* prüft, erfüllt „separat" — nicht zwingend einer pro Kandidat). Pro
   Kandidat: Lässt sich jede Prämisse auf einen konkreten Fakt zeigen, OHNE externes Wissen
   zu importieren? Nur dann behalten. `:k/grounded-in` verweist auf **Entitäts-Handles**
   (kein Tripel-Typ); die konkreten Tripel nenne im `:k/confidence`-Text. Vergib Konfidenz
   (`hoch` = reine Rekombination / `plausibel` = milde Zusatzprämisse).
3. **Persistieren (alles, auch Rejects).** Reifiziere jede Einsicht als `:i-*`-Entität:
   - Überlebt: `[:i-x :k/label "…"] [:i-x :k/inferred-across "<claim>"] [:i-x :k/grounded-in :a] … [:i-x :k/confidence "<stufe> — <begründung>"]`
   - Verworfen: `[:i-x :k/label "Verworfen: …"] [:i-x :k/rejected-inference "<claim>"] [:i-x :k/grounded-in …] [:i-x :k/confidence "VERWORFEN: <grund>"]`
4. `weave --space <dir> check` MUSS grün sein.
5. Berichte: #überlebt (mit Konfidenz-Verteilung), #verworfen (mit Gründen).

## Warum so
- Getrennte Rollen verhindern Selbst-Abnicken. Geerdetheit ≠ Wahrheit — der Verifizierer
  prüft NUR Rückführbarkeit. Rejects sind Urteils-Artefakte (behalten). Zwei Läufe dürfen
  abweichen; das ist Signal, kein Defekt.
