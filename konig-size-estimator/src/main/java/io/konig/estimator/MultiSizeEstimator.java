package io.konig.estimator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

/*
 * #%L
 * Konig Size Estimator
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

public class MultiSizeEstimator {
	protected ShapeManager shapeManager;
	
	
	public MultiSizeEstimator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	} 

	/**
	 * Load the specified manifest file, compute size estimates for each shape
	 * referenced in the manifest, and write the results to the
	 * <code>reportFile</code> given in the request.
	 * 
	 * @param request
	 *            A request object that gives the location of the manifest file and
	 *            the output file to which the report will be written
	 * @throws SizeEstimateException
	 * @throws IOException
	 */
	public void run(MultiSizeEstimateRequest request) throws SizeEstimateException, RDFParseException, RDFHandlerException, IOException {

		List<SizeEstimateRequest> estimates = getSizeEstimates(request);

		StringBuffer buffer = new StringBuffer();
		
		for (SizeEstimateRequest estimateRequest : estimates) {

			SizeEstimator estimator = new SizeEstimator(shapeManager);
			List<DataSourceSizeEstimate> list = estimator.averageSize(
					new SizeEstimateRequest(estimateRequest.getShapeId(), estimateRequest.getSampleDataDir()));
			
			
			buffer.append("BigQuery Table: ");
			buffer.append(shapeManager.getShapeById(estimateRequest.getShapeId()).getTargetClass().getLocalName());
			buffer.append("\n");
			
			for (DataSourceSizeEstimate estimate : list) {

				buffer.append("Number of Records in sample Files: ");
				buffer.append(estimate.getRecordCount());
				buffer.append("\n");

				buffer.append("Total size (in bytes): ");
				buffer.append(estimate.getSizeSum());
				buffer.append("\n");

				buffer.append("Average record size (in bytes): ");
				buffer.append(estimate.averageSize());
				buffer.append("\n");

				System.out.println(buffer);
				
				buffer.append("-------------------------------------------\n");
				
				
			}
		}
		
		FileUtils.writeStringToFile(request.getReportFile(), buffer.toString());
	}
	
	private List<SizeEstimateRequest> getSizeEstimates(MultiSizeEstimateRequest request) throws SizeEstimateException, IOException {
		List<SizeEstimateRequest> estimateList = new ArrayList<SizeEstimateRequest>();

		try {
			JSONParser parser = new JSONParser();
			JSONArray records = (JSONArray) parser.parse(new FileReader(request.getManifestFile()));
			
			for(Object obj : records) {
				JSONObject record = (JSONObject)obj;
				String shapeId = (String)record.get("shapeId");
				String sampleDataDir = (String)record.get("sampleDataDir");
				
				estimateList.add(new SizeEstimateRequest(new URIImpl(shapeId), new File(sampleDataDir)));
			}

		} catch (Exception e) {
			throw new SizeEstimateException("File " + request.getManifestFile() + " is not a valid file");
		} finally {
			
		}

		return estimateList;
	}
}
