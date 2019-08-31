node {

  properties([
    parameters([
      string(name: 'pluginName', trim: true, description: "Name of the plugin")
    ])
  ])

  stage('Checkout') {
    checkout scm
  }

  stage('Create Job') {
   jobDsl scriptText: '''
     folder('scm-manager') {
       description('SCM-Manager')
     }
     folder('scm-manager/plugins') {
       description('SCM-Manager Plugins')
     }
     def pluginName = jm.getParameters().pluginName
     multibranchPipelineJob('scm-manager/plugins/' + pluginName) {
       // SCM source or additional configuration
       branchSources {
         branchSource {
           source {
             github {
               id('github@scm-manager/' + pluginName) // IMPORTANT: use a constant and unique identifier
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
          scriptId 'Jenkinsfile'

          // If enabled, the configured default Jenkinsfile will be run within
          // a Groovy sandbox.
          useSandbox true
        }
      }
    }
    '''
  }
}
