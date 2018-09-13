package io.konig.shacl.io;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.project.ProjectFile;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;

public class DdlFileEmitter implements ShapeEmitter {
	
	private Set<URI> datasourceType = new HashSet<>();
	
	public DdlFileEmitter(URI...acceptDatasourceType) {
		for (URI uri : acceptDatasourceType) {
			acceptDatasourceType(uri);
		}
	}
	
	public void acceptDatasourceType(URI datasourceType) {
		this.datasourceType.add(datasourceType);
	}

	@Override
	public void emitShape(Shape shape, Graph graph) {
		
		List<DataSource> datasourceList = shape.getShapeDataSource();
		if (datasourceList != null) {
			for (DataSource ds : datasourceList) {
				if (ds instanceof TableDataSource) {
					
					for (URI typeId : ds.getType()) {
						if (datasourceType.contains(typeId)) {
							TableDataSource table = (TableDataSource) ds;
							ProjectFile ddlFile = table.getDdlFile();
							if (ddlFile != null) {
								
								Vertex fileNode = graph.vertex();
								graph.edge(ds.getId(), Konig.ddlFile, fileNode.getId());
								
								fileNode.addProperty(Konig.baseProject, ddlFile.getBaseProject().getId());
								fileNode.addProperty(Konig.relativePath, literal(ddlFile.getRelativePath()));
							}
						}
					}
				}
			}
		}

	}

	private Literal literal(String value) {
		return new LiteralImpl(value);
	}

}
