package io.konig.core.showl;

import java.util.LinkedHashMap;
import java.util.Map;

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


import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;

@SuppressWarnings("serial")
abstract public class ShowlStructExpression extends LinkedHashMap<URI, ShowlExpression> implements ShowlExpression {
	private static final Logger logger = LoggerFactory.getLogger(ShowlStructExpression.class);
	private ShowlDirectPropertyShape propertyShape;
	
	protected ShowlStructExpression(ShowlDirectPropertyShape propertyShape) {
		this.propertyShape = propertyShape;
	}

	@Override
	public String displayValue() {
		return propertyShape.getPath();
	}
	

	public ShowlDirectPropertyShape getPropertyShape() {
		return propertyShape;
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		for (ShowlExpression e : values()) {
			e.addDeclaredProperties(sourceNodeShape, set);
		}
	}
	
	@Override
	public ShowlExpression put(URI key, ShowlExpression value) {
		if (logger.isTraceEnabled()) {
			logger.trace("put({}, {})", key.getLocalName(), value.displayValue());
		}
		return super.put(key, value);
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		for (ShowlExpression e : values()) {
			e.addProperties(set);
		}
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return propertyShape.maybeDirect().getValueType(reasoner);
	}
	
	abstract protected ShowlStructExpression copy();
	
	
	@Override 
	public ShowlStructExpression transform() {
		ShowlStructExpression copy = copy();

		for (Map.Entry<URI,ShowlExpression> entry : entrySet()) {
			copy.put(entry.getKey(), entry.getValue().transform());
		}
		return copy;
		
	}
	
}
