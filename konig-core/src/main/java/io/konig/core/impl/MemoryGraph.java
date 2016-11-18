package io.konig.core.impl;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.Transaction;
import io.konig.core.TransactionWorker;
import io.konig.core.Traversal;
import io.konig.core.UidGenerator;
import io.konig.core.Vertex;

/**
 * An in-memory Graph
 * @author Greg
 *
 */
public class MemoryGraph implements Graph, Transaction {
	
	private static final UidGenerator uid = new UidGeneratorImpl();
	private ValueFactory valueFactory = new ValueFactoryImpl();
	
	private Resource id;
	private Map<String, Vertex> bnodeMap = new HashMap<>();
	private Map<Resource, Vertex> vertexMap = new LinkedHashMap<Resource, Vertex>();
	private List<Edge> txn;
	private List<Edge> sink;
	private Transaction.Status status = Transaction.Status.CLOSED;
	private List<TransactionWorker> workerList = new ArrayList<TransactionWorker>();
	private NamespaceManager nsManager;
	
	private boolean copyEdgeAttributes=false;
	
	public MemoryGraph() {
		
	}
	
	

	public boolean isCopyEdgeAttributes() {
		return copyEdgeAttributes;
	}



	public void setCopyEdgeAttributes(boolean copyEdgeAttributes) {
		this.copyEdgeAttributes = copyEdgeAttributes;
	}



	public Vertex vertex(Resource id) {
		
		if (id instanceof ResourceVertex) {
			ResourceVertex rv = (ResourceVertex) id;
			Vertex v = rv.getVertex();
			if (v.getGraph() == this) {
				return v;
			}
		}

		BNode bnode = null;
		if (id instanceof BNode) {
			bnode = (BNode) id;
		}
		
		if (bnode != null) {
			Vertex v = bnodeMap.get(bnode.getID());
			if (v != null) {
				return v;
			}
			id = new BNodeImpl(uid.next());
		}
		
		Vertex v = vertexMap.get(id);
		if (v == null) {
			v = new VertexImpl(this, id);
			vertexMap.put(id, v);
			if (bnode != null) {
				bnodeMap.put(bnode.getID(), v);
			}
		}
		return v;
	}

	public Vertex getVertex(Resource id) {
		return vertexMap.get(id);
	}

	public Vertex vertex(String id) {
		Resource node = createResource(id);
		return vertex(node);
	}
	
	private Resource createResource(String id) {
		return id.startsWith("_:") ?
			valueFactory.createBNode(id.substring(2)) :
			valueFactory.createURI(id);
	}

	public Vertex getVertex(String id) {
		
		return getVertex(createResource(id));
	}
	
	private ResourceVertex wrap(Resource object) {

		if (object instanceof ResourceVertex) {
			ResourceVertex rv = (ResourceVertex) object;
			Vertex v = rv.getVertexImpl();
			if (v.getGraph() == this) {
				return rv;
			}
			object = v.getId();
		}
		
		if (object instanceof URI) {
			Vertex objectVertex = vertex((Resource) object);
			return (ResourceVertex) objectVertex.getId();
		}
		if (object instanceof BNode) {
			Vertex objectVertex = vertex((BNode)object);
			return (ResourceVertex)objectVertex.getId();
		}
		
		return null;
		
	}
	public Edge edge(Resource subject, URI predicate, Value object) {
		return edge(subject, predicate, object, null);
	}

	public Edge edge(Resource subject, URI predicate, Value object, Resource context) {
		
		ResourceVertex s = (ResourceVertex) wrap(subject);
		ResourceVertex o = null;
		if (object instanceof Resource) {
			object = o = wrap((Resource)object);
		}
		
		
		Edge e = new EdgeImpl(s, predicate, object, context);
		s.getVertexImpl().add(e);
		
		if (o != null) {
			o.getVertexImpl().add(e);
		}
		if (sink != null) {
			sink.add(e);
		}
		
		return e;
	}

	public Edge edge(Vertex subject, URI predicate, Vertex object) {
		return edge(subject.getId(), predicate, object.getId());
	}

	public Collection<Vertex> vertices() {
		return vertexMap.values();
	}

	public Traversal v(Resource subject) {
		return vertex(subject).asTraversal();
	}

	public Transaction tx() {
		return this;
	}

	public void open() {
		txn = sink = new ArrayList<Edge>();
		status = Transaction.Status.OPEN;
		
	}

	public Status getStatus() {
		return status;
	}

