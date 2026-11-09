pipeline {
    agent {
        docker {
            image 'ghcr.io/tailoringexpert/maven:3.9-eclipse-25'
            args '''
                --entrypoint='' \
                -u 501:1000 \
                -v $PWD:/data \
                -v $M2_VOLUME:/home/maven \
                -e MAVEN_CREDENTIALS_USR=$MAVEN_CREDENTIALS_USR \
                -e MAVEN_CREDENTIALS_PSW=$MAVEN_CREDENTIALS_PSW
            '''
            reuseNode true
       }
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '3', numToKeepStr: '3'))
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(
            name: 'RELEASE_BUILD',
            defaultValue: false,
            description: 'Create a release')
        booleanParam(
            name: 'DEPLOY',
            defaultValue: false,
            description: 'Execute deployment step')
        gitParameter(
            name: 'BRANCH',
            branch: '',
            defaultValue: 'develop',
            branchFilter: 'origin/(.*)',
            selectedValue: 'NONE',
            sortMode: 'NONE',
            type: 'PT_BRANCH',
            description: 'Select target branch')
    }

    environment {
        GIT_REPOSITORY = 'tailoringexpert/platform.git' 
        
        GIT_CREDENTIALS_ID = 'TAILORINGEXPERT_GITHUB_CREDENTIALS'
        GIT_CREDENTIALS    = credentials('TAILORINGEXPERT_GITHUB_CREDENTIALS')

        MAVEN_CREDENTIALS = credentials('MAVEN_SONATYPE_CREDENTIALS')
        MAVEN_CREDENTIALS_USR = "${env.MAVEN_CREDENTIALS_USR}"
        MAVEN_CREDENTIALS_PSW = "${env.MAVEN_CREDENTIALS_PSW}"
        MAVEN_GPG_SIGNKEY_BASE64 = credentials('MAVEN_SONATYPE_GPG_SIGNKEY')
        MAVEN_GPG_PASSPHRASE  = credentials('MAVEN_SONATYPE_GPG_PASSPHRASE')

        // other (external) defined env vars
        // M2_VOLUME maven      repoository volume
        // GPG_VOLUME           gpg key volume
        // GIT_COMMITTER_NAME   name of the git committer
        // GIT_COMMITTER_EMAIL  mail of the git committer
        // MAVEN_SNAPSHOTURL    url to deploy snapshots to
        // MAVEN_RELEASEURL     url to deploy releases to
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
                sh '''
                    mvn \
                        --settings ./settings.xml \
                        -Dmaven.repo.local=/home/maven \
                        -DskipTests \
                    clean compile
                '''    
            }
        }

        stage('verify') {
            steps {
                sh '''
                    mvn --settings ./settings.xml \
                        -Dmaven.repo.local=/home/maven \
                    verify
                '''  
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
                    sh '''
                        mvn \
                            --settings ./settings.xml \
                            -Dmaven.repo.local=/home/maven \
                        sonar:sonar
                    '''
                }
              timeout(time: 1, unit: 'HOURS') {
                waitForQualityGate abortPipeline: true, credentialsId: '${SONAR_TOKEN}'
              }
            }
          }

        stage('install') {
            steps {
                sh '''
                    mvn \
                        --settings ./settings.xml \
                        -Dmaven.repo.local=/home/maven \
                        -DskipTests \
                    install
                '''

            }
        }

        stage('release') {
            when {
                expression { params.RELEASE_BUILD }
            }

            steps {
                sh '''
                    setup-git.sh            
				    mvn --settings ./settings.xml -B \
                        -Dresume=false \
                        -DargLine='-DprocessAllModules --settings ./settings.xml' 
                        -DskipTestProject=true 
                        -DgpgSignTag=false 
                        -DgpgSignCommit=false 
                    gitflow:release
                '''
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
                    if (params.RELEASE_BUILD) {
                        git branch: "main", url: env.GIT_URL, credentialsId: GIT_CREDENTIALS_ID
                    }

                    if (params.DEPLOY || params.RELEASE_BUILD) {
                        sh '''
                            setup-maven-gpg.sh
						    mvn \
                                --settings ./settings.xml \
                                -Dmaven.repo.local=/home/maven \
                                -DskipTests \
                                -Dgpg.passphrase="${MAVEN_GPG_PASSPHRASE}" \
                                -Dgpg.arguments="--batch --no-tty --pinentry-mode loopback" \
                                deploy
                        '''

                        // Optionaler Deploy in ein weiteres Repository via Profil
                        if (params.DEPLOY_TO_CUSTOM_REPOSITORY) {
                            sh '''
                                mvn \
                                    --settings ./settings.xml \
                                    -Dmaven.repo.local=/home/maven \
                                    -DskipTests \
                                    -P '!default,custom-maven' \
                                deploy
                            '''                                
                        }
                    } else {
                        sh 'exit 0'
                    }
                }
            }
        }
    }
}
