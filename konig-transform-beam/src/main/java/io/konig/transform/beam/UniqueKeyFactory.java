package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
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
		Map<ShowlPropertyShape, List<BeamUniqueKey>>  propertyKeyMap = new HashMap<>();
		
		BeamUniqueKeyCollection createKeyList(ShowlNodeShape node) throws BeamTransformGenerationException {
			
			
			BeamUniqueKeyCollection collection = new BeamUniqueKeyCollection();
			for (ShowlPropertyShape p : node.getProperties()) {
				if (isUniqueKey(p)) {
					singleKey(p);
				} else {

					List<List<URI>> owlHasKey = owlHasKey(p);
					// TODO: Build keys from owl:hasKey statements
				}
				
			}
			
			return collection;
		}

	

		private void singleKey(ShowlPropertyShape p) throws BeamTransformGenerationException {
			
			List<BeamUniqueKey> keyList = propertyKeyMap.get(p);
			if (keyList != null && !keyList.isEmpty()) {
				
				// Check whether we already have a single key.
				BeamUniqueKey key = keyList.get(0);
				if (key.size()==1) {
					// We already have a single key
					return;
				}
				
				// Since key.size() > 1, it must be the case that
				// the given property is part of a tuple-key.
				// We now know, that it is also a single-key.
				// There is no reason to keep the tuple-key.
				
				keyList.clear();
				
			} else {
				keyList = new ArrayList<>();
				propertyKeyMap.put(p,  keyList);
			}
			
			keyList.add(new BeamUniqueKey(element(p)));
		}



		private UniqueKeyElement element(ShowlPropertyShape p) throws BeamTransformGenerationException {
			
			RdfJavaType type = etran.getTypeManager().rdfJavaType(p);
			return new UniqueKeyElement(p, type);
		}



		private boolean isUniqueKey(ShowlPropertyShape p) {
			
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

		private List<List<URI>> owlHasKey(ShowlPropertyShape p) {
			URI classId = p.getDeclaringShape().getOwlClass().getId();
			
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
