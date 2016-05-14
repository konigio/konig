package io.konig.core.binary;

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


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;

public class BinaryGraphWriter {
	
	private static int MAX_BYTE = 2*(Byte.MAX_VALUE+1);
	private static int MAX_SHORT = 2*(Byte.MAX_VALUE+1);

	public void write(Graph graph, DataOutputStream out) throws IOException {
		Worker worker = new Worker(out);
		worker.write(graph);
	}
	
	static class Worker {
		
		private int termSize;
		private int languageSize;
		private Language defaultLanguage;
		
		private Term root;
		private Map<String, Language> languageMap;
		private Map<String, Integer> bnodeMap;
		
		private DataOutputStream out;
		private int iriCount;

		public Worker(DataOutputStream out) {
			this.out = out;
			root = new Term(null);
			languageMap = new HashMap<>();
			bnodeMap = new HashMap<>();
		}
		
		public void write(Graph graph) throws IOException {
			buildDictionary(graph);
			writeHeader();
			writeTerm(root);
			writeLanguageList();
			writeTriples(graph);
		}

		private void writeTriples(Graph graph) throws IOException {
			for (Statement s : graph) {
				writeTriple(s);
			}
			
		}

		private void writeTriple(Statement s) throws IOException {
			
			writeIndividual(s.getSubject());
			Term predicateTerm = find(s.getPredicate().stringValue());
			writeInteger(termSize, predicateTerm.getIndex());

			Value object = s.getObject();
			if (predicateTerm.hasType(BinaryGraph.NODE_PREDICATE_INDIVIDUAL) &&
				predicateTerm.hasType(BinaryGraph.LITERAL)
			) {
				writeGenericObject(predicateTerm, object);
				
			} else if (object instanceof Resource) {
				writeIndividual((Resource)object);
			} else {
				Literal value = (Literal) object;
				BinaryUtil.writeString(out, value.stringValue());
				if (predicateTerm.hasType(BinaryGraph.NODE_PREDICATE_VARIABLE_LANGUAGE)) {
					String languageCode = value.getLanguage();
					Language record = languageMap.get(languageCode);
					writeInteger(languageSize, record==null ? 0 : record.getIndex());
				}
				
				if (predicateTerm.hasType(BinaryGraph.NODE_PREDICATE_MULTIPLE_DATATYPES)) {
					int index = 0;
					URI datatype = value.getDatatype();
					if (datatype != null) {
						Term datatypeTerm = find(datatype.stringValue());
						index = datatypeTerm.getIndex();
					}
					writeInteger(termSize, index);
				}
			}
			
		}

		private void writeGenericObject(Term predicateTerm, Value object) throws IOException {
			if (object instanceof Resource) {
				BinaryUtil.writeUnsignedByte(out, BinaryGraph.NODE_PREDICATE_INDIVIDUAL);
				writeIndividual((Resource)object);
			} else {
				Literal literal = (Literal) object;
				String language = literal.getLanguage();
				if (language != null) {
					Language record = languageMap.get(language);
					if (record == defaultLanguage) {
						BinaryUtil.writeUnsignedByte(out, BinaryGraph.NODE_PREDICATE_LANGUAGE);
						BinaryUtil.writeString(out, literal.stringValue());
					} else {
						BinaryUtil.writeUnsignedByte(out, BinaryGraph.NODE_PREDICATE_VARIABLE_LANGUAGE);
						BinaryUtil.writeString(out, literal.stringValue());
						writeInteger(languageSize, record.getIndex());
					}
				} else {
					URI datatype = literal.getDatatype();
					if (datatype!=null && !datatype.equals(XMLSchema.STRING)) {
						
						if (predicateTerm.hasType(BinaryGraph.NODE_PREDICATE_MULTIPLE_DATATYPES)) {
							BinaryUtil.writeUnsignedByte(out, BinaryGraph.NODE_PREDICATE_MULTIPLE_DATATYPES);
							BinaryUtil.writeString(out, literal.stringValue());
							writeIRI(datatype);
						} else {
							BinaryUtil.writeUnsignedByte(out, BinaryGraph.NODE_PREDICATE_DATATYPE);
							BinaryUtil.writeString(out, literal.stringValue());
						}
						
					} else {
						BinaryUtil.writeUnsignedByte(out, BinaryGraph.NODE_PREDICATE_PLAIN);
						BinaryUtil.writeString(out, literal.stringValue());
					}
				}
			}
		}

		private void writeIndividual(Resource resource) throws IOException {
			if (resource instanceof URI) {
				writeIRI((URI)resource);
			} else if (resource instanceof BNode) {
				int index = bnodeMap.get(resource.stringValue()) + iriCount;
				writeInteger(termSize, index);
			}
			
		}

		private void writeIRI(URI iri) throws IOException {
			Term term = find(iri.stringValue());
			writeInteger(termSize, term.getIndex());
			
		}

		private void writeHeader() throws IOException {
			BinaryUtil.writeUnsignedShort(out, BinaryGraph.VERSION_1);
			BinaryUtil.writeUnsignedByte(out, termSize);
			BinaryUtil.writeUnsignedByte(out, languageSize);
			BinaryUtil.writeString(out, defaultLanguage==null ? "" : defaultLanguage.getValue());
		}

