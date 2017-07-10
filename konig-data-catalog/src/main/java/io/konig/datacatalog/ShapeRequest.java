package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import java.io.File;

import io.konig.shacl.Shape;

public class ShapeRequest extends PageRequest {

	private Shape shape;
	private File exampleDir;
	
	public ShapeRequest(PageRequest base, Shape shape, File exampleDir) {
		super(base);
		this.shape = shape;
		this.exampleDir = exampleDir;
	}

	public Shape getShape() {
		return shape;
	}

	public File getExamplesDir() {
		return exampleDir;
	}
	
}
