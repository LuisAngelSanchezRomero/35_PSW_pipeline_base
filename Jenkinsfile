pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk   'JDK 17'
    }

    environment {
        SONAR_TOKEN  = credentials('sonar-token')
        SLACK_CHANNEL = '#psw-pipeline'
    }

    stages {

        // ─────────────────────────────────────────────
        // 1. CHECKOUT
        // ─────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo '✅ Código descargado correctamente'
            }
        }

        // ─────────────────────────────────────────────
        // 2. BUILD
        // ─────────────────────────────────────────────
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
                echo '✅ Build completado'
            }
        }

        // ─────────────────────────────────────────────
        // 3. TEST + COVERAGE (JaCoCo)
        // ─────────────────────────────────────────────
        stage('Test') {
            steps {
                sh 'mvn test'
                echo '✅ Pruebas ejecutadas'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        inclusionPattern: '**/*.class'
                    )
                }
            }
        }

        // ─────────────────────────────────────────────
        // 4. ANÁLISIS SONARQUBE
        // ─────────────────────────────────────────────
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=psw-pipeline-base \
                            -Dsonar.projectName='PSW Pipeline Base' \
                            -Dsonar.token=${SONAR_TOKEN}
                    """
                }
                echo '✅ Análisis SonarQube completado'
            }
        }

        // ─────────────────────────────────────────────
        // 5. QUALITY GATE (opcional pero recomendado)
        // ─────────────────────────────────────────────
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        // ─────────────────────────────────────────────
        // 6. PRUEBAS DE CARGA CON JMETER
        // ─────────────────────────────────────────────
        stage('JMeter Load Test') {
            steps {
                // Asegúrate de tener JMETER_HOME configurado en Jenkins
                // o usa la ruta absoluta de JMeter en tu servidor
                sh """
                    ${JMETER_HOME}/bin/jmeter \
                        -n \
                        -t jmeter/psw-pipeline-test.jmx \
                        -l jmeter/results/results.jtl \
                        -e \
                        -o jmeter/results/report
                """
                echo '✅ Pruebas de carga completadas'
            }
            post {
                always {
                    perfReport sourceDataFiles: 'jmeter/results/results.jtl'
                }
            }
        }

    }

    // ─────────────────────────────────────────────────
    // NOTIFICACIONES SLACK
    // ─────────────────────────────────────────────────
    post {
        success {
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'good',
                message: """
✅ *Pipeline exitoso* — ${env.JOB_NAME} #${env.BUILD_NUMBER}
• Build: PASSED
• Tests: PASSED
• SonarQube: analizado
• JMeter: ejecutado
• Ver detalles: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
        failure {
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'danger',
                message: """
❌ *Pipeline fallido* — ${env.JOB_NAME} #${env.BUILD_NUMBER}
• Etapa fallida: ${currentBuild.currentResult}
• Ver detalles: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
        unstable {
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'warning',
                message: """
⚠️ *Pipeline inestable* — ${env.JOB_NAME} #${env.BUILD_NUMBER}
• Algunos tests fallaron o Quality Gate no pasó
• Ver detalles: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
    }
}
