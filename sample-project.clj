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

  :plugins [[lein-pallet-fuz "0.1.0"]]

  ;; Ensure you fill in the place-holders

  :pallet-fuz {:pub-key-path "{pathtopubkey}"
               :pri-key-path "{pathtoprivkey}"
               :git-url "{github-url}"
               :user "{some-user}"
               :checkout-dir "{checkoutdir}"
               :node-spec {:image {:os-family :ubuntu
                                   :os-version-matches "12.04"}
                           :hardware {:hardware-id "t1.micro"}
                           :network {:inbound-ports [22 3000]}
                           :location {:location-id "us-east-1"}}})))
