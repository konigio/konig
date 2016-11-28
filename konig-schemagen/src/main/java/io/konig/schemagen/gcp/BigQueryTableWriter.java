package io.konig.schemagen.gcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.model.Table;

import io.konig.core.KonigException;
import io.konig.schemagen.SchemaGeneratorException;

public class BigQueryTableWriter implements BigQueryTableHandler {

	private File outDir;
	private BigQueryTableGenerator generator;
	

	public BigQueryTableWriter(File outDir, BigQueryTableGenerator generator) {
		this.outDir = outDir;
		this.generator = generator;
		outDir.mkdirs();
	}

	public BigQueryTableGenerator getGenerator() {
		return generator;
	}


	@Override
	public void add(BigQueryTable source) {
		
		Table table = generator.toTable(source);
		
		JacksonFactory factory = JacksonFactory.getDefaultInstance();

		String fileName = tableFileName(source);
		File file = new File(outDir, fileName);
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);

			JsonGenerator generator = factory.createJsonGenerator(writer);
			generator.enablePrettyPrint();
			generator.serialize(table);
			generator.flush();
		
		} catch (IOException e) {
			throw new KonigException(e);
		} finally {
			close(writer);
		}
		
	}
	private void close(FileWriter writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (Throwable oops) {
				oops.printStackTrace(System.err);
			}
		}
		
	}


	private String tableFileName(BigQueryTable table) {
		BigQueryTableReference ref = table.getTableReference();
		if (ref ==null) {
			throw new SchemaGeneratorException("tableReference is not defined");
		}
		String tableId = ref.getTableId();
		String datasetId = ref.getDatasetId();
		String projectId = ref.getProjectId();
		if (tableId == null) {
			throw new SchemaGeneratorException("tableId is not defined");
		}
		if (datasetId == null) {
			throw new SchemaGeneratorException("datasetId is not defined for table " + tableId);
		}
		if (projectId == null) {
			throw new SchemaGeneratorException("projectId is not defined for table " + tableId);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(projectId);
		builder.append('.');
		builder.append(datasetId);
		builder.append('.');
		builder.append(tableId);
		
		return builder.toString();
	}

}
