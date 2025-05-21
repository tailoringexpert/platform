properties([
    parameters([
        booleanParam(
            name: 'RELEASE_BUILD',
            defaultValue: false),
        booleanParam(
            name: 'DEPLOY',
            defaultValue: false),
        gitParameter(
            name: 'BRANCH',
            branch: '',
            defaultValue: 'develop',
            branchFilter: 'origin/(.*)',
            selectedValue: 'NONE',
            sortMode: 'NONE',
            type: 'PT_BRANCH')
    ])
])

pipeline {

    environment {
        GIT_CREDENTIALS_ID = 'TAILORINGEXPERT_GITHUB_CREDENTIALS'
        GIT_CREDENTIALS = credentials('TAILORINGEXPERT_GITHUB_CREDENTIALS')
        GPG_SIGNKEY = credentials('GITHUB_GPG_SIGNKEY')
        NEXUS_CREDENTIALS = credentials('NEXUS_CREDENTIALS')
        SONAR_TOKEN = credentials('TAILORINGEXPERT_SONAR_TOKEN')
        GIT_REPOSITORY = 'tailoringexpert/platform.git' 
        MVN_SKIP_MODULES = '--projects !tailoringexpert-distribution'
        
        // other (external) defined env vars
        // M2_VOLUME maven      repoository volume
        // GPG_VOLUME           gpg key volume
        // GIT_COMMITTER_NAME   name of the git committer
        // GIT_COMMITTER_EMAIL  mail of the git committer
        // NEXUS_SNAPSHOTURL    url to deploy snapshots to
        // NEXUS_RELEASEURL     url to deploy releases to
    }

    agent {
        docker {
            image 'tailoringexpert/maven:3.9-eclipse-23'
            args '''  
                -u 501:1000
                -v $GPG_VOLUME:/.gnupg \
                -v $SONAR_USER_HOME:/.sonar \
                -v $PWD:/data \
                -v $M2_VOLUME:/home/maven \
                -e GIT_CREDENTIALS=$GIT_CREDENTIALS \
                -e GIT_COMMITTER_NAME=$GIT_COMMITTER_NAME \
                -e GIT_COMMITTER_EMAIL=$GIT_COMMITTER_EMAIL \
                -e NEXUS_SNAPSHOTURL=$NEXUS_SNAPSHOTURL \
                -e NEXUS_RELEASEURL=$NEXUS_RELEASEURL \
                -e NEXUS_CREDENTIALS_USR=$NEXUS_CREDENTIALS_USR \
                -e NEXUS_CREDENTIALS_PSW=$NEXUS_CREDENTIALS_PSW \
                -e GPG_SIGNKEY=$GPG_SIGNKEY \
                -e SONAR_TOKEN=$SONAR_TOKEN 
            '''
            reuseNode true
       }       
    }
    
    options {
        buildDiscarder(logRotator(daysToKeepStr: '3', numToKeepStr: '3'))
    }

    stages {
        stage('checkout') {
             steps {
                script {
                    checkoutBranch = params.RELEASE_BUILD ? 'develop' : params.BRANCH
                    currentBuild.displayName = "#${env.BUILD_ID} | " + (params.RELEASE_BUILD ?  "RELEASE " : ("${checkoutBranch}" + (params.DEPLOY ? " (deploy)" : "")))
                }
                sh "echo checking out ${checkoutBranch}"             
             
                cleanWs()
                git branch: "${checkoutBranch}", url: env.GIT_URL, credentialsId: GIT_CREDENTIALS_ID
             }
        }

        stage('build') {
            steps {
                sh "mvn --settings .jenkins/settings.xml -Dmaven.repo.local=${M2_VOLUME}/repository -DskipTests ${MVN_SKIP_MODULES} clean compile"
            }
        }

        stage('verify') {
            steps {
                sh "mvn --settings .jenkins/settings.xml -Dmaven.repo.local=${M2_VOLUME}/repository ${MVN_SKIP_MODULES} verify"
            }

            post {
                success {
                    recordCoverage (                        
                        tools: [
                            [
                                parser: 'JACOCO'
                            ]
                        ],
                        qualityGates: [
                            [
                                criticality: 'NOTE', 
                                integerThreshold: 98, 
                                metric: 'INSTRUCTION', 
                                threshold: 98.0
                            ], 
                            [
                                criticality: 'NOTE', 
                                integerThreshold: 98, 
                                metric: 'BRANCH', 
                                threshold: 98.0
                            ], 
                            [
                                criticality: 'NOTE', 
                                integerThreshold: 100, 
                                metric: 'METHOD', 
                                threshold: 98.0
                            ], 
                            [
                                criticality: 'NOTE', 
                                integerThreshold: 98, 
                                metric: 'LINE', 
                                threshold: 98.0
                            ]
                        ] 
                    )
                }
            }
        }

        stage("quality gate") {
            steps {
                withSonarQubeEnv('default') {
                    sh "mvn --settings .jenkins/settings.xml -Dmaven.repo.local=${M2_VOLUME}/repository ${MVN_SKIP_MODULES} sonar:sonar"
                }
              timeout(time: 1, unit: 'HOURS') {
                waitForQualityGate abortPipeline: true, credentialsId: '${SONAR_TOKEN}'
              }
            }
          }

        stage('install') {
            steps {
                sh "mvn --settings .jenkins/settings.xml -Dmaven.repo.local=${M2_VOLUME}/repository -DskipTests install"
            }
        }

        stage('release') {
            when {
                expression { params.RELEASE_BUILD }
            }

            steps {
                // prepare git signing
                sh('git remote set-url origin https://$GIT_CREDENTIALS@github.com/$GIT_REPOSITORY')
                sh('git config user.name "$GIT_COMMITTER_NAME"')
                sh('git config user.email $GIT_COMMITTER_EMAIL')
                sh('git config commit.gpgsign true')
                sh('git config user.signingkey $GPG_SIGNKEY')
                
                sh "mvn --settings .jenkins/settings.xml -Dmaven.repo.local=${M2_VOLUME}/repository -B -Dresume=false -DargLine='-DprocessAllModules --settings .jenkins/settings.xml -Dmaven.repo.local=/home/maven/.m2 --settings .jenkins/settings.xml' -DskipTestProject=true  -DgpgSignTag=true -DgpgSignCommit=true -DpostReleaseGoals=deploy gitflow:release" 

                // remove credentials
                sh('git remote set-url origin $GIT_URL')
            }
        }

        stage('deploy') {   
            when {
                anyOf {
                    expression { params.RELEASE_BUILD };
                    expression { params.DEPLOY }
                }
            }
                    
            steps {
                script {
                    if (params.DEPLOY) {
                        sh "mvn --settings .jenkins/settings.xml -Dmaven.repo.local=${M2_VOLUME}/repository -DskipTests deploy"
                    } else {
                        sh 'exit 0'
                    }
                }
            }
        }
    }
}
