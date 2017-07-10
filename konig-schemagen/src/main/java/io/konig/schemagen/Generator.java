package io.konig.schemagen;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;

public class Generator {
	
	protected NamespaceManager nsManager;
	protected IriEnumStyle iriEnumStyle = IriEnumStyle.CURIE;

	public Generator(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	protected String documentation(PropertyConstraint p) {
		String doc = p.getComment();
		if (doc != null) {
			doc = RdfUtil.normalize(doc);
		}
		
		
		if (doc == null) {
			doc = "";
		}
		StringBuilder builder = new StringBuilder(doc);
		
		addHasValueDocumentation(p, builder);
		addKnownValueDocumentation(p, builder);
		
		
		doc = builder.toString();
		return doc.length()==0 ? null : doc;
	}
	
	private void addKnownValueDocumentation(PropertyConstraint p, StringBuilder builder) {
		List<Value> possibleValues = p.getKnownValue();
		
		if (possibleValues !=null) {
			List<String> list = curieList(possibleValues);
			Set<Value> hasValue = p.getHasValue();
			if (hasValue != null) {
				for (Value v : hasValue) {
					String value = curie(v);
					list.remove(value);
				}
			}
			if (!list.isEmpty()) {
				beginClause(builder);
				builder.append("Possible values include (but are not limited to): ");
				writeList(builder, list);
			}
		}
		
	}
	

	private void addHasValueDocumentation(PropertyConstraint p, StringBuilder builder) {
		Set<Value> hasValue = p.getHasValue();
		
		if (hasValue != null && !hasValue.isEmpty()) {

			Integer maxCount = p.getMaxCount();
			if (maxCount == null || maxCount > 1) {
				beginClause(builder);
				
				builder.append("The set of values must include ");

				List<String> list = curieList(hasValue);
				if (list.size()>1) {
					builder.append(" all of ");
				}
				writeList(builder, list);
			}
			
			
		}
		
	}

	private void writeList(StringBuilder builder, List<String> list) {
		
		for (int i=0; i<list.size(); i++) {
			if (i>0) {
				if (i==list.size()-1) {
					builder.append(" and ");
				} else {
					builder.append(", ");
				}
			}
			String value = list.get(i);
			builder.append("'");
			builder.append(value);
			builder.append("'");
		}
		
	}

	private List<String> curieList(Collection<Value> source) {
		List<String> result = new ArrayList<>();
		for (Value value : source) {
			if (value instanceof URI) {
				result.add(curie((URI)value));
			} else {
				result.add(value.stringValue());
			}
		}
		Collections.sort(result);
		return result;
	}

	private void beginClause(StringBuilder builder) {
		
		if (builder.length()>0) {
			char c = builder.charAt(builder.length()-1);
			if (c != '.') {
				builder.append('.');
			}
			builder.append(' ');
		}
		
	}


	protected String curie(Value value) {
		if (!(value instanceof URI)) {
			return value.stringValue();
		}
		URI uri = (URI) value;
		String result = null;
		if (nsManager == null) {
			result = uri.stringValue();
		} else {
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(ns.getPrefix());
				builder.append(':');
				builder.append(uri.getLocalName());
				result = builder.toString();
			} else {
				result = uri.stringValue();
			}
		}
		return result;
	}

	protected Set<String> enumList(PropertyConstraint property) {
		
		List<Value> valueList = property.getIn();
		if (valueList != null && !valueList.isEmpty()) {
			Set<String> result = new HashSet<>();
			for (Value value : valueList) {
				if (value instanceof URI) {
					URI uri = (URI) value;
					switch (iriEnumStyle) {
					case CURIE : 	result.add(curie(uri)); break;
					case ABSOLUTE:	result.add(uri.stringValue()); break;
					case LOCAL: result.add(uri.getLocalName()); break;
					case NONE: // Do nothing
					}
					
				} else {
					String text = value.stringValue();
					if (validEnumValue(text)) {
						result.add(value.stringValue());
					}
				}
			}
			if (!result.isEmpty()) {
				return result;
			}
		}
		return null;
		
	}
	


	protected boolean validEnumValue(String text) {
		return true;
	}

	protected String strictValue(PropertyConstraint property) {
		
		Integer maxCount = property.getMaxCount();
		if (maxCount != null && maxCount==1) {
			
			Set<Value> valueSet = property.getHasValue();
			if (valueSet != null && valueSet.size()==1) {
				Value value = valueSet.iterator().next();
				if (value instanceof URI) {
					URI uri = (URI) value;
					switch (iriEnumStyle) {
					case CURIE : return curie(uri); 
					case ABSOLUTE: return uri.stringValue();
					case NONE: return null;
					}
				}
				String text = value.stringValue();
				if (validEnumValue(text)) {
					return text;
				}
			}
		}
		
		return null;
	}
}
