package io.konig.gcp.datasource;

import io.konig.core.pojo.PojoContext;
import io.konig.core.vocab.Konig;
import io.konig.shacl.io.ShapeLoader;

public class GcpShapeConfig {
	
	public static void init() {

		PojoContext context = ShapeLoader.CONTEXT;
		
		context.mapClass(Konig.GoogleBigQueryTable, GoogleBigQueryTable.class);
		context.mapClass(Konig.GoogleCloudStorageBucket, GoogleCloudStorageBucket.class);
	}

}
