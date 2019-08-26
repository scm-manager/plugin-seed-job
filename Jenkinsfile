node {

  properties([
    parameters([
      string(name: 'PLUGIN_NAME', trim: true, description: "Name of the plugin")
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
     def pluginName = jm.getParameters().PLUGIN_NAME
     pipelineJob('scm-manager/plugins/' + pluginName) {
       definition {
         cps {
           script(readFileFromWorkspace('templates/Jenkinsfile'))
           sandbox()
         }
       }
     }
    '''
  }
}
