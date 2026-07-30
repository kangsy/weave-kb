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
  (t "shows North"        (has? o "North"))
  (t "shows South"        (has? o "South"))
  (t "shows Roses"        (has? o "Roses"))
  (t "shows Tulips"       (has? o "Tulips"))
  (t "hides origin bed (split ancestor)"   (not (has? o "origin bed")))
  (t "hides merged-away entity"            (not (has? o "old notes")))
  (t "bare-id topic visible"               (has? o "Standalone bed"))
  (t "shows structure-review verdict"      (and (has? o "reviewed") (has? o "kept"))))

(println "overview (genealogy):")
(let [o (weave "genealogy" "overview")]
  (t "shows current action" (has? o "log the next delivery"))
  (t "uses label, not raw id" (and (has? o "[Tulips]") (not (has? o ":topic/nord-tulpen"))))
  (t "blocked-by action in Waiting, with ⛔" (has? o "⛔ supplier review")))

(println "check (genealogy, coherent):")
(let [o (weave "genealogy" "check")]
  (doseq [bad ["Dangling part-of" "cycle" "nowhere" "Journal source missing" "open next-action"]]
    (t (str "no finding: " bad) (not (has? o bad))))
  (t "superseded e1 not reported as stale" (not (has? o "Stale: :topic/e1"))))

(println "check (drift):")
(let [o (weave "drift" "check")]
  (doseq [need ["Dangling part-of" "cycle" "nowhere" "open next-action" "Journal source missing"]]
    (t (str "reports: " need) (has? o need))))

(println "journal (genealogy):")
(let [o (weave "genealogy" "journal")]
  (t "journal lists Tulips" (has? o "Tulips"))
  (t "journal picks the newest entry across multiple sources" (has? o "Multi-source test"))
  (t "unbound journal file stays visible" (has? o "Legacy note"))
  (t "superseded entity not in journal by default" (not (has? o "old notes")))
  (t "piped: raw markdown preserved (no lossy filter)" (and (has? o "**") (has? o "]("))))

(println "journal --all (genealogy):")
(let [o (weave "genealogy" "journal" "--all")]
  (t "journal --all reveals superseded entity, marked"
     (and (has? o "old notes") (has? o "superseded"))))

(println "topics --all (genealogy):")
(let [o (weave "genealogy" "topics" "--all")]
  (t "topics --all shows superseded split ancestor" (has? o "origin bed"))
  (t "topics --all shows merged-away entity" (has? o "old notes")))

(println "journal (drift, missing source):")
(let [o (weave "drift" "journal")]
  (t "journal does not crash on a missing source" (has? o "Journal —")))

(println "recap (genealogy):")
(let [o (weave "genealogy" "recap")]
  (t "recap shows projects by label" (has? o "Tulips"))
  (t "recap shows :k/digest prose when present" (has? o "Tulips bed tended"))
  (t "recap falls back mechanically to next-action" (has? o "export the bed plan as a chart"))
  (t "recap hides superseded by default" (not (has? o "old notes"))))

(println "recap --all (genealogy):")
(let [o (weave "genealogy" "recap" "--all")]
  (t "recap --all shows superseded, marked" (and (has? o "old notes") (has? o "superseded"))))

(println "styling (TTY detection):")
(let [esc    (str (char 27))
      plain  (weave "genealogy" "journal")
      styled (:out (p/sh ["bb" "weave" "--space" "test/fixtures/genealogy" "journal"]
                         {:extra-env {"WEAVE_STYLE" "1"}}))]
  (t "piped output has no ANSI escapes" (not (str/includes? plain esc)))
  (t "WEAVE_STYLE=1 produces ANSI escapes"  (str/includes? styled esc)))

(println "init (fresh space):")
(let [tmp (str "/tmp/weave-init-test-" (System/currentTimeMillis))]
  (p/sh ["bb" "weave" "init" tmp])
  (t "space.edn created"     (.exists (io/file tmp "space.edn")))
  (t "facts.edn created"     (.exists (io/file tmp "projection" "facts.edn")))
  (t "journal/ created"      (.isDirectory (io/file tmp "journal")))
  (t "register from seed"    (str/includes? (slurp (io/file tmp "projection" "register.edn")) ":k/label"))
  (let [o (:out (p/sh ["bb" "weave" "--space" tmp "check"]))]
    (t "fresh space check-green" (str/includes? o "No violations")))
  ;; ABORT without overwriting when space.edn already exists
  (spit (io/file tmp "projection" "facts.edn") "SENTINEL")
  (let [r (p/sh ["bb" "weave" "init" tmp])]
    (t "init aborts on existing space" (not (zero? (:exit r))))
    (t "init does not overwrite"       (= "SENTINEL" (slurp (io/file tmp "projection" "facts.edn"))))))

(println "neardup (consolidation lens):")
(let [d (weave "neardup" "tensions")
      v (weave "neardup" "tensions" "--verbatim")]
  (t "collapses near-duplicates to one entry with marker" (has? d "(+2 near-identical)"))
  (t "keeps the distinct tension separate"                (has? d "no coding skills"))
  (t "default shows 2 tension entries"     (= 2 (count (re-seq #"(?m)^● " d))))
  (t "--verbatim shows all 4, no marker"   (and (= 4 (count (re-seq #"(?m)^● " v)))
                                                (not (has? v "near-identical"))))
  (t "check stays green on the fixture"    (has? (weave "neardup" "check") "No violations"))
  (t "view collapses near-dup summaries"      (has? (weave "neardup" "view" ":demo") "(+1 near-identical)"))
  (t "view --verbatim expands summaries"      (not (has? (weave "neardup" "view" ":demo" "--verbatim") "near-identical")))
  (t "insights collapses near-dup claims"     (has? (weave "neardup" "insights") "(+1 near-identical)")))

(println "ticket-sample (worked example):")
(let [o (weave "ticket-sample/space" "check")]
  (t "worked-example space is coherent" (has? o "No violations")))

(println)
(if (zero? @failures)
  (println "ALL PASS")
  (do (println (str @failures " FAIL")) (System/exit 1)))
