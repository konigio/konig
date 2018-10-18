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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class RangeInfo {

	private Resource parentShapeId;
	private URI datatype;
	private URI owlClass;
	public RangeInfo(Resource parentShapeId, URI datatype, URI owlClass) {
		this.parentShapeId = parentShapeId;
		this.datatype = datatype;
		this.owlClass = owlClass;
	}
	public Resource getParentShapeId() {
		return parentShapeId;
	}
	public URI getDatatype() {
		return datatype;
	}
	public URI getOwlClass() {
		return owlClass;
	}
	
	
}
