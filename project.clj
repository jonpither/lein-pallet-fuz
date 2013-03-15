(defproject lein-pallet-fuz "0.1.1"
  :description "Plugin for deploying apps to EC2 using git"
  :url "https://github.com/jonpither/lein-pallet-fuz"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories
  {"sonatype-snapshots" "https://oss.sonatype.org/content/repositories/snapshots"
   "sonatype" "https://oss.sonatype.org/content/repositories/releases/"}

  :dependencies [
                 ;; The latest pallet
                 [com.palletops/pallet "0.8.0-beta.4"]

                 ;; The latest jclouds fun
                 [org.cloudhoist/pallet-jclouds "1.5.2"]
                 [org.jclouds/jclouds-allblobstore "1.5.5"]

                 [org.jclouds/jclouds-allcompute "1.5.5"]
                 [org.jclouds.driver/jclouds-slf4j "1.5.5"]
                 [org.jclouds.driver/jclouds-jsch "1.5.5"]
                 [org.jclouds/jclouds-compute "1.5.5"]
                 [org.jclouds/jclouds-blobstore "1.5.5"]

                 ;; Pallet crates:

                 [com.palletops/java-crate "0.8.0-beta.1"]
                 [com.palletops/lein-crate "0.8.0-alpha.1"]
                 [com.palletops/git-crate "0.8.0-alpha.1"]]

  :exclusions [org.jclouds/jclouds-allblobstore
               org.jclouds/jclouds-allcompute
               org.jclouds/jclouds-compute
               org.jclouds/jclouds-blobstore
               org.jclouds.driver/jclouds-slf4j
               org.jclouds.driver/jclouds-jsch
               org.jclouds.labs/greenqloud-compute]

  :eval-in-leiningen true)
