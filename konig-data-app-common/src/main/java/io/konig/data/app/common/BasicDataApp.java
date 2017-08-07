package io.konig.data.app.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/*
 * #%L
 * Konig DAO Core
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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.impl.URIImpl;

import io.konig.core.KonigException;
import io.konig.dao.core.Format;

public class BasicDataApp implements DataApp {
	
	private Map<String,ExtentContainer> containers = new HashMap<>();
	
	public BasicDataApp() {
		
	}
	
	public void addContainer(ExtentContainer container) {
		String slug = container.getSlug();
		if (slug == null) {
			throw new KonigException("slug must be defined for container");
		}
		containers.put(slug, container);
	}
	
	/**
	 * Get the container for a given slug.  Used for testing only.
	 */
	public ExtentContainer getContainerForSlug(String slug) {
		return containers.get(slug);
	}
	
	@Override
	public Collection<ExtentContainer> getContainers() {
		return containers.values();
	}

	@Override
	public GetJob createGetJob(JobRequest jobRequest) throws DataAppException {
		
		MarkedPath path = new MarkedPath(jobRequest.getPath());
		String slug = path.currentElement();
		ExtentContainer container = containers.get(slug);
		
		if (container == null) {
			throw new DataAppException("Not Found", 404);
		}
				
		GetRequest request = new GetRequest();
		request.setFormat(Format.JSONLD);		
		request.setShapeId(container.getDefaultShape());
		
		if (jobRequest.getQueryString() != null) {
			HashMap<String, String> queryParams = new HashMap<String, String>();
			String[] queries = jobRequest.getQueryString().split("&");
			for (String query : queries) {
				String[] queryValue = query.split("=");
				if(queryValue.length >= 2) {
					try {
						queryParams.put(queryValue[0], URLDecoder.decode(queryValue[1], StandardCharsets.UTF_8.name()));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			}
			request.setQueryParams(queryParams);
		} else if (path.hasNext()) {
			String uriValue = path.next();
			request.setIndividualId(new URIImpl(uriValue));
		} else {
			throw new DataAppException("Bad Request: expected a URI at the end of the path or a query string", 400);
		}
				
		DataResponse response = new DataResponse(jobRequest.getWriter());
		
		return new GetJob(request, response, container);
	}

}
