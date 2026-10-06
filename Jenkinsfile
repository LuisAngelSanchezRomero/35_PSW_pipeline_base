pipeline {
    agent any

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
                bat 'mvn clean package -DskipTests'
                echo '✅ Build completado'
            }
        }

        // ─────────────────────────────────────────────
        // 3. TEST + COVERAGE (JaCoCo)
        // ─────────────────────────────────────────────
        stage('Test') {
            steps {
                bat 'mvn test'
                echo '✅ Pruebas ejecutadas'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ─────────────────────────────────────────────
        // 4. ANÁLISIS SONARCLOUD
        // ─────────────────────────────────────────────
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    bat """
                        mvn verify sonar:sonar ^
                            -Dsonar.projectKey=LuisAngelSanchezRomero_35_PSW_pipeline_base ^
                            -Dsonar.organization=luisangelsanchezromero ^
                            -Dsonar.host.url=https://sonarcloud.io ^
                            -Dsonar.token=%SONAR_TOKEN%
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
                bat """
                    C:\\apache-jmeter-5.6.3\\bin\\jmeter.bat ^
                        -n ^
                        -t jmeter\\psw-pipeline-test.jmx ^
                        -l jmeter\\results\\results.jtl ^
                        -e ^
                        -o jmeter\\results\\report
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
            bat """
                curl -X POST -H "Content-type: application/json" --data "{\\"text\\":\\"✅ *Pipeline exitoso* — %JOB_NAME% #%BUILD_NUMBER%\\\\n• Build: PASSED\\\\n• Tests: PASSED\\\\n• SonarCloud: analizado\\\\n• JMeter: ejecutado\\\\n• Ver: %BUILD_URL%\\"}" %SLACK_WEBHOOK%
            """
        }
        failure {
            bat """
                curl -X POST -H "Content-type: application/json" --data "{\\"text\\":\\"❌ *Pipeline fallido* — %JOB_NAME% #%BUILD_NUMBER%\\\\n• Ver: %BUILD_URL%\\"}" %SLACK_WEBHOOK%
            """
        }
        unstable {
            bat """
                curl -X POST -H "Content-type: application/json" --data "{\\"text\\":\\"⚠️ *Pipeline inestable* — %JOB_NAME% #%BUILD_NUMBER%\\\\n• Ver: %BUILD_URL%\\"}" %SLACK_WEBHOOK%
            """
        }
    }
}
