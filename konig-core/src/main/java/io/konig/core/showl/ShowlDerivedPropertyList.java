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


import java.util.ArrayList;

import org.openrdf.model.URI;

@SuppressWarnings("serial")
public class ShowlDerivedPropertyList extends ArrayList<ShowlDerivedPropertyShape> {

	private URI predicate;
	
	public ShowlDerivedPropertyList(URI predicate) {
		this.predicate = predicate;
	}

	public URI getPredicate() {
		return predicate;
	}
	
	/**
	 * Return the one derived property in this list that is not filtered, or null
	 * if no such property is found.
	 */
	public ShowlDerivedPropertyShape unfiltered() {
		for (ShowlDerivedPropertyShape p : this) {
			if (p.getHasValueDeprecated().isEmpty()) {
				return p;
			}
		}
		return null;
	}
	
	public ShowlDerivedPropertyShape withFormula() {
		for (ShowlDerivedPropertyShape p : this) {
			if (p.getFormula()!=null) {
				return p;
			}
		}
		return null;
	}

}
