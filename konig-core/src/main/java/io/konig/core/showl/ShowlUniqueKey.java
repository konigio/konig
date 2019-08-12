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
import java.util.List;

/**
 * Encapsulates information about a unique key.
 * 
 * In general, a unique key may consist of multiple properties, hence the ShowlUniqueKey object
 * is defined as a list of UniqueKeyElement entities.
 * 
 * @author Greg McFall
 *
 */
@SuppressWarnings("serial")
public class ShowlUniqueKey extends ArrayList<UniqueKeyElement> implements Comparable<ShowlUniqueKey> {

	public ShowlUniqueKey() {
	}
	
	public ShowlUniqueKey(UniqueKeyElement...e) {
		for (UniqueKeyElement element : e) {
			add(element);
		}
	}
	
	public List<UniqueKeyElement> flatten() throws ShowlProcessingException {
		List<UniqueKeyElement> list = new ArrayList<>();
		addAllElements(list, this);
		return list;
	}

	

	private void addAllElements(List<UniqueKeyElement> list, ShowlUniqueKey key) throws ShowlProcessingException {

		for (UniqueKeyElement element : key) {
			addElement(list, element);
		}
		
	}

	private void addElement(List<UniqueKeyElement> list, UniqueKeyElement element) throws ShowlProcessingException {
		if (element.getValueKeys()==null) {
			list.add(element);
		} else {
			ShowlUniqueKey key = element.getSelectedKey();
			if (key == null) {
				throw new ShowlProcessingException("Selected key not defined for " + element.getPropertyShape().getPath());
			}
			addAllElements(list, key);
		}
		
	}

	@Override
	public int compareTo(ShowlUniqueKey other) {
		int result = size() - other.size();
		if (result == 0) {
			for (int i=0; result==0 && i<size(); i++) {
				ShowlPropertyShape a = get(i).getPropertyShape();
				ShowlPropertyShape b = other.get(i).getPropertyShape();
				result = a.getPredicate().getLocalName().compareTo(b.getPredicate().getLocalName());
			}
		}
		return result;
	}

	public boolean matches(ShowlUniqueKey other) {
		if (other.size() == size()) {
			for (int i=0; i<size(); i++) {
				UniqueKeyElement thisElement = get(i);
				UniqueKeyElement otherElement = other.get(i);
				SynsetProperty thisSynset = thisElement.getPropertyShape().asSynsetProperty();
				SynsetProperty otherSynset = otherElement.getPropertyShape().asSynsetProperty();
				
				if (!thisSynset.getPredicates().contains(otherElement.getPropertyShape().getPredicate()) &&
						!otherSynset.getPredicates().contains(thisElement.getPropertyShape().getPredicate())
				) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean hasRepeatedField() {
		for (UniqueKeyElement element : this) {
			if (element.getPropertyShape().isRepeated()) {
				return true;
			}
		}
		return false;
	}



}
