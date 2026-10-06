pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk   'JDK 17'
    }

    environment {
        SONAR_TOKEN   = credentials('sonarcloud-token')
        SLACK_WEBHOOK = credentials('slack-webhook')
        SLACK_CHANNEL = '#pipeline-notificaciones'
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
        // 4. ANÁLISIS SONARCLOUD
        // ─────────────────────────────────────────────
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh """
                        mvn verify sonar:sonar \
                            -Dsonar.projectKey=LuisAngelSanchezRomero_35_PSW_pipeline_base \
                            -Dsonar.organization=luisangelsanchezromero \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.token=${SONAR_TOKEN}
                    """
                }
                echo '✅ Análisis SonarCloud completado'
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
            sh """
                curl -X POST -H 'Content-type: application/json' \
                --data '{"text":"✅ *Pipeline exitoso* — ${env.JOB_NAME} #${env.BUILD_NUMBER}\\n• Build: PASSED\\n• Tests: PASSED\\n• SonarCloud: analizado\\n• Ver: ${env.BUILD_URL}"}' \
                ${SLACK_WEBHOOK}
            """
        }
        failure {
            sh """
                curl -X POST -H 'Content-type: application/json' \
                --data '{"text":"❌ *Pipeline fallido* — ${env.JOB_NAME} #${env.BUILD_NUMBER}\\n• Ver: ${env.BUILD_URL}"}' \
                ${SLACK_WEBHOOK}
            """
        }
        unstable {
            sh """
                curl -X POST -H 'Content-type: application/json' \
                --data '{"text":"⚠️ *Pipeline inestable* — ${env.JOB_NAME} #${env.BUILD_NUMBER}\\n• Ver: ${env.BUILD_URL}"}' \
                ${SLACK_WEBHOOK}
            """
        }
    }
}
