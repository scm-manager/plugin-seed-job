#!groovy
pipeline {

  agent {
    docker {
      image 'scmmanager/java-build:11.0.9_11.1'
      label 'docker'
    }
  }

  environment {
    HOME = "${env.WORKSPACE}"
    SONAR_USER_HOME = "${env.WORKSPACE}/.sonar"
  }

  stages {

    stage('Prepare build') {
      steps {
        script {
          if (fileExists('build.gradle')) {
            buildTool = new Gradle(this)
          } else {
            buildTool = new Maven(this)
          }
        }
      }
    }

    stage('Set Version') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        // read version from brach, set it and commit it
        script {
          buildTool.setVersion(releaseVersion)
        }
        commit "release version ${releaseVersion}"

        // fetch all remotes from origin
        sh 'git config "remote.origin.fetch" "+refs/heads/*:refs/remotes/origin/*"'
        sh 'git fetch --all'

        // checkout, reset and merge
        sh 'git checkout master'
        sh 'git reset --hard origin/master'
        sh "git merge --ff-only ${env.BRANCH_NAME}"

        // set tag
        tag releaseVersion
      }
    }

    stage('Build') {
      steps {
        script {
          buildTool.build()
        }
      }
    }

    stage('Check') {
      steps {
        script {
          buildTool.check()
        }
      }
    }

    stage('SonarQube') {
      steps {
        sh 'git config "remote.origin.fetch" "+refs/heads/*:refs/remotes/origin/*"'
        sh 'git fetch origin master'
        script {
          buildTool.sonarQube()
        }
      }
    }

    stage('Deployment') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        withYarnAuth('cesmarvin_npm_token') {
          script {
            buildTool.deploy()
          }
        }
      }
      post { 
        always {
          sh "rm -f settings.xml .npmrc .yarnrc || true"
        }
      }
    }

    stage('Push Tag') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        // push changes back to remote repository
        authGit 'cesmarvin-github', 'push origin master --tags'
        authGit 'cesmarvin-github', 'push origin --tags'
      }
    }

    stage('Plugin Center') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        sh 'rm -rf plugin-center || true'
        sh 'mkdir plugin-center'
        dir("plugin-center") {
          git branch: 'master', changelog: false, poll: false, url: 'https://github.com/scm-manager/website.git'
          script {
            String filename = releaseVersion.replace('.', '-') + ".yaml"
            String pluginName = env.JOB_NAME.split('/')[1]
            dir("content/plugins/${pluginName}/releases") {
              sh "mv ${buildTool.releaseDescriptorPath()} ${filename}"
              sh "git add ${filename}"
              commit "${pluginName}: release ${releaseVersion}"
              authGit 'cesmarvin-github', 'push origin HEAD:master'
            }
          }
        }
      }
    }

    stage('Set Next Version') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        sh returnStatus: true, script: "git branch -D develop"
        sh "git checkout develop"
        sh "git merge master"
        script {
          buildTool.setVersionToNextSnapshot()
        }

        commit 'Prepare for next development iteration'
        authGit 'cesmarvin-github', 'push origin develop'
      }
    }

    stage('Delete Release Branch') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        authGit 'cesmarvin-github', "push origin :${env.BRANCH_NAME}"
      }
    }

  }
}

BuildTool buildTool

String getReleaseVersion() {
  return env.BRANCH_NAME.substring("release/".length());
}

void commit(String message) {
  sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' commit -m '${message}'"
}

void tag(String version) {
  String message = "release version ${version}"
  sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' tag -m '${message}' ${version}"
}

void withYarnAuth(String credentials, Closure<Void> closure) {
  withCredentials([string(credentialsId: credentials, variable: 'NPM_TOKEN')]) {
    writeFile encoding: 'UTF-8', file: '.npmrc', text: "//registry.npmjs.org/:_authToken='${NPM_TOKEN}'"
    writeFile encoding: 'UTF-8', file: '.yarnrc', text: '''
      registry "https://registry.npmjs.org/"
      always-auth true
      email cesmarvin@cloudogu.com
    '''.trim()

    closure.call()
  }
}

void authGit(String credentials, String command) {
  withCredentials([
    usernamePassword(credentialsId: credentials, usernameVariable: 'AUTH_USR', passwordVariable: 'AUTH_PSW')
  ]) {
    sh "git -c credential.helper=\"!f() { echo username='\$AUTH_USR'; echo password='\$AUTH_PSW'; }; f\" ${command}"
  }
}

interface BuildTool {
  void setVersion(String version)
  void check()
  void build()
  void sonarQube()
  void deploy()
  String releaseDescriptorPath()
  void setVersionToNextSnapshot()
}