	public void commit() {
		if (status != Status.OPEN) {
			// We only allow commit when a transaction is open.
			// Consider throwing an exception here.
			return;
		}
		status = Status.VOTE;
		
		if (txn!=null && !txn.isEmpty()) {
			sink = new ArrayList<Edge>();

			while (status == Status.VOTE) {
				for (TransactionWorker worker : workerList) {
					worker.commit(this);
					if (status == Status.ROLLBACK) {
						break;
					}
				}
				if (sink.isEmpty() && status==Status.VOTE) {
					status = Status.COMMIT;
				} else {
					txn = sink;
					sink = new ArrayList<Edge>();
				}
			}
		}
		
		bnodeMap = new HashMap<>();
		
	}

	public void rollback() {
		status = Status.ROLLBACK;
	}

	public List<Edge> asList() {
		return txn;
	}

	public void addWorker(TransactionWorker worker) {
		workerList.add(worker);
	}

	public void removeWorker(TransactionWorker worker) {
		workerList.remove(worker);
	}

	public Edge edge(Edge edge) {
		Edge result = edge(edge.getSubject(), edge.getPredicate(), edge.getObject(), edge.getContext());
		if (copyEdgeAttributes) {
			result.copyAnnotations(edge);
		}
		return result;
	}

	public void remove(Edge edge) {
		
		Vertex subject = vertex(edge.getSubject());
		subject.remove(edge);
		
		if (edge.getObject() instanceof Resource) {
			Vertex object = vertex((Resource)edge.getObject());
			object.remove(edge);
		}
		
	}

	@Override
	public void add(Vertex vertex) {
		
		Set<Entry<URI,Set<Edge>>> out = vertex.outEdges();
		for (Entry<URI,Set<Edge>> e : out) {
			Set<Edge> set = e.getValue();
			for (Edge edge : set) {
				edge(edge);
				Value object = edge.getObject();
				if (object instanceof BNode) {
					Vertex bnode = vertex((Resource)object);
					add(bnode);
				}
			}
		}
		
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		for (Vertex v : vertices()) {
			
			// Only display BNodes that have no incoming edges
			Resource id = v.getId();
			if (id instanceof BNode) {
				Set<Edge> inSet = v.inEdgeSet();
				if (!inSet.isEmpty()) {
					continue;
				}
			}
			
			// At this point, we've determined that the vertex
			// is an IRI or a BNode with no incoming edges.
			
			buffer.append(v.toString());
			Graph namedGraph = v.asNamedGraph();
			if (namedGraph != null) {
				buffer.append("BEGIN GRAPH\n");
				buffer.append(namedGraph.toString());
				buffer.append("END GRAPH\n");
			}
			
		}
		
		return buffer.toString();
	}
	
	

	@Override
	public Resource getId() {
		return id;
	}

	@Override
	public void setId(Resource id) {
		this.id = id;
	}

	@Override
	public Vertex vertex() {

		BNode id = new BNodeImpl(uid.next());
		Vertex vertex = new VertexImpl(this, id);
		id = (BNode) vertex.getId();
		
		bnodeMap.put(id.stringValue(), vertex);
		vertexMap.put(id, vertex);
		
		return vertex;
	}

	@Override
	public GraphBuilder builder() {
		return new GraphBuilder(this);
	}
	
	public boolean add(Statement statement) {
		return add(new EdgeImpl(statement.getSubject(), statement.getPredicate(), statement.getObject()));
	}

	@Override
	public boolean add(Edge edge) {
		edge(edge);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Edge> sequence) {
		for (Edge e : sequence) {
			edge(e);
		}
		return true;
	}

	@Override
	public void clear() {
		bnodeMap = new HashMap<>();
		vertexMap = new LinkedHashMap<Resource, Vertex>();
		txn = null;
		sink=null;
		status = Transaction.Status.CLOSED;
	}

