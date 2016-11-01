package io.konig.shacl.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Vertex;
import io.konig.shacl.Shape;

public class JsonShapeWriterImpl implements JsonShapeWriter {


	@Override
	public void toJson(Vertex subject, Shape shape, JsonGenerator json) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public ObjectNode toJson(Vertex subject, Shape shape) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
