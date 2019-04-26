package io.konig.lineage;

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


import java.io.IOException;
import java.io.Writer;

import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.EmitContext;
import io.konig.core.pojo.SimplePojoEmitter;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceManager;

public class LineageWriter {

	
	/**
	 * Write the DatasourceProperty instances associated with a given DataSource.
	 * @throws RDFHandlerException 
	 */
	public void writeDatasourceProperties(Writer out, NamespaceManager nsManager) throws IOException, RDFHandlerException {
		Graph graph = new MemoryGraph(nsManager);
		EmitContext context = new EmitContext(graph);
		context.setIriReferenceByDefault(true);

		SimplePojoEmitter emitter = SimplePojoEmitter.getInstance();
		
		for (DataSource ds : DataSourceManager.getInstance().listDataSources()) {
			for (DatasourceProperty p : ds.getDatasourceProperty()) {
				emitter.emit(context, p, graph);
				if (p.getGeneratedFrom()!=null) {
					emitter.emit(context, p.getGeneratedFrom(), graph);
				}
			}
		}
		
		RdfUtil.prettyPrintTurtle(graph, out);
		out.flush();
		

		
	}

}
