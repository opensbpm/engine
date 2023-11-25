#!/usr/bin/env groovy

/* set basic job configurations */
properties([
        buildDiscarder(logRotator(numToKeepStr: '50', artifactDaysToKeepStr: '10')), 
        disableConcurrentBuilds(), 
        pipelineTriggers([snapshotDependencies()])
    ])

node('jdk11'){
    try{
        stage('Prepare'){        
            checkout scm
            
            if('master' != env.BRANCH_NAME){
                withMaven(
                    jdk: 'jdk11',
                    maven: 'default', 
                    mavenSettingsConfig: '05894f91-85e1-4e6d-8eb5-a101d90c62e3'
                ) {
                    updateQualifier()
                }
            }
        }
        
        stage('Assemble'){
            withMaven(
                jdk: 'jdk11',
                maven: 'default', 
                mavenSettingsConfig: '05894f91-85e1-4e6d-8eb5-a101d90c62e3'
            ) {
                sh "mvn -U -DskipTests clean install"
            }
        }
        
        stage('Test'){
            try{
                withMaven(
                    jdk: 'jdk11',
                    maven: 'default', 
                    mavenSettingsConfig: '05894f91-85e1-4e6d-8eb5-a101d90c62e3',
                    options: [junitPublisher(disabled:false),jacocoPublisher(disabled:false)]
                ) {
                    sh "mvn verify"
                }
            }finally{
                junit '**/target/*-reports/TEST-*.xml'
                jacoco execPattern: '**/target/jacoco*.exec'    
            }
        }
        
        stage('Static Analysis'){
            try{
                def model = readMavenPom(file: 'pom.xml')
                withMaven(
                    jdk: 'jdk11',
                    maven: 'default', 
                    mavenSettingsConfig: '05894f91-85e1-4e6d-8eb5-a101d90c62e3',
                    options: [
                        openTasksPublisher(highPriorityTaskIdentifiers: 'FIXME', lowPriorityTaskIdentifiers: 'TODO', normalPriorityTaskIdentifiers: 'PENDING', pattern: '**/*.*',excludePattern: '**/target/**')
                    ]
                ) {
                    //'JDK 1.8' is need for sonarqube (Hostname not verified (no certificates))
                    withSonarQubeEnv('Sonarqube') {
                        sh "mvn -DskipTests \
                                -Dsonar.projectKey=${model.getGroupId()}:${model.getArtifactId()}:${BRANCH_NAME.replace('/',"-")} \
                                -Dsonar.projectName=\"${model.getName()} ($BRANCH_NAME)\" \
                                pmd:cpd pmd:pmd sonar:sonar"
                    }
                }
            }finally{
                recordIssues (enabledForFailure: true, 
                    tools: [
                        java(),
                        javaDoc(),
                        cpd(pattern: '**/target/cpd.xml'), 
                        pmdParser(pattern: '**/target/pmd.xml')
                    ])
                timeout(time: 15, unit: 'MINUTES') {
                    def qg = waitForQualityGate()
                    if (qg.status == 'ERROR') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }else if (qg.status == 'WARN') {
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Deploy'){
            retry(3) {
                withMaven(jdk: 'jdk11',
                    maven: 'default', 
                    mavenSettingsConfig: '05894f91-85e1-4e6d-8eb5-a101d90c62e3') {
                    sh "mvn -DskipTests deploy"
                }    
            }
        }
        
        //        stage('Site'){
        //            withMaven(jdk: 'jdk11',
        //                maven: 'default', 
        //                mavenSettingsConfig: '05894f91-85e1-4e6d-8eb5-a101d90c62e3') {
        //                sh "mvn site site:stage"
        //                sh "mvn scm-publish:publish-scm"
        //            }
        //        }
        
        currentBuild.result = 'SUCCESS'
    }catch(ex){
        if(currentBuild.result == null){
            currentBuild.result = 'FAILED'
        }
        throw ex
    }finally{
        recordIssues enabledForFailure: true, tools: [
            mavenConsole()
        ]        
        emailext (recipientProviders: [culprits()], 
            subject: "OpenSBPM Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' ${currentBuild.result}",
            body: """
                <p>${currentBuild.result}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>\n\
            """,
            attachLog: true
        )
    }
}

@NonCPS
def updateQualifier(){
    def qualifier
    def matcher= (env.BRANCH_NAME =~ /^feature\/(.*)/)
    if(!matcher){
        qualifier = "SNAPSHOT"
    }else {
        qualifier = "${matcher[0][1]}-SNAPSHOT"
    }
    println "update pom-versions with qualifier ${qualifier}"
    sh "mvn build-helper:parse-version versions:set \
            -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}-${qualifier} \
            -DoldVersion='*' -DprocessAllModules=true -DgenerateBackupPoms=false "
}
