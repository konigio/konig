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


import static io.konig.core.io.GraphConstants.BNODE;
import static io.konig.core.io.GraphConstants.GRAPH;
import static io.konig.core.io.GraphConstants.IRI;
import static io.konig.core.io.GraphConstants.LANG;
import static io.konig.core.io.GraphConstants.LITERAL_IRI;
import static io.konig.core.io.GraphConstants.LITERAL_QNAME;
import static io.konig.core.io.GraphConstants.LITERAL_TERM;
import static io.konig.core.io.GraphConstants.PLAIN;
import static io.konig.core.io.GraphConstants.QNAME;
import static io.konig.core.io.GraphConstants.RESOURCE;
import static io.konig.core.io.GraphConstants.TERM;
import static io.konig.core.io.GraphConstants.VERSION;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Term;
import io.konig.core.Vertex;
import io.konig.core.io.GraphWriter.Promise;

public class BinaryGraphWriter {

	private static final Logger logger = LoggerFactory.getLogger(BinaryGraphWriter.class);

	Graph graph;
	Context context;
	Context inverse;
	ByteArrayOutputStream out;
	DataOutputStream data;
	short bnodeCount = 1;
	
	private Map<String, Short> bnodeMap = new HashMap<String, Short>();
	
	private PromiseInfo promiseInfo;
	private List<PromiseInfo> promiseInfoList = new ArrayList<>();
	
	
	public byte[] write(Graph source, Context context) throws KonigWriteException {

		this.graph = source;
		this.context = context;
		
		this.inverse = context.inverse();
		promiseInfo = new PromiseInfo();
		promiseInfoList.add(promiseInfo);

		out = new ByteArrayOutputStream();
		data = new DataOutputStream(out);

		try {
			data.writeShort(VERSION);
			writeString(context.getContextIRI());

			Collection<Vertex> list = graph.vertices();
			
			for (Vertex v : list) {
				writeVertex(v);
			}
			
			data.flush();
		} catch (IOException e) {
			throw new KonigWriteException(e);
		}
		

		byte[] array = out.toByteArray();
		
		deliverPromises(array);
		
		return array;
		
	}
	

	private void writeVertex(Vertex v) throws IOException {
		
		Resource id = v.getId();
		


		// Exclude vertices that have no outgoing edges and which map to a 
		// term in the context.

		Term term = inverse.getTerm(id.stringValue());
		Set<Entry<URI, Set<Edge>>> set = v.outEdges();
		if (term != null) {
			if (term!=null && set.isEmpty()) {
				return;
			}
		}

		int position = out.size();
		promiseInfo.putSubject(id.stringValue(), new Integer(position));
		
		if (id instanceof URI) {
			writeIRI((URI)id, term);
		} else {
			writeBNode((BNode)id);
		}
		

		
		data.writeShort(set.size());	
		
		for (Entry<URI, Set<Edge>> e : set) {
			URI predicate = e.getKey();
			writePredicateIRI(predicate);
			
			Set<Edge> value = e.getValue();
			data.writeShort(value.size());
			
			for (Edge edge : value) {

				logger.debug("WRITE: {} {} {}", id.stringValue(), edge.getPredicate().stringValue(), edge.getObject().stringValue() );
				writeObject(edge.getObject());
			}
			
		}

		Graph namedGraph = v.asNamedGraph();
		if (namedGraph != null) {

			logger.debug("WRITE: GRAPH {}", id.stringValue());

			PromiseInfo oldInfo = promiseInfo;
			promiseInfo = new PromiseInfo();
			promiseInfoList.add(promiseInfo);
			
			Collection<Vertex> list = namedGraph.vertices();
			short size = (short)list.size();
			
			data.writeByte(GRAPH);
			data.writeShort(size);
			for (Vertex vertex : list) {
				writeVertex(vertex);
			}
			
			promiseInfo = oldInfo;
		}
	}


	private void writeObject(Value object) throws IOException {
		
		if (object instanceof URI) {
			writeObjectIRI((URI)object);
		} else if (object instanceof BNode) {
			writeBNode((BNode)object);
		} else {
			writeLiteral((Literal)object);
		}
		
	}