	@Override
	public boolean contains(Object obj) {
		Edge edge = null;
		if (obj instanceof Edge) {
			edge = (Edge) obj;
		} else if (obj instanceof Statement) {
			Statement s = (Statement) obj;
			edge = new EdgeImpl(s.getSubject(), s.getPredicate(), s.getObject());
		}
		if (edge != null) {
			Resource subject = edge.getSubject();
			
			Vertex subjectVertex = getVertex(subject);
			if (subjectVertex != null) {
				return subjectVertex.hasEdge(edge);
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Object obj : collection) {
			if (!contains(obj)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return vertexMap.isEmpty();
	}

	@Override
	public Iterator<Edge> iterator() {
		return new EdgeIterator(this);
	}

	@Override
	public boolean remove(Object obj) {
		if (obj instanceof Edge) {
			remove((Edge) obj);
		}
		if (obj instanceof Statement) {
			Statement s = (Statement) obj;
			remove(new EdgeImpl(s.getSubject(), s.getPredicate(), s.getObject()));
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		for (Object obj : collection) {
			remove(obj);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		int count = 0;
		Iterator<Edge> sequence = iterator();
		while (sequence.hasNext()) {
			sequence.next();
			count++;
		}
		return count;
	}

	@Override
	public Object[] toArray() {
		List<Edge> list = new ArrayList<>();
		Iterator<Edge> sequence = iterator();
		while (sequence.hasNext()) {
			list.add(sequence.next());
		}
		
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}
	
	static class StatementIterator implements Iterator<Edge> {
		
		private Edge current;
		private Iterator<Vertex> vertexSequence;
		private Iterator<Entry<URI, Set<Edge>>> predicateIterator;
		private Iterator<Edge> edgeSequence;
		

		public StatementIterator(Iterator<Vertex> vertexSequence) {
			this.vertexSequence = vertexSequence;
			lookAhead();
		}

		private void lookAhead() {
			current = null;
			if (edgeSequence != null) {
				while (edgeSequence.hasNext()) {
					current = edgeSequence.next();
					return;
				}
				edgeSequence = null;
			} 
			
			if (predicateIterator!=null) {
				while (predicateIterator.hasNext()) {
					edgeSequence = predicateIterator.next().getValue().iterator();
					while (edgeSequence.hasNext()) {
						current = edgeSequence.next();
						return;
					}
					edgeSequence = null;
				}
				predicateIterator = null;
			}
			
			while (vertexSequence.hasNext()) {
				Vertex v = vertexSequence.next();
				predicateIterator = v.outEdges().iterator();
				while (predicateIterator.hasNext()) {
					edgeSequence = predicateIterator.next().getValue().iterator();
					while (edgeSequence.hasNext()) {
						current = edgeSequence.next();
						return;
					}
					edgeSequence = null;
				}
				predicateIterator = null;
			}
			
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public Edge next() {
			Edge result = current;
			lookAhead();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
	}

	@Override
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	@Override
	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	@Override
	public boolean contains(Resource subject, URI predicate, Value object) {
		EdgeImpl s = new EdgeImpl(subject, predicate, object);
		return contains(s);
	}



	@Override
	public void remove(Vertex v) {
		if (v == null) {
			return;
		}
		
		Resource id = v.getId();
		
		Graph g = v.getGraph();
		if (g != this) {
			v = getVertex(id);
			id = v.getId();
		}
		
		vertexMap.remove(id);
		
		// Remove incoming edges
		
		Set<Entry<URI, Set<Edge>>> inSet = v.inEdges();
		for (Entry<URI,Set<Edge>> entry : inSet) {
			Iterator<Edge> iterator = entry.getValue().iterator();
			URI predicate = entry.getKey();
			while (iterator.hasNext()) {
				Edge edge = iterator.next();

				iterator.remove();
				Resource subjectId = edge.getSubject();
				Vertex subject = getVertex(subjectId);
				
				if (subject != null) {
					Set<Edge> set = subject.outProperty(predicate);
					set.remove(edge);
					edge = null;
				}
			}
		}
		
		// Remove outgoing edges, and orphaned object nodes
		Set<Entry<URI,Set<Edge>>> outSet = v.outEdges();
		for (Entry<URI,Set<Edge>> entry : outSet) {
			Iterator<Edge> iterator = entry.getValue().iterator();
			URI predicate = entry.getKey();
			while (iterator.hasNext()) {
				Edge edge = iterator.next();
				
				Value value = edge.getObject();
				
				if (value instanceof Resource) {
					iterator.remove();
					Resource objectId = (Resource) value;
					Vertex object = getVertex(objectId);
					if (object != null) {
						Set<Edge> set = object.inProperty(predicate);
						set.remove(edge);
						if (object.isOrphan()) {
							remove(object);
						}
					}
				}				
			}
		}
	}



	@Override
	public void remove(Resource resource) {
		remove(getVertex(resource));
	}



	@Override
	public Set<URI> lookupLocalName(String localName) {
		Set<URI> result = new LinkedHashSet<>();
		
		for (Edge e : this) {
			matchLocalName(result, localName, e.getSubject());
			matchLocalName(result, localName, e.getPredicate());
			matchLocalName(result, localName, e.getObject());
		}
		
		return result;
	}



	private void matchLocalName(Set<URI> result, String localName, Value value) {
		if (value instanceof URI) {
			URI uri = (URI) value;
			if (localName.equals(uri.getLocalName())) {
				result.add(uri);
			}
		}
		
	}
	

}