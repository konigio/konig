package io.konig.activity;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import java.util.Calendar;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.UidGeneratorImpl;
import io.konig.core.vocab.Konig;

public class Activity {
	

	private Resource id;
	private URI type;
	private URI actor;
	private URI instrument;
	private Calendar endTime;
	
	public static URI nextActivityId() {
		String value = Konig.ACTIVIY_BASE_URL + UidGeneratorImpl.getInstance().next();
		
		return new URIImpl(value);
	}
	
	public Activity() {
		
	}
	
	public Activity(Resource id) {
		this.id = id;
	}
	
	public Resource getId() {
		return id;
	}
	public void setId(Resource id) {
		this.id = id;
	}
	public URI getActor() {
		return actor;
	}
	public void setActor(URI actor) {
		this.actor = actor;
	}
	public URI getInstrument() {
		return instrument;
	}
	public void setInstrument(URI instrument) {
		this.instrument = instrument;
	}
	public Calendar getEndTime() {
		return endTime;
	}
	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	
	public void instrument(Class<?> javaClass) {
		URI uri = new URIImpl(Konig.JAVA_NAMESPACE + javaClass.getName());
		setInstrument(uri);
	}
	
	public URI getType() {
		return type;
	}
	public void setType(URI type) {
		this.type = type;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Activity [\n");
		if (type != null) {
			builder.append("  type: ");
			builder.append(type.stringValue());
			builder.append("\n");
		}
		if (id != null) {
			builder.append("  id: ");
			builder.append(id.stringValue());
			builder.append("\n");
		}
		builder.append("]");
		return builder.toString();
	}
	

	
}
