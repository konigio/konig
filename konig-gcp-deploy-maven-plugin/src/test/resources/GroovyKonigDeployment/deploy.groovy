@Grab('io.konig:konig-gcp-deploy-maven-plugin:2.0.0-8')

import static io.konig.deploy.ResourceType.*;
import io.konig.maven.deploy.KonigDeployment;

def deploymentPlan = {
	create BigQueryDataset from "dataset.json"
	create BigQueryTable from "schema.Person.json"
}
deploymentPlan.delegate = new KonigDeployment("C:/Users/Greg/auth/gmcfall-edw-core.json", "C:/Users/Greg/git/konig/konig-gcp-deploy-maven-plugin/src/test/resources/GroovyKonigDeployment")
deploymentPlan()