(ns leiningen.pallet-fuz
  (use [clojure.pprint :only [pprint]])
  (:require [pallet.compute :as compute]
            [pallet.api :as api]
            [pallet.configure :as configure]
            [pallet.actions :as action]
            [pallet.crate.automated-admin-user :as admin-user]
            [pallet.crate.java :as java]
            [pallet.crate.lein :as lein]
            [pallet.crate :as crate]
            [pallet.crate.ssh-key :as ssh-key]
            [pallet.script :as script]
            [pallet.crate.git :as git]
            [clojure.java.io :as io]))

(def pallet-fuz-upstart "upstart.conf")

(crate/defplan install-application
  [{:keys [user pub-key-path pri-key-path git-url checkout-dir port service-name]}]
  (action/package-manager :update)

  ;; Setup deployment user
  (action/user user :action :create :shell :bash :create-home true)

  (ssh-key/install-key user "id_rsa"
                       (-> pri-key-path io/file slurp)
                       (-> pub-key-path io/file slurp))

  (pallet.action/with-action-options {:sudo-user user
                                      :script-env {:HOME (str "/home/" user)}
                                      :script-dir (str "/home/" user "/" checkout-dir)}

    ;; We need to pull from github without any prompts
    (action/remote-file (str "/home/" user "/.ssh/config")
                        :content "Host *github.com\n    StrictHostKeyChecking no")

    ;; Clone from git and cd in
    (git/clone git-url :checkout-dir (str "/home/" user "/" checkout-dir))

    ;; Trigger a download of lein
    (action/exec-script (str "cd /home/" ~user "/" ~checkout-dir))
    (lein/lein "version"))

  (action/service-script service-name
                         :template pallet-fuz-upstart
                         :service-impl :upstart
                         :literal true
                         :values {:fuz-user user
                                  :checkout-dir checkout-dir
                                  :port port})

  (action/service service-name :action :start :service-impl :upstart))

(def default-settings {:user "fuzzer"
                       :checkout-dir "fuz-tmp"
                       :port 3000
                       :service-name "pallet-fuz"})

(defn fuz
  "Install lein and git, create a user, pull from github, fire up application"
  [settings]
  (api/server-spec
   :extends [(java/java {}) (lein/leiningen {}) (git/git {})]
   :phases
   {:bootstrap (api/plan-fn
                (admin-user/automated-admin-user))

    :configure
    (api/plan-fn
     (install-application (merge default-settings settings)))}))

(defn setup [pallet]
  (println "Setting up...")
  (api/converge
   (assoc pallet :count 1)
   :compute (configure/compute-service :aws)))

(defn teardown [pallet]
  (println "Tearing down...")
  (api/converge
   (assoc pallet :count 0)
   :compute (configure/compute-service :aws)))

(defn pallet-fuz
  "Deploy your ring app to the cloud via a git clone from a private github repo.
   There's a lot of data coming back from Pallet so we spit it to a file."
  [{:keys [pallet-fuz]} & args]
  {:pre [(:git-url pallet-fuz) (:pub-key-path pallet-fuz) (:pri-key-path pallet-fuz)]}

  (let [{:keys [group-name node-spec out-file]} pallet-fuz

        ;; TODO move the group-spec config out to the project.clj
        ;;  in theory this makes this plugin extensible
        ;;  and potentially redundant (the lein bit)
        ;;    turn the plugin into a crate :-)
        pallet (api/group-spec (or group-name "fuzgroup")
                               :extends [(fuz pallet-fuz)]
                               :node-spec node-spec)

        out-file (-> out-file (or "lein-pallet-fuz.out") io/file)

        result (condp = (keyword (first args))
                 :setup
                 (setup pallet)
                 :teardown
                 (teardown pallet)
                 :else
                 (println "Please specify either setup or teardown operation"))

        result (deref result)]

    ;; Deal with the output
    (spit out-file (with-out-str (pprint result)))
    (println "Wrote output to" (.getPath out-file))
    (if (:phase-errors result)
      (println "There are errors, check the output.")
      (println "SUCCESS."))
    ))
