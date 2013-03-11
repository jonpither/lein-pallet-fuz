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
  [user pub-key pri-key git-url checkout-dir]
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

  (action/service-script "pallet-fuz"
                         :template pallet-fuz-upstart
                         :service-impl :upstart
                         :literal true
                         :values {:fuz-user user
                                  :checkout-dir checkout-dir})

  (action/service "pallet-fuz" :action :start :service-impl :upstart))

(defn server-spec
  "Install lein and git, create a user, pull from github, fire up application"
  [user pub-key pri-key git-url checkout-dir]
  (api/server-spec
   :extends [(java/java {}) (lein/leiningen {}) (git/git {})]
   :phases
   {:bootstrap (api/plan-fn
                (admin-user/automated-admin-user))

    :configure
    (api/plan-fn
     (install-application user pub-key pri-key git-url checkout-dir))}))

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
  "Deploy your ring app to the cloud via a git clone from a private github repo."
  [{:keys [pallet-fuz]} & args]
  (let [server-spec (server-spec (or (-> pallet-fuz :user) "fuzzer")
                                 (-> pallet-fuz :pub-key-path io/file slurp)
                                 (-> pallet-fuz :pri-key-path io/file slurp)
                                 (-> pallet-fuz :git-url)
                                 (or (-> pallet-fuz :checkout-dir) "fuz-tmp"))

        pallet (api/group-spec "fuzgroup"
                               :extends [server-spec]
                               :node-spec (-> pallet-fuz :node-spec))

        result (condp = (keyword (first args))
                 :setup
                 (setup pallet)
                 :teardown
                 (teardown pallet)
                 :else
                 (println "Please specify either setup or teardown operation"))]

    (pprint result)))
