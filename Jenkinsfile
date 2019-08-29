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
         github {
           id('github@scm-manager/' + pluginName) // IMPORTANT: use a constant and unique identifier
           scanCredentialsId('cesmarvin-github')
           checkoutCredentialsId('cesmarvin-github')
           repoOwner('scm-manager')
           repository(pluginName)
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
