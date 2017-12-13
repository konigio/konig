package io.konig.transform.rule;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.transform.proto.PropertyModel;

public class AnyValuePropertyRule implements PropertyRule {
	private PropertyRule collection;

	public AnyValuePropertyRule(PropertyRule collection) {
		this.collection = collection;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.beginObjectField("collection", collection);
		collection.print(out);
		out.endObjectField(collection);
		out.endObject();

	}

	@Override
	public int compareTo(PropertyRule other) {
		if (other == this) {
			return 0;
		}
		DataChannel channel = getDataChannel();
		String channelName = channel==null ? "" : channel.getName();
		DataChannel otherChannel = other.getDataChannel();
		String otherChannelName = otherChannel == null ? "" : otherChannel.getName();
		if (channelName ==null) {
			channelName = "";
		}
		if (otherChannelName==null) {
			otherChannelName = "";
		}
		
		int result = channelName.compareTo(otherChannelName);
		if (result == 0) {
			result = getPredicate().getLocalName().compareTo(other.getPredicate().getLocalName());
			if (result == 0) {
				result = getPredicate().stringValue().compareTo(other.getPredicate().stringValue());
			}
		}
		return result;
	}

	@Override
	public ShapeRule getContainer() {
		
		return collection.getContainer();
	}

	@Override
	public String simplePath() {
		return collection.simplePath();
	}

	@Override
	public PropertyModel getSourcePropertyModel() {
		return collection.getSourcePropertyModel();
	}

	@Override
	public void setSourcePropertyModel(PropertyModel sourcePropertyModel) {
		collection.setSourcePropertyModel(sourcePropertyModel);

	}

	@Override
	public void setContainer(ShapeRule container) {
		collection.setContainer(container);

	}

	@Override
	public DataChannel getDataChannel() {
		return collection.getDataChannel();
	}

	@Override
	public ShapeRule getNestedRule() {
		return collection.getNestedRule();
	}

	@Override
	public URI getPredicate() {
		return collection.getPredicate();
	}

	public PropertyRule getCollection() {
		return collection;
	}

}
