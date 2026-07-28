#!/usr/bin/env bb
(require '[babashka.process :as p]
         '[clojure.string :as str]
         '[clojure.java.io :as io])

(defn weave [space & args]
  (let [{:keys [out err]} (p/sh (into ["bb" "weave" "--space" (str "test/fixtures/" space)] args))]
    (str out err)))

(def failures (atom 0))
(defn t [desc ok?] (if ok? (println "  ✓" desc)
                       (do (swap! failures inc) (println "  ✗" desc))))
(defn has? [s sub] (str/includes? s sub))

(println "topics (genealogy):")
(let [o (weave "genealogy" "topics")]
  (t "zeigt Nord"        (has? o "Nord"))
  (t "zeigt Süd"        (has? o "Süd"))
  (t "zeigt Rosen"  (has? o "Rosen"))
  (t "zeigt Tulpen"     (has? o "Tulpen"))
  (t "versteckt Ur-Beet (split-Vorfahr)" (not (has? o "Ur-Beet")))
  (t "versteckt merged-away Entität"        (not (has? o "alte Notizen")))
  (t "nackte-ID Topic sichtbar"             (has? o "Standalone-Beet"))
  (t "zeigt Struktur-Prüf-Verdikt"          (and (has? o "geprüft") (has? o "beibehalten"))))

(println "overview (genealogy):")
(let [o (weave "genealogy" "overview")]
  (t "zeigt aktuelle Aktion" (has? o "nächste Lieferung protokollieren"))
  (t "nutzt Label statt roher ID" (and (has? o "[Tulpen]") (not (has? o ":topic/nord-tulpen"))))
  (t "blocked-by-Aktion in Wartet, mit ⛔" (has? o "⛔ Lieferanten-Review")))

(println "check (genealogy, kohärent):")
(let [o (weave "genealogy" "check")]
  (doseq [bad ["Dangling part-of" "Zyklus" "ins Leere" "Journal-Quelle fehlt" "offene next-action"]]
    (t (str "keine Meldung: " bad) (not (has? o bad))))
  (t "abgelöste e1 nicht als stale gemeldet" (not (has? o "Stale: :topic/e1"))))

(println "check (drift):")
(let [o (weave "drift" "check")]
  (doseq [need ["Dangling part-of" "Zyklus" "ins Leere" "offene next-action" "Journal-Quelle fehlt"]]
    (t (str "meldet: " need) (has? o need))))

(println "journal (genealogy):")
(let [o (weave "genealogy" "journal")]
  (t "journal listet Tulpen" (has? o "Tulpen"))
  (t "journal wählt neuesten Eintrag über mehrere Quellen" (has? o "Doppelquelle-Test"))
  (t "ungebundene Journal-Datei bleibt sichtbar" (has? o "Legacy-Notiz"))
  (t "abgelöste Entität standardmäßig NICHT im journal" (not (has? o "alte Notizen")))
  (t "gepipt: rohes Markdown bleibt erhalten (kein lossy Filter)" (and (has? o "**") (has? o "]("))))

(println "journal --all (genealogy):")
(let [o (weave "genealogy" "journal" "--all")]
  (t "journal --all enthüllt abgelöste Entität, markiert"
     (and (has? o "alte Notizen") (has? o "abgelöst"))))

(println "topics --all (genealogy):")
(let [o (weave "genealogy" "topics" "--all")]
  (t "topics --all zeigt abgelösten Split-Vorfahren" (has? o "Ur-Beet"))
  (t "topics --all zeigt merged-away Entität" (has? o "alte Notizen")))

(println "journal (drift, fehlende Quelle):")
(let [o (weave "drift" "journal")]
  (t "journal crasht nicht bei fehlender Quelle" (has? o "Journal —")))

(println "recap (genealogy):")
(let [o (weave "genealogy" "recap")]
  (t "recap zeigt Projekte per Label" (has? o "Tulpen"))
  (t "recap zeigt :k/digest-Prosa wenn vorhanden" (has? o "Tulpen-Beet gepflegt"))
  (t "recap fällt mechanisch auf next-action zurück" (has? o "Beet-Plan als Diagramm exportieren"))
  (t "recap versteckt abgelöste standardmäßig" (not (has? o "alte Notizen"))))

(println "recap --all (genealogy):")
(let [o (weave "genealogy" "recap" "--all")]
  (t "recap --all zeigt abgelöste, markiert" (and (has? o "alte Notizen") (has? o "abgelöst"))))

(println "styling (TTY-Erkennung):")
(let [plain  (weave "genealogy" "journal")
      styled (:out (p/sh ["bb" "weave" "--space" "test/fixtures/genealogy" "journal"]
                         {:extra-env {"WEAVE_STYLE" "1"}}))]
  (t "gepipte Ausgabe ohne ANSI-Escapes" (not (str/includes? plain "[")))
  (t "WEAVE_STYLE=1 erzeugt ANSI-Escapes"  (str/includes? styled "[")))

(println "init (frischer Space):")
(let [tmp (str "/tmp/weave-init-test-" (System/currentTimeMillis))]
  (p/sh ["bb" "weave" "init" tmp])
  (t "space.edn angelegt"    (.exists (io/file tmp "space.edn")))
  (t "facts.edn angelegt"    (.exists (io/file tmp "projection" "facts.edn")))
  (t "journal/ angelegt"     (.isDirectory (io/file tmp "journal")))
  (t "register aus Seed"     (str/includes? (slurp (io/file tmp "projection" "register.edn")) ":k/label"))
  (let [o (:out (p/sh ["bb" "weave" "--space" tmp "check"]))]
    (t "frischer Space check-grün" (str/includes? o "No violations")))
  ;; ABBRUCH ohne Überschreiben, wenn space.edn schon existiert
  (spit (io/file tmp "projection" "facts.edn") "SENTINEL")
  (let [r (p/sh ["bb" "weave" "init" tmp])]
    (t "init bricht bei existierendem Space ab" (not (zero? (:exit r))))
    (t "init überschreibt nicht"                (= "SENTINEL" (slurp (io/file tmp "projection" "facts.edn"))))))

(println)
(if (zero? @failures)
  (println "ALL PASS")
  (do (println (str @failures " FAIL")) (System/exit 1)))
