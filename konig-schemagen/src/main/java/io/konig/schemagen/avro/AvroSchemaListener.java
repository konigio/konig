package io.konig.schemagen.avro;

import java.io.IOException;

public interface AvroSchemaListener {

	public void handleSchema(AvroSchemaResource resource) throws IOException;
}
