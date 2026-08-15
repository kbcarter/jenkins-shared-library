def call(Map config = [:]) {

  String application = config.application
  List environments = config.environments ?: ['dev', 'test', 'prod']

  if (!application) {
    error("terraformPipeline: 'application' is required!")
  }

  pipeline {
    agent any

    options {
      timestamps()
      disableConcurrentBuilds()
      ansiColor('xterm')
    }

    tools {
      terraform 'terraform-1.15'
    }

    environment {
      TF_IN_AUTOMATION = 'true'
      TF_INPUT = 'false'
    }

    stages {

      stage('Terraform Format') {
        steps {
          sh 'terraform fmt -check -recursive'
        }
      }
      
      stage('Terrform Validate') {
        steps {
          script {
            environments.each { envName ->
                terraformValidate(envName)
            }
          }
        }
      }

      stage('Terraform Plan') {
        steps {
          script {
            echo "Configured environments: ${environments}"

            environments.each { envName ->
                terraformPlan(envName)
            }
          }
        }
      }

      stage('Deploy Dev') {
        when {
          allOf {
            branch 'main'
            environments.contains('dev')
          }
        }

        steps {
          terraformApply('dev')
        }
      }

      stage('Integration Tests') {
        when {
          branch 'main'
        }

        steps {
          echo "Running intergration tests for ${application}"
        }
      }
      
      stage('Deploy Test') {
        when {
          allOf {
            branch 'main'
            environments.contains('test')
          }
        }

        steps {
          terraformApply('test')
        }
      }

      stage('Deploy Prod') {
        when {
          allOf {
            branch 'main'
            environments.contains('prod')
          }
        }

        steps {
          input(
            message: "Deploy ${application} to production?",
            ok: 'Deploy',
          )

          terraformApply('prod')
        }
      }
    }
  }

}
