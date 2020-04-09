#!/usr/bin/env groovy

def createJob(String pluginName) {
  multibranchPipelineJob('scm-manager-plugins/' + pluginName) {

    branchSources {
      branchSource {

        source {
          github {
            id('github@scm-manager/' + pluginName)
            credentialsId('cesmarvin-github')
            repoOwner('scm-manager')
            repository(pluginName)
            configuredByUrl(true)
            repositoryUrl("https://github.com/scm-manager/" + pluginName)
          }
        }

        buildStrategies {
          skipInitialBuildOnFirstBranchIndexing()
          buildTags {
            atLeastDays ''
            atMostDays '7'
          }
        }

      }
    }

    orphanedItemStrategy {
      discardOldItems {
        numToKeep(5)
      }
    }

    configure {
      def traits = it / sources / data / 'jenkins.branch.BranchSource' / source / traits
      traits << 'org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait' {
        strategyId(1)
      }
      traits << 'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
        strategyId(1)
      }
    }

    factory {
      pipelineBranchDefaultsProjectFactory {
        // The ID of the default Jenkinsfile to use from the global Config
        // File Management.
        scriptId 'ScmPluginJenkinsfile'

        // If enabled, the configured default Jenkinsfile will be run within
        // a Groovy sandbox.
        useSandbox false
      }
    }

  }

  queue('scm-manager-plugins/' + pluginName)
}

def createFolders() {
  folder('scm-manager-plugins') {
    description('SCM-Manager Plugins')
  }
}

def createScmPluginJenkinsfile() {
  configFiles {
    groovyScript {
      id('ScmPluginJenkinsfile')
      name('Jenkinsfile')
      comment('Jenkinsfile for SCM-Manager Plugins')
      content(readFileFromWorkspace('templates/Jenkinsfile'))
    }
  }
}

def createJobs() {
  URL apiUrl = new URL("https://api.github.com/orgs/scm-manager/repos")
  def repositories = new groovy.json.JsonSlurper().parse(apiUrl)
  repositories.each { repo ->
    if (repo.name.startsWith("scm-") && repo.name.endsWith("-plugin")) {
      createJob(repo.name)
    }
  } 
}

def pluginName = jm.getParameters().pluginName

if ("none".equals(pluginName)) {
  createScmPluginJenkinsfile()
} else if ("all".equals(pluginName)) {
  createScmPluginJenkinsfile()
  createFolders()
  createJobs()
} else {
  createScmPluginJenkinsfile()
  createFolders()
  createJob(pluginName)
}

