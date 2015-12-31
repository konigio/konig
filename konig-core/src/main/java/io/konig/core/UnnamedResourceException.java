package io.konig.core;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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
 * An exception that occurs if a resource is expected to be named by a URI but 
 * is instead represented as a BNode.
 * 
 * @author Greg McFall
 *
 */
public class UnnamedResourceException extends KonigException {
	private static final long serialVersionUID = 1L;

	public UnnamedResourceException(String message) {
		super(message);
	}

	public UnnamedResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnnamedResourceException(Throwable cause) {
		super(cause);
	}

}
