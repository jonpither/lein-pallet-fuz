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

;; 2 settings
;;  1 for server spec
;;  1 for lein task?

(def pallet-fuz-upstart "upstart.conf")

(crate/defplan install-application
  [{:keys [user pub-key pri-key git-url checkout-dir port service-name]}]
  (action/package-manager :update)

  ;; Setup deployment user
  (action/user user :action :create :shell :bash :create-home true)
  (ssh-key/install-key user "id_rsa" pri-key pub-key)

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

(defn server-spec
  "Install lein and git, create a user, pull from github, fire up application"
  [settings]
  (api/server-spec
   :extends [(java/java {}) (lein/leiningen {}) (git/git {})]
   :phases
   {:bootstrap (api/plan-fn
                (admin-user/automated-admin-user))

    :configure
    (api/plan-fn
     (install-application settings))}))

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

  (let [{:keys [git-url pub-key-path pri-key-path
                user checkout-dir port service-name group-name out-file]} pallet-fuz

        server-spec (server-spec
                     {;; mandatory args:
                      :git-url git-url
                      :pub-key (-> pub-key-path io/file slurp)
                      :pri-key (-> pri-key-path io/file slurp)

                      ;; optional args:
                      :user (or user "fuzzer")
                      :checkout-dir (or checkout-dir "fuz-tmp")
                      :port (or port 3000)
                      :service-name (or service-name "pallet-fuz")
                      })

        out-file (-> out-file (or "lein-pallet-fuz.out") io/file)

        pallet (api/group-spec (or group-name "fuzgroup")
                               :extends [server-spec]
                               :node-spec (-> pallet-fuz :node-spec))

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
