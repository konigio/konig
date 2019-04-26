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


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.datasource.DataSource;
import io.konig.lineage.DatasourceProperty;
import io.konig.lineage.DatasourcePropertyPath;
import io.konig.lineage.PropertyGenerator;

public class LineageShowlNodeShapeConsumer implements ShowlNodeShapeConsumer {

	public LineageShowlNodeShapeConsumer() {
	}

	@Override
	public void consume(ShowlNodeShape node) throws ShowlProcessingException {
		
		if (node.getRoot() == node && node.getShapeDataSource()!=null) {
			DataSource ds = node.getShapeDataSource().getDataSource();
			if (ds != null) {
				if (!node.getChannels().isEmpty()) {
					scan(ds, node, node.getChannels());
				}
			}
		}
	}

	private void scan(DataSource ds, ShowlNodeShape node, List<ShowlChannel> channels) {
		for (ShowlDirectPropertyShape p : node.getProperties()) {
			Set<ShowlPropertyShape> set = new HashSet<>();
			ShowlExpression e = p.getSelectedExpression();
			if (e != null) {
				for (ShowlChannel channel : channels) {
					ShowlNodeShape sourceNode = channel.getSourceNode();
					e.addDeclaredProperties(sourceNode, set);
				}
			}
			
			DatasourceProperty q = produceProperty(p);
			PropertyGenerator generator = produceGenerator(q);
			
			for (ShowlPropertyShape sourceProperty : set) {
				DatasourceProperty r = produceProperty(sourceProperty);
				generator.addGeneratorInput(r);
				r.addGeneratorInputOf(generator);
			}
		}
		
	}

	private PropertyGenerator produceGenerator(DatasourceProperty q) {

		PropertyGenerator generator = q.getGeneratedFrom();
		if (generator == null) {
			generator = new PropertyGenerator();
			generator.setId(generatorId(q));
			generator.setGeneratorOutput(q);
			q.setGeneratedFrom(generator);
		}
		
		return generator;
		
	}

	private DatasourceProperty produceProperty(ShowlPropertyShape p) {
		DataSource ds = p.getRootNode().getShapeDataSource().getDataSource();
		DatasourcePropertyPath path = path(p);
		
		DatasourceProperty q = ds.findPropertyByPath(path);
		if (q == null) {
			q = new DatasourceProperty();
			q.setPropertyPath(path);
			q.setId(propertyId(ds, q.getPropertyPath()));
			q.setPropertySource(ds);
			ds.addDatasourceProperty(q);
			
		}
		return q;
	}

	private URI generatorId(DatasourceProperty q) {
		return new URIImpl(q.getId().stringValue() + "/generator");
	}

	private URI propertyId(DataSource ds, DatasourcePropertyPath propertyPath) {
		StringBuilder builder = new StringBuilder(ds.getId().stringValue());
		String delim = "/property/";
		for (URI predicate : propertyPath) {
			builder.append(delim);
			delim = ".";
			builder.append(predicate.getLocalName());
		}
		return new URIImpl(builder.toString());
	}

	private DatasourcePropertyPath path(ShowlPropertyShape p) {
		DatasourcePropertyPath path = new DatasourcePropertyPath();
		while (p != null) {
			path.add(p.getPredicate());
			p = p.getDeclaringShape().getAccessor();
		}
		Collections.reverse(path);
		return path;
	}

}
