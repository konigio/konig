package io.konig.data.app.common;

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


public class GetJob {
	private GetRequest request;
	private DataResponse response;
	private Container container;
	
	public GetJob(GetRequest request, DataResponse response, Container container) {
		this.request = request;
		this.response = response;
		this.container = container;
	}
	

	public GetRequest getRequest() {
		return request;
	}


	public DataResponse getResponse() {
		return response;
	}


	public Container getContainer() {
		return container;
	}


	public void execute() throws DataAppException {
		container.get(request, response);
	}
	
}
