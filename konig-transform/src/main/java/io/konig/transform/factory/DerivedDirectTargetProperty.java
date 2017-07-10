package io.konig.transform.factory;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import io.konig.shacl.PropertyConstraint;

public class DerivedDirectTargetProperty extends DirectTargetProperty {

	public DerivedDirectTargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return null;
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {

	}
	@Override
	public int totalPropertyCount() {
		return 0;
	}

	@Override
	public int mappedPropertyCount() {
		return 0;
	}

}
