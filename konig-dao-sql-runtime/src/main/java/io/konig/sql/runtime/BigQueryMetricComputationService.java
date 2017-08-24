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

import java.util.List;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.Get;
import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationQuery;
import com.google.api.services.bigquery.model.TableReference;

import io.konig.dao.core.DaoException;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;

public class BigQueryMetricComputationService {
	private EntityStructureService structureService;
	private Bigquery bigQuery;	
	
	public BigQueryMetricComputationService(EntityStructureService structureService, Bigquery bigQuery) {
		this.bigQuery = bigQuery;
		this.structureService = structureService;
	}

	public void executeSql(URI shapeId, String query) throws DaoException {
		try {			
			// TODO: Get the shape object from the shapeId and get the Table reference
			
			BigQueryTableReference bqTableRef = new BigQueryTableReference();//getTableReference(shape);	
			bqTableRef.setProjectId("mypedia-dev-55669");
			bqTableRef.setDatasetId("fact");
			bqTableRef.setTableId("StartAssessmentUniqueCountByGrade");		
			TableReference tableRef = new TableReference().setProjectId(bqTableRef.getProjectId())
					.setDatasetId(bqTableRef.getDatasetId())
					.setTableId(bqTableRef.getTableId());
			
			JobConfigurationQuery queryConfig = new JobConfigurationQuery()
					.setQuery(query)
					.setDestinationTable(tableRef).setAllowLargeResults(true)
					.setUseLegacySql(false)
					.setPriority("BATCH")
					.setWriteDisposition("WRITE_TRUNCATE");
			
			Job queryJob = new Job().setConfiguration(new JobConfiguration().setQuery(queryConfig));
			
			Job job = bigQuery.jobs().insert(bqTableRef.getProjectId(), queryJob).execute();
			
			Get getRequest = bigQuery.jobs().get(bqTableRef.getProjectId(), job.getJobReference().getJobId());
			
			Job jobRequest = getRequest.execute();
			while (!jobRequest.getStatus().getState().equals("DONE")) {
				Thread.sleep(1000);
				jobRequest = getRequest.execute();
			}
			System.out.println( "Job Status : " + jobRequest.getStatus().getState());
			ErrorProto errorResult = job.getStatus().getErrorResult();

			if (errorResult != null) {
				throw new DaoException("Error running job: " + errorResult);
			}			
			
		} catch (Exception e) {
			throw new DaoException(e.getMessage());
		}

	}
	
	public BigQueryTableReference getTableReference(Shape shape) {		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds instanceof GoogleBigQueryTable) {
					GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
					return table.getTableReference();
				}
			}
		}
		
		return null;
	}
}
