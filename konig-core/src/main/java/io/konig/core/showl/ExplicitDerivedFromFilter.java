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


import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;

/**
 * A filter that accepts only those shapes where the explicitDerivedFrom set is
 * non-empty.
 * 
 * @author Greg McFall
 *
 */
public class ExplicitDerivedFromFilter implements ShapeFilter {

	@Override
	public boolean accept(Shape shape) {
		
		return !shape.getExplicitDerivedFrom().isEmpty();
	}

}
