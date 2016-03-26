package io.konig.core.io;

/*
 * #%L
 * konig-core
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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.openrdf.model.BNode;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.KonigValueFactory;
import io.konig.core.ListHandler;
import io.konig.core.NamespaceManager;
import io.konig.core.Term;
import io.konig.core.UidGenerator;

public class JsonldParser extends RDFParserBase {
	
	private ContextReader contextReader;
	private NamespaceManager namespaceManager;

	public JsonldParser(ContextManager contextManager) {
		this(contextManager, new KonigValueFactory());
	}
	
	
	public JsonldParser(ContextManager contextManager, NamespaceManager nsManager) {
		this(contextManager, nsManager, new KonigValueFactory());
	}

	public JsonldParser(ContextManager contextManager, NamespaceManager namespaceManager, ValueFactory valueFactory) {
		super(valueFactory);
		this.contextReader = new ContextReader(contextManager);
		this.namespaceManager = namespaceManager;
	}



	public JsonldParser(ContextManager contextManager, ValueFactory valueFactory) {
		this(contextManager, null, valueFactory);
	}

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.JSONLD;
	}

	@Override
	public void parse(InputStream stream, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
		
		parse(new InputStreamReader(stream), baseURI);
	}

	@Override
	public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
		
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(reader);
		if (!(node instanceof ObjectNode)) {
			throw new RDFParseException("Root element is not an object");
		}
		Worker worker = new Worker(baseURI);
		worker.parseObject((ObjectNode)node);
		rdfHandler.endRDF();
	}
	
	private class Worker {
		private String baseURI;

		public Worker(String baseURI) {
			this.baseURI = baseURI;
			
		}

		private Context context;
		private String valueKey="@value";
		private String idKey="@id";
		private String typeKey="@type";
		private String languageKey="@language";


		private BNode bnode() {
			return valueFactory.createBNode(UidGenerator.INSTANCE.next());
		}

		private Resource parseObject(ObjectNode object) throws RDFParseException, RDFHandlerException {
			
			Context saveContext = context;
			String saveIdKey = idKey;
			String saveValueKey = valueKey;
			String saveTypeKey = typeKey;
			String saveLanguageKey = languageKey;
			
			Resource subject = null;
			
			JsonNode contextNode = object.get("@context");
			if (contextNode != null) {
				try {
					context = contextReader.parse(object);
					context.compile();
				} catch (Throwable oops) {
					throw new RDFHandlerException(oops);
				}

				valueKey = context.alias("@value");
				idKey = context.alias("@id");
				typeKey = context.alias("@type");
				languageKey = context.alias("@language");
			}
			
			JsonNode idNode = object.get(idKey);
			if (idNode == null) {
				subject = bnode();
			} else {
				String idValue = idNode.asText();
				if (idValue.startsWith("_:")) {
					idValue = idValue.substring(2);
					subject = valueFactory.createBNode(idValue);
				} else {
					subject = iri(idValue);
				}
				
			}
			
			
			Iterator<Entry<String,JsonNode>> sequence = object.fields();
			
			while (sequence.hasNext()) {
				Entry<String,JsonNode> e = sequence.next();
				
				String fieldName = e.getKey();
				JsonNode value = e.getValue();
				
				if ("@context".equals(fieldName) || "@id".equals(fieldName)) {
					continue;
				}
				
				if ("@graph".equals(fieldName)) {
					
					if (value instanceof ArrayNode) {
						parseGraph((ArrayNode)value);
					} else {
						throw new RDFParseException("@graph must have array value");
					}
					continue;
				}
				Term term = null;
				URI predicate = null;
				if (typeKey.equals(fieldName)) {
					predicate = RDF.TYPE;
				} else {

					term = context==null ? null : context.getTerm(fieldName);
					
					if (term != null) {
						fieldName = term.getExpandedIdValue();
					}
					
					predicate = valueFactory.createURI(fieldName);
				}
				
				// TODO: handle @list
				if (value instanceof ArrayNode) {
					
					
					if ("@list".equals(term.getContainer())) {
						handleListField(subject, predicate, term, (ArrayNode) value);
					} else {
						handleSetField(subject, predicate, term, (ArrayNode) value);
					}
					
					
//					ArrayNode array = (ArrayNode) value;
//					for (int i=0; i<array.size(); i++) {
//						JsonNode element = array.get(i);
//						
//						handleField(subject, predicate, element, term);
//					}
				} else {
					handleField(subject, predicate, value, term);
				}
				
			}
			
			
			
			context = saveContext;
			idKey = saveIdKey;
			valueKey = saveValueKey;
			typeKey = saveTypeKey;
			languageKey = saveLanguageKey;
			
			return subject;
		}

		private void parseGraph(ArrayNode array) throws RDFParseException, RDFHandlerException {
			for (int i=0; i<array.size(); i++) {
				JsonNode node = array.get(i);
				if (node instanceof ObjectNode) {
					parseObject((ObjectNode)node);
				} else {
					throw new RDFParseException("@graph elements must be objects");
				}
			}
			
		}

		private void handleField(Resource subject, URI predicate, JsonNode value, Term term) throws RDFParseException, RDFHandlerException {
			
			Value object = toValue(subject, predicate, term, value);
			
			if (object != null) {
				rdfHandler.handleStatement(valueFactory.createStatement(subject, predicate, object));
			}
			
		}

		private void handleSetField(Resource subject, URI predicate, Term term, ArrayNode array) throws RDFParseException, RDFHandlerException {
			
			for (int i=0; i<array.size(); i++) {
				Value value = toValue(subject, predicate, term, array.get(i));
				Statement s = valueFactory.createStatement(subject, predicate, value);
				rdfHandler.handleStatement(s);
			}
			
		}

		private void handleListField(Resource subject, URI predicate, Term term, ArrayNode array) throws RDFHandlerException, RDFParseException {

			if (rdfHandler instanceof ListHandler) {
				List<Value> valueList = toValueList(subject, predicate, term, array);
				ListHandler listHandler = (ListHandler) rdfHandler;
				listHandler.handleList(subject, predicate, valueList);
			} else {
				BNode bnode = valueFactory.createBNode();
				rdfHandler.handleStatement(valueFactory.createStatement(subject, predicate, bnode));
				for (int i=0; i<array.size(); i++) {
					Value value = toValue(subject, predicate, term, array.get(i));
					Statement st = valueFactory.createStatement(bnode, RDF.FIRST, value);
					rdfHandler.handleStatement(st);

					BNode next = valueFactory.createBNode();
					rdfHandler.handleStatement(valueFactory.createStatement(bnode, RDF.REST, next));
					bnode = next;
				}
			}
			
		}

		private List<Value> toValueList(Resource subject, URI predicate, Term term, ArrayNode array) throws RDFParseException, RDFHandlerException {
			List<Value> list = new ArrayList<>();
			
			for (int i=0; i<array.size(); i++) {
				JsonNode node = array.get(i);
				Value value = toValue(subject, predicate, term, node);
				list.add(value);
			}
			
			return list;
		}

		private Value toValue(Resource subject, URI predicate, Term term, JsonNode value) throws RDFParseException, RDFHandlerException {

			Value object = null;
			if (value instanceof ObjectNode) {
				
				ObjectNode node = (ObjectNode)value;
				
				JsonNode valueNode = node.get(valueKey);
				if (valueNode != null) {
					String valueString = valueNode.asText();
					JsonNode typeNode = node.get(typeKey);
					if (typeNode != null) {
						URI typeIRI = iri(typeNode.asText());
						object = valueFactory.createLiteral(valueString, typeIRI);
					} else {
						JsonNode languageNode = node.get(languageKey);
						if (languageNode != null) {
							String language = languageNode.asText();
							object = valueFactory.createLiteral(valueString, language);
						} else {
							object = valueFactory.createLiteral(valueString);
						}
					}
				} else {
					
					if (term != null) {
						URI datatype = term.getExpandedType();
						if (datatype != null) {
							object = valueFactory.createLiteral(node.asText(), datatype);
						} else {
							String language = term.getLanguage();
							if (language != null) {
								object = valueFactory.createLiteral(node.asText(), language);
							} else {
								object = parseObject((ObjectNode)value);
							}
						}
					} else {
						object = parseObject((ObjectNode)value);
					}
				} 
			} else if (value instanceof ArrayNode) {
						
					if ("@list".equals(term.getContainer())) {
						handleListField(subject, predicate, term, (ArrayNode) value);
					} else {
						handleSetField(subject, predicate, term, (ArrayNode) value);
					}
					return null;
			
			} else if (value.isValueNode()) {
				
				String text = value.asText();
				if (term != null) {
					if ("@id".equals(term.getType())) {
						object = iri(text);
					} else {
						URI datatype = term.getExpandedType();
						if (datatype != null) {
							object = valueFactory.createLiteral(text, datatype);
						} else {
							String language = term.getLanguage();
							if (language != null) {
								object = valueFactory.createLiteral(text, language);
							} else {
								object = valueFactory.createLiteral(text);
							}
						}
					}
				} else {
					object = valueFactory.createLiteral(text);
				}
				
			} else {
				throw new RDFParseException("Invalid node type");
			}
			return object;
		}

		private URI iri(String idValue) throws RDFParseException {
			
			if (
				idValue.startsWith("http://") ||
				idValue.startsWith("https://") ||
				idValue.startsWith("urn:")
			) {
				return valueFactory.createURI(idValue);
			}

			int colon = idValue.indexOf(':');
			if (colon >= 0) {
				if (context == null) {
					throw new RDFParseException("Cannot expand IRI '" + idValue + "' because context is not defined");
				}
				String prefix = idValue.substring(0, colon);
				Term term = context.getTerm(prefix);
				if (term != null) {
					String localName = idValue.substring(colon+1);
					String namespace = term.getExpandedIdValue();
					if (namespaceManager != null) {
						Namespace ns = namespaceManager.findByName(namespace);
						if (ns == null) {
							namespaceManager.add(prefix, namespace);
						}
					}
					return valueFactory.createURI(namespace + localName);
				}
				return valueFactory.createURI(idValue);
			} else {
				Term term = context.getTerm(idValue);
				if (term != null) {
					return term.getExpandedId();
				}
				
				return valueFactory.createURI(baseURI + idValue);
			}
		}
	}
	
	

}