	private void writeLiteral(Literal object) throws IOException {
		
		URI type = object.getDatatype();
		String language = object.getLanguage();
		String value = object.stringValue();
		
		if (language != null) {
			data.writeByte(LANG);
		} else if (type != null) {
			Term term = inverse.getTerm(type.stringValue());
			if (term != null) {
				data.writeByte(LITERAL_TERM);
				data.writeShort(term.getIndex());
			} else {
				String namespace = type.getNamespace();
				term = inverse.getTerm(namespace);
				if (term != null) {
					data.writeByte(LITERAL_QNAME);
					data.writeShort(term.getIndex());
					writeString(type.getLocalName());
				} else {
					data.writeByte(LITERAL_IRI);
					writeString(type.stringValue());
				}
			}
		} else {
			data.writeByte(PLAIN);
		}
		writeString(value);
		
	}


	private void writeObjectIRI(URI object) throws IOException {

		Term term = inverse.getTerm(object.stringValue());
		
		if (term != null) {
			data.writeByte(TERM);
			data.writeShort(term.getIndex());
		} else {

			Integer position = promiseInfo.subjectMap.get(object.stringValue());
			data.writeByte(RESOURCE);

			if (position == null) {
				int mark = out.size();
				promiseInfo.addPromise(mark, (Resource)object);
				data.writeInt(0); // Zero is a placeholder until the promise is fulfilled.
			} else {
				data.writeInt(position.intValue());
			}
		}
		
	}


	private void writePredicateIRI(URI predicate) throws IOException {
		Term term = inverse.getTerm(predicate.stringValue());
		writeIRI(predicate, term);
	}


	private void writeBNode(BNode id) throws IOException {
		data.writeByte(BNODE);
		data.writeShort(shortBNode(id));
	}

	private short shortBNode(BNode bnode) {
		Short id = bnodeMap.get(bnode.getID());
		if (id == null) {
			bnodeCount++;
			bnodeMap.put(bnode.getID(), id=new Short(bnodeCount));
		}
		return id.shortValue();
	}


	private void writeIRI(URI id, Term term) throws IOException {
		
		if (term != null) {
			data.writeByte(TERM);
			data.writeShort(term.getIndex());
		} else {
			String namespace = id.getNamespace();
			term = inverse.getTerm(namespace);
			if (term != null) {
				data.writeByte(QNAME);
				data.writeShort(term.getIndex());
				writeString(id.getLocalName());
			} else {
				data.writeByte(IRI);
				writeString(id.stringValue());
			}
		}
		
		
	}


	private void writeString(String value) throws IOException {
		if (value == null) {
			value = "";
		}
		byte[] array = value.getBytes();
		data.write(array);
		data.writeByte(0);
	}
	

	private void deliverPromises(byte[] array) {
		for (PromiseInfo info : promiseInfoList) {
			info.deliverPromises(array);
		}
	}

	static class PromiseInfo {
		Map<String,Integer> subjectMap = new HashMap<String, Integer>(); 
		Map<String, List<Promise>> promiseMap = new HashMap<String, List<Promise>>();
		
		private void putSubject(String resource, Integer index) {
			subjectMap.put(resource, index);
		}
		

		private void addPromise(int mark, Resource target) {
			Promise promise = new Promise(target, mark);
			String key = target.stringValue();
			List<Promise> list = promiseMap.get(key);
			if (list == null) {
				list = new ArrayList<Promise>();
				promiseMap.put(key, list);
			}
			list.add(promise);
		}


		private void deliverPromises(byte[] array) {
			ByteBuffer buffer = ByteBuffer.wrap(array);
			
			for (Entry<String, List<Promise>> e : promiseMap.entrySet()) {
				String resourceKey = e.getKey();
				Integer position = subjectMap.get(resourceKey);
				
				List<Promise> list = e.getValue();
				for (Promise promise : list) {
					buffer.position(promise.mark);
					buffer.putInt(position);
				}
			}
			
		}
		
	}

}
