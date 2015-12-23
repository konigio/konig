package io.konig.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
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
		
		ResourceVertex s = (ResourceVertex) wrap(subject);
		ResourceVertex o = null;
		if (object instanceof Resource) {
			object = o = wrap((Resource)object);
		}
		
		
		Edge e = new EdgeImpl(s, predicate, object);
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

	public Edge add(Edge edge) {
		return edge(edge.getSubject(), edge.getPredicate(), edge.getObject());
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
				add(edge);
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
			buffer.append(v.toString());
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
	

}
