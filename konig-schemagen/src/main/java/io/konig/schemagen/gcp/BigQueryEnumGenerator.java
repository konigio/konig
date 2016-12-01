package io.konig.schemagen.gcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.json.JsonWriter;

public class BigQueryEnumGenerator {
	
	private ShapeManager shapeManager;
	
	public BigQueryEnumGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public void generate(Graph graph, DataFileMapper dataFileMapper) throws IOException {
		List<Vertex> enumClassList = graph.v(Schema.Enumeration).in(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex owlClass : enumClassList) {
			generate(owlClass, dataFileMapper);
		}
	}

	private void generate(Vertex owlClass, DataFileMapper dataFileMapper) throws IOException {
		
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			URI enumClassId = (URI) id;

			List<Vertex> list = owlClass.asTraversal().in(RDF.TYPE).toVertexList();
			
			if (!list.isEmpty()) {

				File file = dataFileMapper.fileForEnumRecords(owlClass);
				if (file != null) {
					
					Shape shape = getBigQueryTableShape(enumClassId);
					if (shape != null) {

						FileWriter out = new FileWriter(file);
						
						JsonFactory factory = new JsonFactory();
						JsonGenerator generator = factory.createGenerator(out);
						JsonWriter jsonWriter = new JsonWriter(generator);
						try {

							for (Vertex v : list) {
								jsonWriter.write(shape, v);
								generator.flush();
								out.write('\n');
							}

						} finally {
							out.close();
						}
						
					}
					
					
				}
			}
			
		}
		
		
	}

	private Shape getBigQueryTableShape(URI enumClassId) {

		List<Shape> shapeList = shapeManager.getShapesByTargetClass(enumClassId);
		for (Shape shape : shapeList) {
			if (shape.getBigQueryTableId() != null) {
				return shape;
			}
		}
		return null;
	}

}
