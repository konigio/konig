package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
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


import java.util.List;

import io.konig.core.Vertex;

/**
 * An expression which corresponds to the lookup of an attribute of a named individual from the semantic model.
 * @author Greg McFall
 *
 */
public class TReferenceDataExpression implements TExpression {
	
	private TPropertyShape valueOf;
	private List<Vertex> individuals;
	private TPropertyShape keyProperty;
	
	public TReferenceDataExpression(TPropertyShape valueOf, TPropertyShape keyProperty, List<Vertex> individuals) {
		this.valueOf = valueOf;
		this.individuals = individuals;
		this.keyProperty = keyProperty;
	}
	
	

	public TPropertyShape getKeyProperty() {
		return keyProperty;
	}



	@Override
	public TPropertyShape valueOf() {
		return valueOf;
	}
	
	public String toString() {
		return "TReferenceDataExpression[" + valueOf.getPath() + "]";
	}

	public List<Vertex> getIndividuals() {
		return individuals;
	}
	

}
