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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlServiceImpl implements ShowlService {
	
	private Map<URI,ShowlClass> classMap = new HashMap<>();
	private Map<URI,ShowlProperty> propertyMap = new HashMap<>();
	
	private OwlReasoner reasoner;

	public ShowlServiceImpl(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public ShowlClass produceShowlClass(URI id) {
		ShowlClass owlClass = classMap.get(id);
		if (owlClass == null) {
			owlClass = new ShowlClass(reasoner, id);
			classMap.put(id,  owlClass);
		}
		return owlClass;
	}

	@Override
	public ShowlProperty produceProperty(URI predicate) {
		ShowlProperty property = propertyMap.get(predicate);
		if (property == null) {
			property = new ShowlProperty(predicate);
			propertyMap.put(predicate, property);
		}
		return property;
	}

	@Override
	public ShowlNodeShape logicalNodeShape(URI owlClass) throws ShowlProcessingException {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlNodeShape createNodeShape(Shape shape) throws ShowlProcessingException {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlNodeShape createNodeShape(Shape shape, DataSource ds) throws ShowlProcessingException {

		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlClass inferDomain(ShowlProperty p) {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlClass inferRange(ShowlProperty p) {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlClass mostSpecificClass(ShowlClass a, ShowlClass b) {
		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShowlNodeShape createShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {

		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlNodeShape targetShape) {

		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public ShapeManager getShapeManager() {

		throw new ShowlProcessingException("not implemented yet");
	}

	@Override
	public Graph getGraph() {
		return reasoner.getGraph();
	}

	@Override
	public OwlReasoner getOwlReasoner() {
		return reasoner;
	}

}
