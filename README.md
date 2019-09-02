# SCM-Manager Plugin Seed Job

The SCM-Manager Plugin Seed Job allow the creation of Jenkins jobs for SCM-Manager Plugins.
The Seed Job is build with Jenkins [Job DSL Plugin](https://jenkinsci.github.io/job-dsl-plugin/).

## Requirements

The Seed Job requires a bunch of Jenkins Plugins which are defined in the `docker/plugins.txt`.

## Usage

The Seed Job it self is defined in the `Jenkinsfile` which triggers the Job DSL script which is defined in the `plugin.groovy` script.
If the `plugin.groovy` is modified, the script must be approved, after the first run.
This must be done in the Jenkins settings (`jenkinsurl/scriptApproval/`).


The Jenkinsfile for the plugin build job is defiened at `templates/Jenkinsfile` and is stored in global **Managed files** as `ScmPluginJenkinsfile`.
This file gets refreshed on every Seed Job run.


The Seed Job accepts a single parameter, the `pluginName`.
This could be either the name of a plugin, `all` or `none`:

* **none** refreshes the `ScmPluginJenkinsfile` only
* **all** read all repositories from the [Github SCM-Manager Organization](https://github.com/scm-manager) and creates a build job for every repository
  which starts with **scm-** and ends with **-plugin**

The created build jobs are running with activated **Groovy Sandbox** and requires the following **signature approvements**:

* method groovy.lang.GroovyObject getProperty java.lang.String
* method groovy.util.XmlSlurper parseText java.lang.String
* method groovy.util.slurpersupport.GPathResult children
* method groovy.util.slurpersupport.GPathResult text
* method org.apache.maven.model.Model getVersion
* new groovy.util.XmlSlurper

This approvements are required by the used xml parser (`XmlSlurper`) and must be approved on by one during the first run(s).

### Plugin Releases

To release a plugin, do the following steps in the repository of the plugin:

* Ensure that you are on the `develop` branch
* Set the version in your `pom.xml` 
* Ensure no version is defined in `src/main/resources/scm/plugin.xml` (`information/version` not `scm-version`)
* Commit the changes to `pom.xml`
* Merge changes into the `master` branch
* Set an Git tag to the same version as defined in the `pom.xml` (without any prefixes)
* Push the changes (don't forget to push the tag (`--tags`))

The plugin build job does the following steps, if it detects a new tag on the repository:

* builds, test and analyses the plugin (same as normal snapshot build)
* ensures all three versions are equal (git tag, `pom.xml` and `plugin.xml`), if not build is aborted
* pushes the plugin to the scm-manager maven repository
* updates the plugin-center repositories with the information of the new release

# Development

We use a local Jenkins installation for the development of the seed job.
To setup those development instances just use docker-compose e.g.:

```bash
docker-compose up
```

The development instance has preinstalled all required plugins and authorization and authentication are deactivated.
After docker-compose has finished the instance is available at [http://localhost:8080](http://localhost:8080).

