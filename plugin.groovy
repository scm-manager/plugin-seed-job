#!/usr/bin/env groovy

def pluginName = jm.getParameters().pluginName

folder('scm-manager') {
  description('SCM-Manager')
}

folder('scm-manager/plugins') {
  description('SCM-Manager Plugins')
}

configFiles {
  groovyScript {
    id('ScmPluginJenkinsfile')
    name('Jenkinsfile')
    comment('Jenkinsfile for SCM-Manager Plugins')
    content(readFileFromWorkspace('templates/Jenkinsfile'))
  }
}

multibranchPipelineJob('scm-manager/plugins/' + pluginName) {

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
    traits << 'org.jenkinsci.plugins.github__branch__source.TagDiscoveryTrait'()
  }

  factory {
    pipelineBranchDefaultsProjectFactory {
      // The ID of the default Jenkinsfile to use from the global Config
      // File Management.
      scriptId 'ScmPluginJenkinsfile'

      // If enabled, the configured default Jenkinsfile will be run within
      // a Groovy sandbox.
      useSandbox true
    }
  }

}
