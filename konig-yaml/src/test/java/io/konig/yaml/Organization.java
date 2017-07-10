package io.konig.yaml;

/*
 * #%L
 * Konig YAML
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


import java.util.HashMap;
import java.util.Map;

public class Organization {
	
	private ContactPointMap contactPoints;

	public Organization() {
	}

	public Map<String, ContactPoint> getContactPoint() {
		return contactPoints;
	}

	public void setContactPoint(ContactPointMap contactPoints) {
		this.contactPoints = contactPoints;
	}
	
	@SuppressWarnings("serial")
	static class ContactPointMap extends HashMap<String, ContactPoint> {
		
	}

}
