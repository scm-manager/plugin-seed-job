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
   def pluginName = jm.getParameters().PLUGIN_NAME

    pipelineJob(pluginName) {
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
