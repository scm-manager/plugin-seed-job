node {

  properties([
    parameters([
      string(name: 'pluginName', trim: true, description: "Name of the plugin"),
      choice(name: 'scmType', choices: ['Git', 'Mercurial'], description: 'Type of SCM')
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
     pipelineJob('scm-manager/plugins/' + pluginName) {
       definition {
         cps {
           script(readFileFromWorkspace('templates/Jenkinsfile'))
         }
       }
       triggers {
         scm('')
       }
       environmentVariables {
         env("pluginName", pluginName)
         env("scmType", scmType)
       }
     }
    '''
  }
}
