package io.konig.estimator;

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


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class SizeEstimator {
	private static final String JSON = ".json";
	private static final String CSV = ".csv";
	private ShapeManager shapeManager;
	BigQueryDatatypeStorageMapper mapper = new BigQueryDatatypeStorageMapper();

	public SizeEstimator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public List<DataSourceSizeEstimate> averageSize(SizeEstimateRequest request) throws SizeEstimateException {

		Shape shape = shapeManager.getShapeById(request.getShapeId());
		if (shape == null) {
			throw new SizeEstimateException("Shape not found: " + request.getShapeId());
		}

		List<DataSourceSizeEstimate> response = prepareResponse(shape);

		File dataDir = request.getSampleDataDir();
		File[] fileList = dataDir.listFiles();
		if (fileList != null) {
			for (File dataFile : fileList) {
				handleFile(dataFile, response);
			}
		}

		if (response.get(0).getRecordCount() == 0) {
			throw new SizeEstimateException("No data files found at " + dataDir);
		}

		return response;
	}

	private void handleFile(File dataFile, List<DataSourceSizeEstimate> response) throws SizeEstimateException {
		String fileName = dataFile.getName();

		try {
			if (fileName.endsWith(JSON)) {
				handleJson(dataFile, response);
			} else if (fileName.endsWith(CSV)) {
				handleCsv(dataFile, response);
			}
		} catch (ParseException e) {
			throw new SizeEstimateException("Improper data file: " + fileName);
		} catch (IOException e) {
			throw new SizeEstimateException("Improper data file: " + fileName);
		}

	}

	private void handleCsv(File dataFile, List<DataSourceSizeEstimate> response) throws IOException {
		// Parse the CSV file.
		// For each DataSourceSizeEstimate entity in the response,
		// For each property in the file, locate the corresponding PropertyConstraint
		// from the Shape.
		// Use an appropriate data type mapper (such as BigQueryDatatypeMapper) to
		// identify the physical data type.
		// Lookup the expected size of the physical datatype; in the case of strings,
		// compute the size from the actual data.
		// Invoke incrementSize on the DataSourceSizeEstimate
		for (DataSourceSizeEstimate sourceEstimate : response) {			
			List<PropertyConstraint> properties = sourceEstimate.getShape().getProperty();
			
			CSVParser parser = null;
			try {
				parser = new CSVParser(new FileReader(dataFile), CSVFormat.DEFAULT.withAllowMissingColumnNames());
				for (CSVRecord record : parser) {
					for (int i = 0; i < record.size(); ++i) {
						int value = mapper.getDataSize(properties.get(i), record.get(i));
						sourceEstimate.incrementSize(value);
					}
					sourceEstimate.incrementRecord();
				}
			} finally {
				if (parser != null) parser.close();
			}
		}
	}

	private void handleJson(File dataFile, List<DataSourceSizeEstimate> response) throws IOException, ParseException {
		// Similar to handleCsv, except we parse a JSON file instead of CsvFile.
		for (DataSourceSizeEstimate sourceEstimate : response) {
			
			List<PropertyConstraint> properties = sourceEstimate.getShape().getProperty();
			
			JSONParser parser = new JSONParser();
			JSONArray records = (JSONArray) parser.parse(new FileReader(dataFile));
			processJsonRecords(sourceEstimate, properties, records);			
		}
	}

	private void processJsonRecords(DataSourceSizeEstimate sourceEstimate, List<PropertyConstraint> properties, JSONArray records) {
		for(Object obj : records) {
			JSONObject record = (JSONObject)obj;
			for(PropertyConstraint pc : properties) {
				String key = pc.getPredicate().getLocalName();
				
				int value = mapper.getDataSize(pc, record.get(key));
				if (value == -1) {
					processJsonRecords(sourceEstimate, pc.getShape().getProperty(), (JSONArray)record.get(key));
				}
				sourceEstimate.incrementSize(value);
			}
			sourceEstimate.incrementRecord();
		}
	}

	private List<DataSourceSizeEstimate> prepareResponse(Shape shape) throws SizeEstimateException {
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			List<DataSourceSizeEstimate> result = new ArrayList<>();
			for (DataSource source : list) {
				if (acceptDataSource(source)) {
					result.add(new DataSourceSizeEstimate(shape, source));
				}
			}
			if (!result.isEmpty()) {
				return result;
			}
		}

		throw new SizeEstimateException("No suitable datasources found for Shape: " + shape.getId());
	}

	/**
	 * Return true if the DataSource is suitable for estimating the size.
	 */
	private boolean acceptDataSource(DataSource source) {
		return (source.isA(Konig.GoogleBigQueryTable) && !source.isA(Konig.GoogleBigQueryView)
				&& !source.isA(Konig.GoogleCloudStorageBucket));
	}

}
