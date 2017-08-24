package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.GetQueryResults;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationQuery;
import com.google.api.services.bigquery.model.TableReference;

import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BigQueryMetricComputationServiceTest {

	@SuppressWarnings("unused")
	@Ignore
	public void test() throws Exception {
		HttpTransport httpTransport = new NetHttpTransport();

		GoogleCredential credentials = GoogleCredential.getApplicationDefault();
		if (credentials.createScopedRequired()) {
			credentials = credentials.createScoped(BigqueryScopes.all());
		}
		Bigquery.Builder serviceBuilder = new Bigquery.Builder(httpTransport, new JacksonFactory(), credentials);

		Bigquery service = serviceBuilder.build();

		TableReference tableRef = new TableReference().setProjectId("mypedia-dev-55669").setDatasetId("fact")
				.setTableId("StartAssessmentUniqueCountByGrade_copy");

		JobConfigurationQuery queryConfig = new JobConfigurationQuery().setQuery(readFile())
				.setDestinationTable(tableRef).setAllowLargeResults(true).setUseLegacySql(false).setPriority("BATCH")
				.setWriteDisposition("WRITE_TRUNCATE");

		Job injob = new Job().setConfiguration(new JobConfiguration().setQuery(queryConfig));
		Job job = service.jobs().insert("mypedia-dev-55669", injob).execute();
		Bigquery.Jobs.Get getRequest = service.jobs().get("mypedia-dev-55669", job.getJobReference().getJobId());

		System.out.println(job.toPrettyString());
		if (job == null) {
			throw new Exception("Job is null");
		}

		Job jobre = getRequest.execute();
		while (!jobre.getStatus().getState().equals("DONE")) {
			System.out.println("Job is " + jobre.getStatus().getState() + " waiting  milliseconds...");
			Thread.sleep(1000);
			jobre = getRequest.execute();
		}
		System.out.println(jobre.getStatus().getState());

		GetQueryResults resultsRequest = service.jobs().getQueryResults("mypedia-dev-55669",
				job.getJobReference().getJobId());
		System.out.println(resultsRequest.toString());

		ErrorProto errorResult = job.getStatus().getErrorResult();

		if (errorResult != null) {
			throw new Exception("Error running job: " + errorResult);
		}
	}

	private String readFile() throws Exception {
		try {
			return IOUtils.toString(
					new FileInputStream("src/test/resources/BigQueryMetrics/StartAssessmentUniqueCountByGrade.sql"));
		} catch (Exception e) {
			throw (e);
		}
	}
	
	@Test
	public void testShape() {
		ShapeManager shapeManager = new MemoryShapeManager();
		System.out.println(shapeManager.getShapeById(new URIImpl("http://example.com/shapes/StartAssessmentUniqueCountByGradeShape")));
	}
}
