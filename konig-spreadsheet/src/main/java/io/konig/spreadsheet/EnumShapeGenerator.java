package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.services.ShapeProducer;

public class EnumShapeGenerator {
	
	private DataSourceGenerator dataSourceGenerator;
	private ShapeProducer producer;

	public EnumShapeGenerator(ShapeProducer producer, DataSourceGenerator dataSourceGenerator) {
		this.producer = producer;
		this.dataSourceGenerator = dataSourceGenerator;
	}

	public void generateShapes(Graph graph, ShapeManager shapeManager, IriTemplate shapeIdTemplate,  List<String> dataSourceTemplates) {
		List<Vertex> classList = graph.v(Schema.Enumeration).in(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex owlClass : classList) {
			if (owlClass.getId() instanceof URI) {
				Shape shape = producer.produceShape(owlClass, shapeIdTemplate);
				if (dataSourceTemplates != null && dataSourceGenerator!=null) {
					for (String templateName : dataSourceTemplates) {
						dataSourceGenerator.generate(shape, templateName, shapeManager);
					}
				}
			}
		}
	}
	

}
