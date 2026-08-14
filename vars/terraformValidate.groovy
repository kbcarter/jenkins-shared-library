def call(String environment) {
  
  dir("environments/${environment}") {

    echo "Validating Terraform for ${environment}"

    sh '''
        terraform init \
        -input=false \
        -backend=true

        terraform validate
    '''
  }
}
