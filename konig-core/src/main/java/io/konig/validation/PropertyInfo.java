package io.konig.validation;

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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public class PropertyInfo {
	private URI property;
	
	private List<RangeInfo> rangeInfo = new ArrayList<>();

	public PropertyInfo(URI property) {
		this.property = property;
	}

	public URI getProperty() {
		return property;
	}

	public List<RangeInfo> getRangeInfo() {
		return rangeInfo;
	}
	
	public void add(RangeInfo range) {
		rangeInfo.add(range);
	}
	
	public boolean isConflict(OwlReasoner reasoner) {
		URI datatype1 = null;
		URI owlClass1 = null;
		for (RangeInfo range : rangeInfo) {
			URI datatype2 = range.getDatatype();
			if (datatype1==null) {
				datatype1 = datatype2;
			} else if (datatype2 != null && !datatype2.equals(datatype1)) {
				return true;
			}
			
			URI owlClass2 = range.getOwlClass();
			if (owlClass1==null) {
				owlClass1 = owlClass2;
			} else if (
					owlClass2 != null && 
					!reasoner.isSubClassOf(owlClass2, owlClass1) &&
					!reasoner.isSubClassOf(owlClass1, owlClass2)
			) {
				return true;
			}
		}
		
		return false;
	}

	public URI rdfPropertyRange() {
		for (RangeInfo r : rangeInfo) {
			if (r.getParentShapeId()==null) {
				if (r.getDatatype()!=null) {
					return r.getDatatype();
				}
				return r.getOwlClass();
			}
		}
		return null;
	}
	

}
