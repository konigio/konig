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


public class DataAppException extends Exception {
	private static final long serialVersionUID = 1L;
	private int statusCode=500;

	public DataAppException(String msg) {
		super(msg);
	}
	
	public DataAppException(String msg, int status) {
		super(msg);
		statusCode = status;
	}

	public DataAppException(Throwable cause) {
		super(cause);
	}

	public DataAppException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public int getStatusCode() {
		return statusCode;
	}
	
	

}
