# EC-SingleSignOnConfigurator plugin

The SingleSignOnConfigurator plugin was an internally bundled platform resource used to integrate Kerberos keytabs with CloudBees CD/RO. As of CloudBees CD/RO v2023.10.0, the features and APIs associated with these features were removed.

> **_NOTE:_** This plugin and its features have been deprecated from CloudBees CD/RO.
> For information on configuring CloudBees CD/RO with Kerberos, refer to [Configuring single sign-on with Kerberos](https://docs.cloudbees.com/docs/cloudbees-sda/latest/configure/kerberos-sso).

If you still want to use this plugin, in theory it is possible. However, CloudBees no longer offers support for its usage, and any implementation would be specific to your project.

## Building the plugin

Run `./gradlew` to compile the plugin.

## Installing the plugin

To install the plugin .jar, follow the instruction found on [Installing a plugin from a file](https://docs.cloudbees.com/docs/cloudbees-cd/latest/plugin-manager/install-plugins#_installing_a_plugin_from_a_file). 