#!/usr/bin/env groovy

def createJob(String pluginName, boolean premium) {
  def namespace = premium? "scm-manager-premium-plugins" : "scm-manager-plugins"
  multibranchPipelineJob("${namespace}/${pluginName}") {

    branchSources {
      branchSource {
        source {
          scmManager {
            id('ecosystem@scm-manager/' + pluginName)
            credentialsId('SCM-Manager')
            serverUrl('https://ecosystem.cloudogu.com/scm')
            repository("${namespace}/${pluginName}/git")
            traits {
              scmManagerBranchDiscoveryTrait()
              pullRequestDiscoveryTrait {
                excludeBranchesWithPRs(true)
              }
            }
          }
        }

        strategy {
        }

      }
    }

    orphanedItemStrategy {
      discardOldItems {
        numToKeep(5)
      }
    }

    factory {
      pipelineBranchDefaultsProjectFactory {
        // The ID of the default Jenkinsfile to use from the global Config
        // File Management.
        scriptId(premium ? 'ScmPremiumPluginJenkinsfile' : 'ScmPluginJenkinsfile')

        // If enabled, the configured default Jenkinsfile will be run within
        // a Groovy sandbox.
        useSandbox false
      }
    }
  }

  queue("${namespace}/${pluginName}")
}

def createFolders() {
  folder('scm-manager-plugins') {
    description('SCM-Manager Plugins')
  }
  folder('scm-manager-premium-plugins') {
    description('SCM-Manager Premium Plugins')
  }
}

def createScmPluginJenkinsfiles() {
  createScmPluginJenkinsfile(false)
  createScmPluginJenkinsfile(true)
}

def createScmPluginJenkinsfile(boolean premium) {
  configFiles {
    groovyScript {
      id(premium ? 'ScmPremiumPluginJenkinsfile' : 'ScmPluginJenkinsfile')
      name('Jenkinsfile')
      comment('Jenkinsfile for SCM-Manager ' + (premium ? "premium " : "") + 'Plugins')
      content(readFileFromWorkspace(premium ? 'templates/Jenkinsfile.premium' : 'templates/Jenkinsfile.default'))
    }
  }
}

def createJobs() {
  createJobs(false)
  createJobs(true)
}

def createJobs(boolean premium) {
  def namespace = premium ? "scm-manager-premium-plugins" : "scm-manager-plugins"
  URL apiUrl = new URL("https://ecosystem.cloudogu.com/scm/api/v2/repositories/${namespace}?pageSize=1000")
  def repositories = new groovy.json.JsonSlurper().parse(apiUrl)
  repositories._embedded.repositories.each{ repo ->
    createJob(repo.name, premium)
  }
}

def pluginName = jm.getParameters().pluginName
def premiumPlugin = jm.getParameters().premiumPlugin.toString().toBoolean()

if ("none".equals(pluginName)) {
  createScmPluginJenkinsfiles()
} else if ("all".equals(pluginName)) {
  createScmPluginJenkinsfiles()
  createFolders()
  createJobs()
} else {
  createScmPluginJenkinsfile(premiumPlugin)
  createFolders()
  createJob(pluginName, premiumPlugin)
}

