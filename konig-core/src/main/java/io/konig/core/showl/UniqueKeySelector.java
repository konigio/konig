package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;

/**
 * A utility that selects a common unique key across multiple sources
 * @author Greg McFall
 *
 */
public class UniqueKeySelector {
	private OwlReasoner reasoner;
	
	public UniqueKeySelector(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	public Map<ShowlNodeShape, ShowlUniqueKey> selectBestKey(List<ShowlUniqueKeyCollection> collectionList) {

		Map<ShowlNodeShape, ShowlUniqueKey> result = new HashMap<>();
		if (collectionList.isEmpty()) {
			return result;
		}
		ShowlUniqueKeyCollection firstCollection = collectionList.get(0);
		List<ShowlUniqueKey> candidateList = new ArrayList<>(firstCollection);
		for (int i=1; i<collectionList.size(); i++) {
			ShowlUniqueKeyCollection otherCollection = collectionList.get(i);
			Iterator<ShowlUniqueKey> sequence = candidateList.iterator();
			while (sequence.hasNext()) {
				ShowlUniqueKey candidate = sequence.next();
				if (otherCollection.findMatch(candidate) == null) {
					sequence.remove();
				}
			}
		}
		
		ShowlUniqueKey bestKey = bestKey(candidateList);
		if (bestKey!=null) {
			result.put(firstCollection.getNodeShape(), bestKey);
			for (int i=1; i<collectionList.size(); i++) {
				ShowlUniqueKeyCollection otherCollection = collectionList.get(i);
				ShowlUniqueKey match = otherCollection.findMatch(bestKey);
				if (match == null) {
					result.clear();
					return result;
				}
				result.put(otherCollection.getNodeShape(), match);
			}
		}
		return result;
	}

	private ShowlUniqueKey bestKey(List<ShowlUniqueKey> candidateList) {
		if (candidateList.isEmpty()) {
			return null;
		}
		
		
		ShowlUniqueKey best = null;
		
		return 
				(best=idKey(candidateList))!=null ? best :
				(best=numericKey(candidateList))!=null ? best :
				(best=simpleKey(candidateList))!=null ? best :
				smallestKey(candidateList);
	}

	private ShowlUniqueKey smallestKey(List<ShowlUniqueKey> list) {
		int minCount = Integer.MAX_VALUE;
		
		ShowlUniqueKey best = null;
		
		for (ShowlUniqueKey key : list) {
			int count = leafElementCount(key);
			if (count < minCount || (count==minCount && key.compareTo(best)<0)) {
				minCount = count;
				best = key;
			}
		}
		return best;
	}

	private int leafElementCount(ShowlUniqueKey key) {
		int sum = 0;
		for (UniqueKeyElement element : key) {
			sum += leafElementCount(element);
		}
		
		return sum;
	}

	private int leafElementCount(UniqueKeyElement element) {
		
		if (element.getValueKeys() == null) {
			return 1;
		}
		ShowlUniqueKey key = element.getSelectedKey();
		if (key == null) {
			key = bestKey(element.getValueKeys());
			element.setSelectedKey(key);
		}
		
		return leafElementCount(key);
	}

	private ShowlUniqueKey simpleKey(List<ShowlUniqueKey> list) {
		for (ShowlUniqueKey key : list) {
			if (key.size()==1) {
				ShowlPropertyShape p = key.get(0).getPropertyShape();
				URI valueType = p.getValueType(reasoner);
				if (reasoner.isDatatype(valueType)) {
					return key;
				}
			}
		}
		return null;
	}

	private ShowlUniqueKey numericKey(List<ShowlUniqueKey> list) {

		for (ShowlUniqueKey key : list) {
			if (key.size()==1) {
				ShowlPropertyShape p = key.get(0).getPropertyShape();
				URI valueType = p.getValueType(reasoner);
				if (reasoner.isNumericDatatype(valueType)) {
					return key;
				}
			}
		}
		return null;
	}

	private ShowlUniqueKey idKey(List<ShowlUniqueKey> list) {
		for (ShowlUniqueKey key : list) {
			if (key.size()==1 && key.get(0).getPropertyShape().getPredicate().equals(Konig.id)) {
				return key;
			}
		}
		return null;
	}

}
