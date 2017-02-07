package io.konig.schemagen.gcp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.KonigException;
import io.konig.datasource.DataSource;
import io.konig.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class GoogleCloudStorageBucketWriter implements ShapeVisitor {
	
	private File outDir;

	public GoogleCloudStorageBucketWriter(File outDir) {
		this.outDir = outDir;
	}

	public void write(GoogleCloudStorageBucket bucket, JsonGenerator json) throws IOException {
		json.writeStartObject();
		json.writeStringField("name", bucket.getName());
		json.writeStringField("location", bucket.getLocation());
		json.writeStringField("storageClass", bucket.getStorageClass());
		json.writeEndObject();
	}

	@Override
	public void visit(Shape shape) {
		try {
			List<DataSource> list = shape.getShapeDataSource();
			if (list != null) {
				for (DataSource source : list) {
					if (source instanceof GoogleCloudStorageBucket) {
						GoogleCloudStorageBucket bucket = (GoogleCloudStorageBucket) source;
						String fileName = bucket.getName() + ".json";
						outDir.mkdirs();
						File file = new File(outDir, fileName);
						
						JsonFactory factory = new JsonFactory();
						JsonGenerator json = factory.createGenerator(file, JsonEncoding.UTF8);
						json.useDefaultPrettyPrinter();
						try {
							write(bucket, json);
						} finally {
							json.close();
						}
					}
				}
			}
		} catch (Throwable oops) {
			throw new KonigException(oops);
		}
		
	}

}