		private void writeTerm(Term term) throws IOException {
			
			Collection<Term> list = term.list();
			if (list == null) {
				writeTerm(term.getLocalName(), term);
			} else {
				
				String name = term.getLocalName();
				if (name == null) {
					name = "";
				}
				if (list.size()==1) {
					StringBuilder builder = new StringBuilder();
					
					while (list!=null && list.size()==1) {
						builder.append(term.getLocalName());
						term = list.iterator().next();
						list = term.list();
					}
				}
				
				writeTerm(name, term);
				if (list != null) {
					writeInteger(termSize, list.size());
					for (Term child : list) {
						writeTerm(child);
					}
				}
			}
		}

		private void writeLanguageList() throws IOException {
			writeInteger(languageSize, languageMap.size());
			int count = 1;
			for (Language language : languageMap.values()) {
				language.setIndex(count++);
				BinaryUtil.writeString(out, language.getValue());
			}
		}

		private void writeTerm(String localName, Term term) throws IOException {
			BinaryUtil.writeUnsignedByte(out, term.getTermType());
			BinaryUtil.writeString(out, localName);
			
			if (term.hasType(BinaryGraph.NODE_PREDICATE_DATATYPE)) {
				Term datatype = term.getDatatype();
				writeTermRef(datatype);
			}
			
		}

		private void writeTermRef(Term term) throws IOException {
			writeInteger(termSize, term.getIndex());
		}

		private void writeInteger(int size, int value) throws IOException {
			
			switch (size) {
			case BinaryGraph.SIZE_BYTE :
				BinaryUtil.writeUnsignedByte(out, value);
				break;
				
			case BinaryGraph.SIZE_SHORT :
				BinaryUtil.writeUnsignedShort(out, value);
				break;
				
			default:
				out.writeInt(value);
			}
			
		}

		private void buildDictionary(Graph graph) {
			
			for (Statement s : graph) {
				handleStatement(s);
			}
			assignTermIndex(root);
			
		}


		private void assignTermIndex(Term parent) {
			int pattern = ~BinaryGraph.NODE_PREFIX;
			
			Collection<Term> list = parent.list();
			iriCount = 1;
			if (list != null) {
				for (Term term : list) {
					int type = term.getTermType();
					if ((type & pattern) != 0) {
						term.setIndex(iriCount++);
					}
					assignTermIndex(term);
				}
			}
			
			int size = iriCount + bnodeMap.size();
			
			termSize = integerSize(size);
			languageSize = integerSize(languageMap.size()+1);
		}

		private int integerSize(int size) {
			return 
				(size < MAX_BYTE) ? BinaryGraph.SIZE_BYTE :
				(size < MAX_SHORT) ? BinaryGraph.SIZE_SHORT :
				BinaryGraph.SIZE_INT;
		}

		private void handleStatement(Statement s) {
			handleIndividual(s.getSubject());
			
			Value object = s.getObject();
			
			Term term = find(s.getPredicate().stringValue(), 0);
			
			if (object instanceof Resource) {
				handleIndividual((Resource)object);
				term.addType(BinaryGraph.NODE_PREDICATE_INDIVIDUAL);
			} else {
				Literal literal = (Literal) s.getObject();
				String language = literal.getLanguage();
				
				if (language == null) {
					Language record = languageMap.get(language);
					if (record == null) {
						record = new Language(language);
						languageMap.put(language, record);
						
					} else {
						record.increment();
					}
					if (defaultLanguage == null) {
						defaultLanguage = record;
					} else if (record.getCount() > defaultLanguage.getCount()) {
						defaultLanguage = record;
					}
					
					Language prior = term.getLanguage();
					if (prior == null) {
						term.addType(BinaryGraph.NODE_PREDICATE_LANGUAGE);
						term.setLanguage(record);
					} else if (prior != record) {
						term.addType(BinaryGraph.NODE_PREDICATE_VARIABLE_LANGUAGE);
						term.removeType(BinaryGraph.NODE_PREDICATE_LANGUAGE);
					}
					
				} else {
					
					URI thisDatatype = literal.getDatatype();
					if (thisDatatype != null && !XMLSchema.STRING.equals(thisDatatype)) {
						URI otherDatatype = term.getDatatypeURI();
						if (otherDatatype == null) {
							term.setDatatypeURI(thisDatatype);
							term.addType(BinaryGraph.NODE_PREDICATE_DATATYPE);
						} else if (!thisDatatype.equals(otherDatatype)) {
							term.addType(BinaryGraph.NODE_PREDICATE_MULTIPLE_DATATYPES);
							term.removeType(BinaryGraph.NODE_PREDICATE_DATATYPE);
						}
					} else {
						term.addType(BinaryGraph.NODE_PREDICATE_PLAIN);
					}
				}
			}
			
		}
		
		private void handleIndividual(Resource r) {
			if (r instanceof URI) {
				find(r.stringValue(), BinaryGraph.NODE_INDIVIDUAL);
			} else {
				String key = r.stringValue();
				if (!bnodeMap.containsKey(key)) {
					Integer value = bnodeMap.size();
					bnodeMap.put(key, value);
				}
			}
		}

		private Term find(String iri) {
			return find(iri);
		}

		private Term find(String iri, int termType) {

			Term node = root;
			TermPart part = new TermPart(iri);
			
			boolean seek = true;
			
			do {
				String localName = part.toString();

				Term child = seek ? node.get(localName) : null;
				if (child == null) {
					seek = false;
					child = new Term(localName);
					node.add(child);
				}
				if (termType > 0) {
					child.addType(part.more() ? BinaryGraph.NODE_PREFIX : termType);
				}
				
			} while (part.next());
			
			return node;
		}
		
		 
	}
}
