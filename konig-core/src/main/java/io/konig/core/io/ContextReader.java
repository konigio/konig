package io.konig.core.io;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.CompositeContext;

public class ContextReader {
	
	private ContextManager contextManager;

	public ContextReader() {
	}
	
	
	public ContextReader(ContextManager contextManager) {
		this.contextManager = contextManager;
	}


	public Context parse(ObjectNode node) throws KonigReadException {
		return parseObject(node, false);
	}
	
	public Context read(InputStream input) throws KonigReadException {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			node = mapper.readTree(input);

			if (node instanceof ObjectNode) {
				ObjectNode object = (ObjectNode) node;
				Context context = parseObject(object, true);
				if (context != null) {
					JsonNode mimeType = object.get("vendorType");
					if (mimeType != null) {
						String vendorType = mimeType.textValue();
						context.setVendorType(vendorType);
					}
				}
				return context;
			} 
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
		
		throw new KonigReadException("Root node in stream is not a JSON object");
		
	}
	
	private Context parseArray(ArrayNode array) throws KonigReadException {
		CompositeContext composite = new CompositeContext();
		for (int i=0; i<array.size(); i++) {
			JsonNode node = array.get(i);
			if (node.isTextual()) {
				String contextIRI = node.asText();
				if (contextManager == null) {
					throw new KonigReadException("Cannot load context because manager is not defined: " + contextIRI);
				}
				Context context = contextManager.getContextByURI(contextIRI);
				if (context == null) {
					throw new KonigReadException("Context not found: " + contextIRI);
				}
				composite.append(context);
				
			} else if (node instanceof ObjectNode) {
				ObjectNode terms = (ObjectNode) node;
				Context context = new BasicContext(null);
				parseTerms(terms, context);
				composite.append(context);
			}
		}
		return composite;
	}
	
	private void parseTerms(ObjectNode terms, Context context) {
		Iterator<String> sequence = terms.fieldNames();
		while (sequence.hasNext()) {
			String key = sequence.next();
			JsonNode termNode = terms.get(key);
			String idValue=null;
			String language=null;
			String type=null;
			String container=null;
			
			Term term = null;
			if (termNode.isTextual()) {
				idValue = termNode.asText();
			} else if (termNode.isObject()) {
				ObjectNode termObject = (ObjectNode) termNode;
				idValue = stringValue(termObject, "@id");
				language = stringValue(termObject, "@language");
				type = stringValue(termObject, "@type");
				container = stringValue(termObject, "@container");
			}
			
			term = new Term(key, idValue, language, type, container);
			context.add(term);
		}
	}
	

	private Context parseObject(ObjectNode node, boolean useId) throws KonigReadException {
		String contextIRI = null;
		if (useId) {
			JsonNode id = node.get("@id");
			if (id != null) {
				contextIRI = id.asText();
				if (contextManager!=null) {
					Context context = contextManager.getContextByURI(contextIRI);
					if (context != null) {
						return context;
					}
				}
			}
		}
		
		
		JsonNode contextNode = node.get("@context");
		
		if (contextNode instanceof ArrayNode) {
			return parseArray((ArrayNode)contextNode);
		}
		Context context = null;
		if (contextNode.isTextual()) {
			contextIRI = contextNode.asText();
			if (contextManager!=null) {
				context = contextManager.getContextByURI(contextIRI);
			}
			if (context == null) {
				throw new KonigReadException("Context not found: " + contextIRI);
			}
			return context;
					
		}
		
		context = new BasicContext(contextIRI);
		if (contextManager != null && contextIRI!=null) {
			contextManager.add(context);
		}
		
		if (contextNode instanceof ObjectNode) {
			parseTerms((ObjectNode)contextNode, context);
		}
		
		return context;
	}

	private String stringValue(ObjectNode node, String key) {
		JsonNode value = node.get(key);
		if (value != null && value.isTextual()) {
			return value.asText();
		}
		return null;
	}

}
