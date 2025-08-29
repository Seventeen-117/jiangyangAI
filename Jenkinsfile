pipeline {
    agent any
    
    parameters {
        choice(
            name: 'TEST_ENVIRONMENT',
            choices: ['local', 'test', 'staging', 'production'],
            description: '选择测试环境'
        )
        string(
            name: 'TEST_SERVICES',
            defaultValue: '',
            description: '要测试的服务 (逗号分隔，留空测试所有服务)'
        )
        booleanParam(
            name: 'PARALLEL_EXECUTION',
            defaultValue: false,
            description: '是否并行执行测试'
        )
        booleanParam(
            name: 'SKIP_UNIT_TESTS',
            defaultValue: false,
            description: '跳过单元测试'
        )
        booleanParam(
            name: 'RUN_PERFORMANCE_TESTS',
            defaultValue: false,
            description: '运行性能测试'
        )
    }
    
    environment {
        JAVA_HOME = tool('JDK17')
        MAVEN_HOME = tool('Maven3')
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
        ALLURE_HOME = tool('Allure')
        
        // 测试配置
        TEST_ENV = "${params.TEST_ENVIRONMENT}"
        TEST_PARALLEL = "${params.PARALLEL_EXECUTION}"
        TEST_SERVICES = "${params.TEST_SERVICES}"
        
        // 数据库配置
        MYSQL_URL = credentials('test-mysql-url')
        MYSQL_USER = credentials('test-mysql-user')
        MYSQL_PASSWORD = credentials('test-mysql-password')
        
        // Redis配置
        REDIS_URL = credentials('test-redis-url')
        
        // 通知配置
        SLACK_WEBHOOK = credentials('slack-webhook-url')
        EMAIL_RECIPIENTS = credentials('test-email-recipients')
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        retry(1)
    }
    
    stages {
        stage('环境准备') {
            steps {
                script {
                    echo "=== 开始江阳AI微服务集成测试 ==="
                    echo "测试环境: ${TEST_ENV}"
                    echo "并行执行: ${TEST_PARALLEL}"
                    echo "测试服务: ${TEST_SERVICES ?: '全部服务'}"
                    
                    // 设置构建描述
                    currentBuild.description = "环境: ${TEST_ENV} | 并行: ${TEST_PARALLEL}"
                }
                
                // 检出代码
                checkout scm
                
                // 检查Java和Maven版本
                sh '''
                    echo "Java版本:"
                    java -version
                    echo "Maven版本:"
                    mvn -version
                '''
            }
        }
        
        stage('代码质量检查') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            
            parallel {
                stage('编译检查') {
                    steps {
                        dir('test-service') {
                            sh 'mvn clean compile -DskipTests'
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'test-service/target/site',
                                reportFiles: 'index.html',
                                reportName: '编译报告'
                            ])
                        }
                    }
                }
                
                stage('代码规范检查') {
                    steps {
                        dir('test-service') {
                            sh '''
                                mvn checkstyle:check || true
                                mvn spotbugs:check || true
                            '''
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'test-service/target/site',
                                reportFiles: 'checkstyle.html',
                                reportName: '代码规范报告'
                            ])
                            
                            recordIssues(
                                enabledForFailure: true,
                                tools: [
                                    checkStyle(pattern: 'test-service/target/checkstyle-result.xml'),
                                    spotBugs(pattern: 'test-service/target/spotbugsXml.xml')
                                ]
                            )
                        }
                    }
                }
            }
        }
        
        stage('单元测试') {
            when {
                not { params.SKIP_UNIT_TESTS }
            }
            
            steps {
                dir('test-service') {
                    sh '''
                        mvn test \
                            -Dtest=*UnitTest \
                            -DfailIfNoTests=false
                    '''
                }
            }
            
            post {
                always {
                    publishTestResults testResultsPattern: 'test-service/target/surefire-reports/*.xml'
                    
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'test-service/target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: '单元测试覆盖率报告'
                    ])
                }
            }
        }
        
        stage('服务健康检查') {
            steps {
                script {
                    echo "检查测试环境服务健康状态..."
                    
                    def services = [
                        'gateway-service': '8080',
                        'bgai-service': '8688',
                        'signature-service': '8689',
                        'messages-service': '8687',
                        'chat-agent': '8690',
                        'deepSearch-service': '8691'
                    ]
                    
                    def healthCheckResults = [:]
                    
                    services.each { serviceName, port ->
                        try {
                            def baseUrl = getServiceBaseUrl(TEST_ENV)
                            def healthUrl = "${baseUrl}:${port}/actuator/health"
                            
                            def response = sh(
                                script: "curl -s -o /dev/null -w '%{http_code}' --connect-timeout 10 ${healthUrl} || echo '000'",
                                returnStdout: true
                            ).trim()
                            
                            healthCheckResults[serviceName] = response
                            
                            if (response == '200') {
                                echo "✅ ${serviceName} 健康检查通过"
                            } else {
                                echo "⚠️ ${serviceName} 健康检查失败，状态码: ${response}"
                            }
                            
                        } catch (Exception e) {
                            echo "❌ ${serviceName} 健康检查异常: ${e.message}"
                            healthCheckResults[serviceName] = 'ERROR'
                        }
                    }
                    
                    // 保存健康检查结果
                    writeJSON file: 'health-check-results.json', json: healthCheckResults
                    archiveArtifacts artifacts: 'health-check-results.json'
                }
            }
        }
        
        stage('集成测试') {
            parallel {
                stage('签名验证服务') {
                    when {
                        anyOf {
                            params.TEST_SERVICES == ''
                            params.TEST_SERVICES.contains('signature-service')
                        }
                    }
                    steps {
                        runServiceTests('signature-service')
                    }
                    post {
                        always {
                            publishServiceTestResults('signature-service')
                        }
                    }
                }
                
                stage('网关服务') {
                    when {
                        anyOf {
                            params.TEST_SERVICES == ''
                            params.TEST_SERVICES.contains('gateway-service')
                        }
                    }
                    steps {
                        runServiceTests('gateway-service')
                    }
                    post {
                        always {
                            publishServiceTestResults('gateway-service')
                        }
                    }
                }
                
                stage('AI核心服务') {
                    when {
                        anyOf {
                            params.TEST_SERVICES == ''
                            params.TEST_SERVICES.contains('bgai-service')
                        }
                    }
                    steps {
                        runServiceTests('bgai-service')
                    }
                    post {
                        always {
                            publishServiceTestResults('bgai-service')
                        }
                    }
                }
                
                stage('消息服务') {
                    when {
                        anyOf {
                            params.TEST_SERVICES == ''
                            params.TEST_SERVICES.contains('messages-service')
                        }
                    }
                    steps {
                        runServiceTests('messages-service')
                    }
                    post {
                        always {
                            publishServiceTestResults('messages-service')
                        }
                    }
                }
                
                stage('聊天代理') {
                    when {
                        anyOf {
                            params.TEST_SERVICES == ''
                            params.TEST_SERVICES.contains('chat-agent')
                        }
                    }
                    steps {
                        runServiceTests('chat-agent')
                    }
                    post {
                        always {
                            publishServiceTestResults('chat-agent')
                        }
                    }
                }
                
                stage('深度搜索') {
                    when {
                        anyOf {
                            params.TEST_SERVICES == ''
                            params.TEST_SERVICES.contains('deepSearch-service')
                        }
                    }
                    steps {
                        runServiceTests('deepSearch-service')
                    }
                    post {
                        always {
                            publishServiceTestResults('deepSearch-service')
                        }
                    }
                }
            }
        }
        
        stage('端到端测试') {
            when {
                anyOf {
                    params.TEST_SERVICES == ''
                    params.TEST_SERVICES.contains('e2e')
                }
            }
            
            steps {
                dir('test-service') {
                    sh '''
                        mvn test \
                            -Dtest.env=${TEST_ENV} \
                            -Dtest.parallel=${TEST_PARALLEL} \
                            -Dtest.retries=2 \
                            -Dtest=JiangYangAIIntegrationTests#testEndToEndScenarios
                    '''
                }
            }
            
            post {
                always {
                    publishTestResults testResultsPattern: 'test-service/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('性能测试') {
            when {
                params.RUN_PERFORMANCE_TESTS
            }
            
            steps {
                dir('test-service') {
                    sh '''
                        mvn test \
                            -Dtest.env=${TEST_ENV} \
                            -Dtest.include.tags=performance \
                            -Dtest.parallel=true \
                            -Dtest.timeout=300000
                    '''
                }
            }
            
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'test-service/target/allure-report',
                        reportFiles: 'index.html',
                        reportName: '性能测试报告'
                    ])
                }
            }
        }
        
        stage('生成测试报告') {
            steps {
                dir('test-service') {
                    sh '''
                        # 生成Allure报告
                        ${ALLURE_HOME}/bin/allure generate target/allure-results --clean -o target/allure-report
                        
                        # 生成测试摘要
                        echo "=== 测试执行摘要 ===" > test-summary.txt
                        echo "测试时间: $(date)" >> test-summary.txt
                        echo "测试环境: ${TEST_ENV}" >> test-summary.txt
                        echo "构建编号: ${BUILD_NUMBER}" >> test-summary.txt
                        echo "Git提交: ${GIT_COMMIT}" >> test-summary.txt
                        
                        # 统计测试结果
                        if [ -d "target/allure-results" ]; then
                            TOTAL_TESTS=$(find target/allure-results -name "*-result.json" | wc -l)
                            echo "总测试数: ${TOTAL_TESTS}" >> test-summary.txt
                        fi
                    '''
                }
            }
            
            post {
                always {
                    // 发布Allure报告
                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'test-service/target/allure-results']]
                    ])
                    
                    // 归档测试报告
                    archiveArtifacts artifacts: 'test-service/target/allure-report/**/*', fingerprint: true
                    archiveArtifacts artifacts: 'test-service/test-summary.txt'
                    
                    // 发布HTML报告
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'test-service/target/allure-report',
                        reportFiles: 'index.html',
                        reportName: '集成测试报告'
                    ])
                }
            }
        }
    }
    
    post {
        always {
            script {
                def testResult = currentBuild.result ?: 'SUCCESS'
                def color = testResult == 'SUCCESS' ? 'good' : 'danger'
                def emoji = testResult == 'SUCCESS' ? '✅' : '❌'
                
                def message = """
                ${emoji} 江阳AI微服务集成测试完成
                
                *环境*: ${TEST_ENV}
                *构建*: #${BUILD_NUMBER}
                *结果*: ${testResult}
                *持续时间*: ${currentBuild.durationString}
                *测试服务*: ${TEST_SERVICES ?: '全部服务'}
                
                *报告链接*: ${BUILD_URL}allure
                """.stripIndent()
                
                // 发送Slack通知
                try {
                    slackSend(
                        channel: '#test-results',
                        color: color,
                        message: message,
                        webhookToken: SLACK_WEBHOOK
                    )
                } catch (Exception e) {
                    echo "Slack通知发送失败: ${e.message}"
                }
                
                // 发送邮件通知
                try {
                    emailext(
                        subject: "${emoji} 江阳AI集成测试 - ${testResult}",
                        body: message,
                        to: EMAIL_RECIPIENTS,
                        attachmentsPattern: 'test-service/test-summary.txt'
                    )
                } catch (Exception e) {
                    echo "邮件通知发送失败: ${e.message}"
                }
            }
        }
        
        success {
            echo "🎉 所有测试成功完成！"
        }
        
        failure {
            echo "💥 测试执行失败，请检查日志和报告"
        }
        
        cleanup {
            // 清理测试数据
            sh '''
                echo "清理测试环境..."
                # 这里可以添加测试数据清理脚本
            '''
            
            // 清理工作空间
            cleanWs()
        }
    }
}

// 辅助函数
def runServiceTests(serviceName) {
    dir('test-service') {
        sh """
            mvn test \
                -Dtest.env=${TEST_ENV} \
                -Dtest.parallel=${TEST_PARALLEL} \
                -Dtest.include.tags=${serviceName} \
                -Dtest=JiangYangAIIntegrationTests#test*Service
        """
    }
}

def publishServiceTestResults(serviceName) {
    publishTestResults testResultsPattern: 'test-service/target/surefire-reports/*.xml'
    
    publishHTML([
        allowMissing: true,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: "test-service/target/allure-report-${serviceName}",
        reportFiles: 'index.html',
        reportName: "${serviceName} 测试报告"
    ])
}

def getServiceBaseUrl(environment) {
    def urls = [
        'local': 'http://localhost',
        'test': 'http://test.jiangyang.com',
        'staging': 'http://staging.jiangyang.com',
        'production': 'https://api.jiangyang.com'
    ]
    return urls[environment] ?: 'http://localhost'
}