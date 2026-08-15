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
          branch 'main'
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
        steps {
          terraformApply('test')
        }
      }

      stage('Production Approval') {
        steps {
          input(
            message: "Deploy ${application} to production?",
            ok: 'Deploy',
            submitter: 'platform-team'
          )
        }
      }

      stage('Deploy Prod') {
        when {
          branch 'main'
        }

        steps {
          terraformApply('prod')
        }
      }
    }
  }

}
