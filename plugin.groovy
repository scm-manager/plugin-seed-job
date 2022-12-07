#!/usr/bin/env groovy

def createJob(String pluginName) {
  multibranchPipelineJob('scm-manager-plugins/' + pluginName) {

    branchSources {
      branchSource {
        source {
          scmManager {
            id('ecosystem@scm-manager/' + pluginName)
            credentialsId('SCM-Manager')
            serverUrl('https://ecosystem.cloudogu.com/scm')
            repository('scm-manager-plugins/' + pluginName + '/git')
            traits {
              scmManagerBranchDiscoveryTrait()
              pullRequestDiscoveryTrait {
                excludeBranchesWithPRs(true)
              }
            }
          }
        }

        strategy {
/*          buildRegularBranches()
          buildChangeRequests {
            ignoreTargetOnlyChanges true
            ignoreUntrustedChanges true
          }*/
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
  URL apiUrl = new URL("https://ecosystem.cloudogu.com/scm/api/v2/repositories/scm-manager-plugins?pageSize=1000")
  def repositories = new groovy.json.JsonSlurper().parse(apiUrl)
  repositories._embedded.repositories.each{ repo ->
    createJob(repo.name)
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

