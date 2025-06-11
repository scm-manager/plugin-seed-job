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
    if (env.BRANCH_NAME == 'master') {
      currentBuild.description = params.pluginName
      jobDsl targets: 'plugin.groovy'
    } else {
      echo 'skip, executing job dsl in order to avoid bringing an unfinished feature live.'
    }
  }
  
  stage('Update GitHub') {
      if (env.BRANCH_NAME == 'master' && isBuildSuccess()) {
        sh 'git checkout master'
        
        // push changes to GitHub
        authGit 'cesmarvin', "push -f https://github.com/scm-manager/plugin-seed-job master --tags"
      }
  }
  
  
}

def pluginNameDescription() {
  "Name of the plugin. Use 'none' to update the SCM Plugin Jenkinsfile only. Use 'all' to create a job for every plugin in the scm-manager github organization."
}


boolean isBuildSuccess() {
  return currentBuild.result == null || currentBuild.result == 'SUCCESS'
}

void authGit(String credentials, String command) {
  withCredentials([
    usernamePassword(credentialsId: credentials, usernameVariable: 'AUTH_USR', passwordVariable: 'AUTH_PSW')
  ]) {
    sh "git -c credential.helper=\"!f() { echo username='\$AUTH_USR'; echo password='\$AUTH_PSW'; }; f\" ${command}"
  }
}