class Gradle implements BuildTool {

  def script

  Gradle(def script) {
    this.script = script
  }

  void setVersion(String version) {
    gradle "setVersion --newVersion=${version}"
    gradle 'fix'
    script.sh 'git add gradle.properties package.json'
  }

  void check() {
    gradle 'check -PignoreTestFailures=true'
    // update timestamp to avoid rerun tests again and fix junit-plugin:
    // ERROR: Test reports were found but none of them are new
    script.sh 'touch build/test-results/*/*.xml || true'
    script.junit allowEmptyResults: true, testResults: 'build/test-results/*/*.xml'
  }

  void build() {
    gradle 'build -xtest'
    script.archiveArtifacts artifacts: 'build/libs/*.smp'
  }

  void sonarQube(){
    script.withSonarQubeEnv('sonarcloud.io-scm') {
      String sonar = "sonarqube -Dsonar.organization=scm-manager -Dsonar.branch.name=${script.env.BRANCH_NAME}"
      if (script.env.BRANCH_NAME != "master") {
        sonar += " -Dsonar.branch.target=master"
      }
      gradle sonar
    }
  }

  void deploy() {
    script.withCredentials([script.usernamePassword(credentialsId: 'maven.scm-manager.org', passwordVariable: 'serverPassword', usernameVariable: 'serverUsername')]) {
      gradle "publish -PpackagesScmManagerUsername=${script.env.serverUsername} -PpackagesScmManagerPassword=${script.env.serverPassword}"
    }
  }

  String releaseDescriptorPath() {
    return "${script.env.WORKSPACE}/build/libs/release.yaml"
  }

  void setVersionToNextSnapshot() {
    gradle 'setVersionToNextSnapshot'
    gradle 'fix'
    script.sh 'git add gradle.properties package.json'
  }

  void gradle(String command) {
    script.sh "./gradlew ${command}"
  }

}

class Maven implements BuildTool {

  def script

  Maven(def script) {
    this.script = script
  }

  void setVersion(String version) {
    mvn "versions:set -DgenerateBackupPoms=false -DnewVersion=${version}"
    mvn 'smp:fix'
    script.sh 'git add pom.xml package.json'
  }

  void check() {
    mvn 'clean org.jacoco:jacoco-maven-plugin:0.8.5:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.5:report -Dmaven.test.failure.ignore=true'
    // Archive Unit and integration test results, if any
    script.junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml,**/target/surefire-reports/TEST-*.xml,**/target/jest-reports/TEST-*.xml'
  }

  void build() {
    mvn 'clean package -DskipTests'
    script.archiveArtifacts 'target/*.smp'
  }

  void sonarQube(){
    script.withSonarQubeEnv('sonarcloud.io-scm') {
      String sonar = "${script.env.SONAR_MAVEN_GOAL} -Dsonar.organization=scm-manager -Dsonar.branch.name=${script.env.BRANCH_NAME}"
      if (script.env.BRANCH_NAME != "master") {
        sonar += " -Dsonar.branch.target=master"
      }
      mvn sonar
    }
  }

  void deploy() {
    script.withCredentials([script.usernamePassword(credentialsId: 'maven.scm-manager.org', passwordVariable: 'serverPassword', usernameVariable: 'serverUsername')]) {
      script.writeFile file: ".m2/settings.xml", text: """
      <settings>
        <servers>
          <server>
            <id>packages.scm-manager.org</id>
            <username>${script.env.serverUsername}</username>
            <password>${script.env.serverPassword}</password>
          </server>
        </servers>
      </settings>
      """
    }
    
    mvn '-DskipTests -DaltReleaseDeploymentRepository=packages.scm-manager.org::default::https://packages.scm-manager.org/repository/plugin-releases/ -DaltSnapshotDeploymentRepository=packages.scm-manager.org::default::https://packages.scm-manager.org/repository/plugin-snapshots/ -s .m2/settings.xml deploy'
  }

  void mvn(String command) {
    script.sh "./mvnw --batch-mode -U -e  -DperformRelease -Dlicense.useDefaultExcludes=true -Dmaven.javadoc.failOnError=false ${command}"
  }

  String releaseDescriptorPath() {
    return "${script.env.WORKSPACE}/target/release.yaml"
  }

  void setVersionToNextSnapshot() {
    mvn "build-helper:parse-version versions:set -DgenerateBackupPoms=false -DnewVersion='\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}.0-SNAPSHOT'"
    mvn 'smp:fix'
    script.sh "git add pom.xml package.json"
  }

}
