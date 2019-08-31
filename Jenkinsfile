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
    jobDsl targets: 'plugin.groovy'
  }
}
