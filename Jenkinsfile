node {

  properties([
    parameters([
      string(name: 'pluginName', trim: true, description: pluginNameDescription(), defaultValue: 'none'),
      booleanParam(name: 'premiumPlugin', description: "Select this, if the plugin should be handled as a premium plugin")
    ])
  ])

  stage('Checkout') {
    checkout scm
  }

  stage('Create Job') {
    if (env.BRANCH_NAME == 'master') {
      currentBuild.description = params.pluginName
      jobDsl targets: 'plugin.groovy'
    } else {
      echo 'skip, executing job dsl in order to avoid bringing an unfinished feature live.'
    }
  }
}

def pluginNameDescription() {
  "Name of the plugin. Use 'none' to update the SCM Plugin Jenkinsfile only. Use 'all' to create a job for every plugin in the scm-manager github organization."
}
