def call(String environment) {

    dir("environments/${environment}") {
      
      echo "Planning Terraform for ${environment}"

      sh '''
          terraform init \
            -input=false \
            -backend=true

          terraform apply \
            -input=false \
            tfplan
      '''
    }

}

