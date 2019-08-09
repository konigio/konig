package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.shacl.PropertyConstraint;

public class UniqueKeyFactory {
	
	private OwlReasoner reasoner;
	

	public UniqueKeyFactory(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	public ShowlUniqueKeyCollection createKeyCollection(ShowlNodeShape node) throws ShowlProcessingException {
		
		Worker worker = new Worker();
		return worker.createKeyList(node);
	}
	
	private class Worker {
		Map<URI,List<List<URI>>> owlKeyMap = new HashMap<>();
		ShowlUniqueKeyCollection keyCollection ;
		
		ShowlUniqueKeyCollection createKeyList(ShowlNodeShape node) throws ShowlProcessingException {
			keyCollection = new ShowlUniqueKeyCollection(node);
			
			for (ShowlPropertyShape p : node.getProperties()) {
				
				if (
						Konig.id.equals(p.getPredicate()) ||
						isShowlUniqueKey(p) ||
						isInverseFunctional(p)
				) {
					singleKey(p);
				}
				
			}

			buildOwlKeys(node);
			
		
			for (ShowlUniqueKey key : keyCollection) {
				Collections.sort(key);
				addNestedKeys(key);
			}
			Collections.sort(keyCollection);
		
			return keyCollection;
		}

	
		private void addNestedKeys(ShowlUniqueKey key) throws ShowlProcessingException {
			for (UniqueKeyElement e : key) {
				ShowlPropertyShape p = e.getPropertyShape();
				ShowlNodeShape node = p.getValueShape();
				if (node != null) {
					ShowlUniqueKeyCollection c = UniqueKeyFactory.this.createKeyCollection(node);
					e.setValueKeys(c);
				}
			}
			
		}



		private void buildOwlKeys(ShowlNodeShape node) throws ShowlProcessingException {

			List<List<URI>> owlHasKey = owlHasKey(node);
			if (owlHasKey!=null && !owlHasKey.isEmpty()) {
				for (List<URI> keyPredicates : owlHasKey) {
					if (acceptKey(node, keyPredicates)) {
						ShowlUniqueKey key = createKey(node, keyPredicates);
						addKey(key);
					}
				}
			}
			
		}



		/**
		 * Add the key to the collection only if it is not already in the collection
		 */
		private void addKey(ShowlUniqueKey key) {
		
			Iterator<ShowlUniqueKey> sequence = keyCollection.iterator();
			while (sequence.hasNext()) {
				ShowlUniqueKey other = sequence.next();
				if (key.containsAll(other)) {
					// The new key is a superset of another key that is already in the collection.
					// Thus, the new key is either equal to the other key, or contains additional,
					// unnecessary elements.  
					return;
				}
				if (other.containsAll(key)) {
					// The other key contains unnecessary elements, so we shall remove it.
					sequence.remove();
				}
			}
			
			keyCollection.add(key);
			
		}



		private ShowlUniqueKey createKey(ShowlNodeShape node, List<URI> keyPredicates) throws ShowlProcessingException {
			ShowlUniqueKey key = new ShowlUniqueKey();
			for (URI predicate : keyPredicates) {
				ShowlPropertyShape p = node.getProperty(predicate);
				key.add(element(p));
			}
			
			return key;
		}



		private boolean acceptKey(ShowlNodeShape node, List<URI> keyPredicates) {
			// For now, we only accept a key if there is a direct property for each predicate.
			// In the future, we might want to consider keys that can be derived.
			
			for (URI predicate : keyPredicates) {
				if (node.getProperty(predicate) == null) {
					return false;
				}
			}
			return true;
		}


		private boolean isInverseFunctional(ShowlPropertyShape p) {
			return reasoner.isInverseFunctionalProperty(p.getPredicate());
		}


		/**
		 * Assert that a given property is a single key.
		 * If there is already a key that includes the given property, reuse it.
		 * Otherwise, create a new key that includes the given property and no others.
		 * @param p
		 * @throws ShowlProcessingException
		 */
		private ShowlUniqueKey singleKey(ShowlPropertyShape p) throws ShowlProcessingException {
			
			// Scan the current collection of keys to see if we already have a key that includes p.
			
			ShowlPropertyShapeGroup pGroup = p.asGroup();
			for (ShowlUniqueKey key : keyCollection) {
				for (UniqueKeyElement e : key) {
					ShowlPropertyShapeGroup keyGroup = e.getPropertyShape().asGroup();
					if (pGroup == keyGroup) {
						// We found a key that lists p as an element.
						// If it is a single key, we'll just keep it and return immediately.
						// Otherwise, we'll convert this key to a single key by removing the other elements.
						
						if (key.size()==1) {
							// We have already listed p as a single key, so we can return immediately
							return null;
						} else {
							// p is listed as an element within a tuple key.
							// But now we know that p is also a single key, so there is no point
							// retaining the tuple key.  Thus, we change the tuple key to a single key.
							
							Iterator<UniqueKeyElement> sequence = key.iterator();
							while (sequence.hasNext()) {
								UniqueKeyElement e2 = sequence.next();
								if (e2 != e) {
									sequence.remove();
								}
							}
							return null;
						}
					}
				}
			}
			
			// No existing key including p as an element was found.  
			// Create the key now, and add it to the collection.
			ShowlUniqueKey key = new ShowlUniqueKey(element(p));
			keyCollection.add(key);
			return key;
			
		}



		private UniqueKeyElement element(ShowlPropertyShape p) throws ShowlProcessingException {
			
			return new UniqueKeyElement(p);
		}



		private boolean isShowlUniqueKey(ShowlPropertyShape p) {
			
			PropertyConstraint constraint = p.getPropertyConstraint();
			URI stereotype = constraint==null ? null : constraint.getStereotype();
			if (
				Konig.primaryKey.equals(stereotype) ||
				Konig.syntheticKey.equals(stereotype) ||
				Konig.uniqueKey.equals(stereotype)
			) {
				return true;
			}
			
			return false;
		}

		private List<List<URI>> owlHasKey(ShowlNodeShape node) {
			URI classId = node.getOwlClass().getId();
			
			List<List<URI>> list = owlKeyMap.get(classId);
			if (list == null) {

				Graph graph = reasoner.getGraph();
				Vertex v = graph.getVertex(classId);
				
				if (v != null) {
					List<Vertex> keyList = v.asTraversal().out(OwlVocab.hasKey).toVertexList();
					if (!keyList.isEmpty()) {
						list = new ArrayList<>();
						owlKeyMap.put(classId, list);
						for (Vertex k : keyList) {
							List<Value> valueList = k.asList();
							List<URI> uriList = new ArrayList<>();
							for (Value value : valueList) {
								URI uri = RdfUtil.uri(value);
								if (uri != null) {
										uriList.add(uri);
								}
							}
							list.add(uriList);
						}
					}
				}
			}
			
			return list;
		}
	}


}
