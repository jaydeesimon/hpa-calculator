#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn process-builder [args]
  (doto (ProcessBuilder. args)
    (.inheritIO)))

(def ^:dynamic *cwd* (System/getProperty "user.dir"))

(defn fatal [& msg]
  (apply println "[\033[0;31mERROR\033[0m]" msg)
  (System/exit -1))

(defn spawn
  "Like [[clojure.java.shell/sh]], but inherit IO stream from the parent process,
  and prints out the invocation."
  [& args]
  (let [[opts args] (if (map? (last args))
                      [(last args) (butlast args)]
                      [{} args])
        dir (:dir opts *cwd*)]
    (println "=>" (str/join " " args) (if dir (str "(in " dir ")") ""))
    (let [res (-> (process-builder args)
                  (cond-> dir
                          (.directory (io/file dir)))
                  .start
                  .waitFor)]
      (when (and (not= 0 res) (not (:continue-on-error? opts)))
        (fatal (:fail-message opts "command failed") res)))))

(defn ensure-dir-exists [base-dir dir]
  (.mkdirs (File. base-dir dir)))

(let [dirs ["cljs-out"
            "css"]
      files ["resources/public/cljs-out/dev-main.js"
             "resources/public/index.html"
             "resources/public/css/style.css"]
      base-dir "/Users/jeffreysimon/projects/jaydeesimon.github.io/hpa-calc"]
  (spawn "lein" "clean")
  (spawn "lein" "fig:min")
  (doseq [dir dirs]
    (ensure-dir-exists base-dir dir))
  (doseq [file files]
    (let [[_ file-dest] (str/split file #"resources/public/")]
      (spawn "cp" file (str base-dir "/" file-dest)))))
