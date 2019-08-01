package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlPropertyShapeGroup;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.shacl.PropertyConstraint;

public class UniqueKeyFactory {
	
	private BeamExpressionTransform etran;

	public UniqueKeyFactory(BeamExpressionTransform etran) {
		this.etran = etran;
	}

	public BeamUniqueKeyCollection createKeyList(ShowlNodeShape node) throws BeamTransformGenerationException {
		
		Worker worker = new Worker();
		return worker.createKeyList(node);
	}
	
	private class Worker {
		Map<URI,List<List<URI>>> owlKeyMap = new HashMap<>();
		BeamUniqueKeyCollection keyCollection = new BeamUniqueKeyCollection();
		
		BeamUniqueKeyCollection createKeyList(ShowlNodeShape node) throws BeamTransformGenerationException {
			
			
			for (ShowlPropertyShape p : node.getProperties()) {
				if (
						isShowlUniqueKey(p) ||
						isInverseFunctional(p)
				) {
					singleKey(p);
					
				} else {
					buildOwlKeys(p);
				}
				
			}
		
			return keyCollection;
		}

	

		private void buildOwlKeys(ShowlPropertyShape p) {

			List<List<URI>> owlHasKey = owlHasKey(p.getDeclaringShape());
			if (owlHasKey!=null && !owlHasKey.isEmpty()) {
				URI predicate = p.getPredicate();
				
				for (List<URI> keyElements : owlHasKey) {
					if (keyElements.contains(predicate)) {
						
					}
				}
			}
			
		}



		private boolean hasKey(ShowlPropertyShape p, List<URI> keyElements) {
			
			return false;
		}





		private boolean isInverseFunctional(ShowlPropertyShape p) {
			return etran.getOwlReasoner().isInverseFunctionalProperty(p.getPredicate());
		}


		/**
		 * Assert that a given property is a single key.
		 * If there is already a key that includes the given property, reuse it.
		 * Otherwise, create a new key that includes the given property and no others.
		 * @param p
		 * @throws BeamTransformGenerationException
		 */
		private void singleKey(ShowlPropertyShape p) throws BeamTransformGenerationException {
			
			// Scan the current collection of keys to see if we already have a key that includes p.
			
			ShowlPropertyShapeGroup pGroup = p.asGroup();
			for (BeamUniqueKey key : keyCollection) {
				for (UniqueKeyElement e : key) {
					ShowlPropertyShapeGroup keyGroup = e.getPropertyShape().asGroup();
					if (pGroup == keyGroup) {
						// We found a key that lists p as an element.
						// If it is a single key, we'll just keep it and return immediately.
						// Otherwise, we'll convert this key to a single key by removing the other elements.
						
						if (key.size()==1) {
							// We have already listed p as a single key, so we can return immediately
							return;
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
							return;
						}
					}
				}
			}
			
			// No existing key including p as an element was found.  
			// Create the key now, and add it to the collection.
		
			keyCollection.add(new BeamUniqueKey(element(p)));
			
		}



		private UniqueKeyElement element(ShowlPropertyShape p) throws BeamTransformGenerationException {
			
			RdfJavaType type = etran.getTypeManager().rdfJavaType(p);
			return new UniqueKeyElement(p, type);
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

				Graph graph = etran.getOwlReasoner().getGraph();
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
