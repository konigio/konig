package io.konig.shacl;

/*
 * #%L
 * Konig SHACL
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.PathFactory;

public class ShapeValidator {
	
	private PathFactory pathFactory;

	public ShapeValidator(PathFactory pathFactory) {
		this.pathFactory = pathFactory;
	}

	public void validate(Vertex focusNode, Shape shape, ValidationReport report) {
		validateEquivalentPath(focusNode, shape, report);
	}

	private void validateEquivalentPath(Vertex focusNode, Shape shape, ValidationReport report) {
		
		for (PropertyConstraint p : shape.getProperty()) {
			validateEquivalentPath(focusNode, shape, p, report);
		}
		
	}

	private void validateEquivalentPath(Vertex focusNode, Shape shape, PropertyConstraint p, ValidationReport report) {
		
		String equivalentPath = p.getEquivalentPath();
		if (equivalentPath != null) {
			
			URI predicate = p.getPredicate();
			Set<Object> actual = RdfUtil.toJavaValue( focusNode.getValueSet(predicate) );
			
			Path path = p.getCompiledEquivalentPath();
			if (path == null) {
				path = pathFactory.createPath(equivalentPath);
				p.setCompiledEquivalentPath(path);
			}
			
			Set<Object> expected = RdfUtil.toJavaValue( path.traverse(focusNode) );
			
			boolean ok = expected.size() == actual.size();
			if (ok) {
				for (Object a : expected) {
					if (!actual.contains(a)) {
						ok = false;
						break;
					}
				}
			}
			
			if (!ok) {
				StringBuilder message = new StringBuilder();
				RdfUtil.append(message, focusNode.getId());
				message.append('!');
				message.append(predicate.getLocalName());
				message.append(" equivalentPath :");
				message.append(" Expected ");
				String comma = "";
				for (Object v : expected) {
					message.append(comma);
					comma = ", ";
					RdfUtil.append(message, v);
				}
				message.append( " ...but found... ");
				
				
				if (actual.isEmpty()) {
					message.append("NULL");
				} else {

					comma = "";
					for (Object v : actual) {
						message.append(comma);
						comma = ", ";
						RdfUtil.append(message, v);
					}
				}
				
				ValidationResult result = new ValidationResult();
				result.setFocusNode(focusNode.getId());
				result.setMessage(message.toString());
				result.setPath(predicate);
				result.setSeverity(Severity.WARNING);
				result.setSourceShape(shape.getId());
				report.addValidationResult(result);
			}
			
		}
		
		
	}

}
