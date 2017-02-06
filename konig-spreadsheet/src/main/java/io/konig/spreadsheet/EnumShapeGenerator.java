package io.konig.spreadsheet;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.services.ShapeProducer;

public class EnumShapeGenerator {
	
	private DataSourceGenerator dataSourceGenerator;
	private ShapeProducer producer;

	public EnumShapeGenerator(ShapeProducer producer, DataSourceGenerator dataSourceGenerator) {
		this.producer = producer;
		this.dataSourceGenerator = dataSourceGenerator;
	}

	public void generateShapes(Graph graph, IriTemplate shapeIdTemplate,  List<String> dataSourceTemplates) {
		List<Vertex> classList = graph.v(Schema.Enumeration).in(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex owlClass : classList) {
			if (owlClass.getId() instanceof URI) {
				Shape shape = producer.produceShape(owlClass, shapeIdTemplate);
				if (dataSourceTemplates != null && dataSourceGenerator!=null) {
					for (String templateName : dataSourceTemplates) {
						dataSourceGenerator.generate(shape, templateName, graph);
					}
				}
			}
		}
	}
	

}
