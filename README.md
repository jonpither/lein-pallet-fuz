# lein-pallet-fuz

A Leiningen plugin for deploying ring applications to the cloud via pulling from a private github repo using [Pallet](http://palletops.com/). Once deployed the web app will be started remotely via `lein ring server`. You configure it via your project.clj. Tested with EC2 and an Ubuntu instance.

This plugin is primarily here to serve as a reference point for people to see how you can use Pallet to do this various things, such as registering an upstart service and pulling from github. If you want to do more with Pallet then you should probably just rip the code and do whatever.

## Latest version

`[lein-pallet-fuz "0.1.2]`

## Usage

First things first, set up pallet with the service credentials it needs. Visit the [first-steps page](http://palletops.com/doc/first-steps/). In particular you'll need some service config in your `~/.pallet directory`. See the `lein pallet add-service` task. If you're deploying to EC2 you'll need your aws-key and aws-secret-key which you can find contained within the 'security credentials' section in the EC2 portal.

Consult the [sample-project.clj](https://github.com/jonpither/lein-pallet-fuz/blob/master/sample-project.clj) file here for how to configure. You'll need to setup github as to authorise using the ssh keypair you want to use for access to the private repo. Github has the 'deployment key' feature exactly for this purpose.

Once your project.clj is setup you can now do:

    $ lein pallet-fuz setup

and

	$ lein pallet-fuz teardown


The plugin bascially executes `lein ring server` remotely, so you should try this locally if you have any problems.

The output of the remote service execution is stored in `~/{user}/{checkout-dir}/out.log` so you can check this for errors.

## License

Copyright © 2013 @jonpither

Do whatever you want with it
