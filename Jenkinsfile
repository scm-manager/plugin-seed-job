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

  stage('Start Repository Scan') {
    build job: "scm-manager/plugins/${params.pluginName}", wait: false
  }
}
