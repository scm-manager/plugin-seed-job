node {

  properties([
    parameters([
      string(name: 'pluginName', trim: true, description: pluginNameDescription(), defaultValue: 'none')
    ])
  ])

  stage('Checkout') {
    checkout scm
  }

  stage('Create Job') {
    currentBuild.description = params.pluginName
    jobDsl targets: 'plugin.groovy'
  }

}

def pluginNameDescription() {
  "Name of the plugin. Use 'none' to update the SCM Plugin Jenkinsfile only. Use 'all' to create a job for every plugin in the scm-manager github organization."
}
