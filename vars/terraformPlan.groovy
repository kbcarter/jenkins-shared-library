def call(String environment) {

    dir("environments/${environment}") {
      
      echo "Planning Terraform for ${environment}"

      sh '''
          terraform init \
            -input=false \
            -backend=true

          terraform plan \
            -out=tfplan
      '''

      archiveArtifacts(
        artifacts: 'tfplan',
        fingerprint: true
      )
    }

}
