(defproject sample-project "0.1.0"
  :description "Sample project for using lein pallet-fuz"
  :url "http://example.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]

  ;; You can set up lein-pallet-fuz as a typical plugin dependency.
  ;; Here however we set it up as a plugin inside of a profile.
  ;; This is so when any other lein task is executed in the cloud we don't
  ;; trigger a download of all the pallet-fuz dependencies.

  :profiles {:dev {:plugins [[lein-pallet-fuz "0.1.0"]
                             [org.cloudhoist/pallet-lein "0.5.1"]]}}

  ;; Ensure you fill in the place-holders
  ;;
  ;; Mandatory:
  ;;   git-url - url to github project
  ;;   pub-key-path - path to the deployment user public key
  ;;   pri-key-path - path to the deployment user private key
  ;;
  ;; Optional:
  ;;   user - username of the deployment user to be created, defaults to fuzzer
  ;;   checkout-dir - dir name where git project is cloned to, defaults to fuz-tmp
  ;;   port - port used for ring, defaults to 3000
  ;;   service-name - name to use to register upstart service, defaults to pallet-fuz
  ;;   group-name - name of the group-spec, defaults to fuzgroup


  :pallet-fuz {

               ;; Mandatory pallet-fuz args

               :pub-key-path "{pathtopubkey}"
               :pri-key-path "{pathtoprivkey}"
               :git-url "{github-url}"

               ;; A sample EC2 pallet node-spec

               :node-spec {:image {:os-family :ubuntu
                                   :os-version-matches "12.04"}
                           :hardware {:hardware-id "t1.micro"}
                           :network {:inbound-ports [22 3000]}
                           :location {:location-id "us-east-1"}}})))
