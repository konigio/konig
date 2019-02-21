package io.konig.gcp.deployment;

/*
 * #%L
 * Konig GCP Deployment Manager
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeploymentConfig {
	private Map<String, GcpResource<?>> resources = new LinkedHashMap<>();
	
	public void addResource(GcpResource<?> resource) {
		resources.put(resource.getName(), resource);
	}
	
	public <T> T findResource(String name, Class<T> type) {
		Object result = (Object) resources.get(name);
		return result==null ? null : type.cast(result);
	}

	public List<GcpResource<?>> getResources() {
		return new ArrayList<GcpResource<?>>(resources.values());
	}
	

	

}
