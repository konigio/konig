package io.konig.core.showl;

import io.konig.shacl.PropertyConstraint;

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


public class ShowlOutwardPropertyShape extends ShowlDerivedPropertyShape {

	public ShowlOutwardPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property) {
		this(declaringShape, property, null);
	}
	
	public ShowlOutwardPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property, PropertyConstraint c) {
		super(declaringShape, property, c);
		property.addPropertyShape(this);
	}

}
