package io.konig.shacl;

/*
 * #%L
 * konig-shacl
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


import io.konig.core.KonigException;

public class ShapeNotFoundException extends KonigException {
	private static final long serialVersionUID = 1L;

	public ShapeNotFoundException(String shapeId) {
		super("Shape not found: " + shapeId);
	}

	public ShapeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShapeNotFoundException(Throwable cause) {
		super(cause);
	}

}
