package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Collection;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * A utility that builds ShowlClass and ShowlProperty instances.
 * @author Greg McFall
 *
 */
public class ShowlClassProcessor {
	

	private ShowlSchemaService schemaService;
	private ShowlNodeShapeService nodeService;
	
	
	public ShowlClassProcessor(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService) {
		this.schemaService = schemaService;
		this.nodeService = nodeService;
	}

	public void buildAll(ShapeManager shapeManager) {
		ShowlNodeShapeBuilder shapeBuilder = new ShowlNodeShapeBuilder(schemaService, nodeService);
		shapeBuilder.setRecursive(false);
		
		for (Shape shape : shapeManager.listShapes()) {
			ShowlNodeShape nodeShape = shapeBuilder.buildNodeShape(null, shape);
			nodeShape.getOwlClass().addTargetClassOf(nodeShape);
			enrichProperties(nodeShape);
		}
	}

	private void enrichProperties(ShowlNodeShape nodeShape) {
		scanDirectProperties(nodeShape);
		scanDerivedProperties(nodeShape.getDerivedProperties());
	}

	private void scanDerivedProperties(Collection<ShowlDerivedPropertyList> list) {
		
		for (ShowlDerivedPropertyList innerList : list) {
			for (ShowlDerivedPropertyShape p : innerList) {
				if (p.getPropertyConstraint() != null) {
					p.getProperty().addPropertyShape(p);
				}
			}
		}
	}

	private void scanDirectProperties(ShowlNodeShape node) {
		ShowlClass owlClass = node.getOwlClass();
		for (ShowlPropertyShape p : node.getProperties()) {
			if (p.getPropertyConstraint() != null) {
				p.getProperty().addPropertyShape(p);
				owlClass.addDomainOf(p.getProperty());
			}
		}
		
	}

}
