package io.konig.core.showl;

import java.util.LinkedHashSet;

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

import io.konig.core.OwlReasoner;

/**
 * An expression that constructs the value of a target property within the 
 * context of an ETL process.
 * 
 * @author Greg McFall
 *
 */
public interface ShowlExpression {
	
	/**
	 * Return a copy of this expression where every PropertyShape expression is replaced
	 * with its 'selectedExpression'
	 */
	ShowlExpression transform();
	
	public String displayValue();
	
	// TODO: Replace addDeclaredProperties with a more generic method that scans for all properties,
	//       Not just those rooted at a given sourceNodeShape.

	/**
	 * Collect from this expression all properties declared by the specified source NodeShape, or any 
	 * NodeShape nested within the source NodeShape.
	 * @param sourceNodeShape
	 * @param set  The set to which the properties should be added.
	 */
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
	throws ShowlProcessingException;
	
	public void addProperties(Set<ShowlPropertyShape> set);
	
	public static Set<ShowlPropertyShape> parameters(ShowlExpression e) {
		Set<ShowlPropertyShape> set = new LinkedHashSet<>();
		e.addProperties(set);
		return set;
	}
	
	public URI valueType(OwlReasoner reasoner);
	
}
