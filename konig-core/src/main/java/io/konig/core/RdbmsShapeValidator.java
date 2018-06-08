package io.konig.core;

/*
 * #%L
 * Konig Schema Generator
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


import org.openrdf.model.URI;
import io.konig.core.util.StringUtil;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class RdbmsShapeValidator {
	

	
	public boolean validate(Shape shape){
		if (shape!= null && shape.getRdbmsLogicalShape()!= null){
			return true;
		}
		return false;
	}
	
public boolean isValidRDBMSShape(Shape shape) {
		
		for (PropertyConstraint p : shape.getProperty()) {
			URI predicate = p.getPredicate();
			if (p.getShape() == null && p.getMaxCount()!=null && p.getMaxCount()==1 &&predicate != null) {
					String localName = predicate.getLocalName();
					String snakeCase = StringUtil.SNAKE_CASE(localName);
					if (localName.equals(snakeCase)) {
						return true;
					}
				}

	}
		return false;

}
}
