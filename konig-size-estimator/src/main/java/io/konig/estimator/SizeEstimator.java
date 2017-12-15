package io.konig.estimator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.schemagen.gcp.BigQueryDatatype;
import io.konig.schemagen.gcp.BigQueryDatatypeMapper;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class SizeEstimator {
	private static final String JSON = ".json";
	private static final String CSV = ".csv";
	private ShapeManager shapeManager;

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
						int value = handleDataTypeMapping(sourceEstimate.getDataSource(), properties.get(i), record.get(i));
						sourceEstimate.incrementSize(value);
					}
					sourceEstimate.incrementRecord();
				}
			} finally {
				if (parser != null) parser.close();
			}
		}
	}

	private int handleDataTypeMapping(DataSource dataSource, PropertyConstraint propertyConstraint, String data) {
		int value = 0;
		if (dataSource.isA(Konig.GoogleBigQueryTable)) {
			BigQueryDatatypeStorageMapper mapper = new BigQueryDatatypeStorageMapper();
			value = mapper.getDataSize(propertyConstraint, data);
		}
		return value;
	}

	private void handleJson(File dataFile, List<DataSourceSizeEstimate> response) {
		// Similar to handleCsv, except we parse a JSON file instead of CsvFile.

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
