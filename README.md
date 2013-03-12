# lein-pallet-dep

A Leiningen plugin for deploying ring applications to the cloud via pulling from a private github repo. You configure it via your project.clj. Tested with EC2 and an Ubuntu instance.

## Usage

Put `[lein-pallet-fuz "0.1.0]` into the `:plugins` vector of your project.clj.

    $ lein pallet-fuz


Consult the sample project file here. You'll need to setup github as to authorise using the ssh keypair you want to use. Github has the 'deployment key' feature exactly for this purpose.

## Limitations

This plugin is quite simple so if you want to do more with Pallet then you should probably just rip this code and do whatever. It could be possible to make this plugin extensible but for the moment I'm wary of maintaining a layer on top of pallet.

## License

Copyright © 2013 @jonpither

Do whatever you want with it
