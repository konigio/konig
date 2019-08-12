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


/**
 * Encapsulates information about a property that serves as one element of a unique key.
 * @author Greg McFall
 *
 */
public class UniqueKeyElement implements Comparable<UniqueKeyElement> {
	
	private ShowlPropertyShape propertyShape;
	private ShowlUniqueKeyCollection valueKeys;
	private ShowlUniqueKey selectedKey;
	
	public UniqueKeyElement(ShowlPropertyShape propertyShape) {
		this.propertyShape = propertyShape;
	}

	public ShowlPropertyShape getPropertyShape() {
		return propertyShape;
	}

	@Override
	public int compareTo(UniqueKeyElement other) {
		return propertyShape.getPredicate().getLocalName().compareTo(
				other.getPropertyShape().getPredicate().getLocalName());
	}

	/**
	 * Get the collection of keys for the value NodeShape contained within the PropertyShape
	 * referenced by this element.
	 */
	public ShowlUniqueKeyCollection getValueKeys() {
		return valueKeys;
	}
	/**
	 * Set the collection of keys for the value NodeShape contained within the PropertyShape
	 * referenced by this element.
	 */
	public void setValueKeys(ShowlUniqueKeyCollection valueKeys) {
		this.valueKeys = valueKeys;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UniqueKeyElement(propertyShape: ");
		builder.append(propertyShape.getPath());
		if (valueKeys!=null) {
			builder.append(", valueKeys: ");
			builder.append(valueKeys.toString());
		}
		builder.append(')');
		
		return builder.toString();
	}

	public ShowlUniqueKey getSelectedKey() {
		return selectedKey;
	}

	public void setSelectedKey(ShowlUniqueKey selectedKey) {
		this.selectedKey = selectedKey;
	}

}
