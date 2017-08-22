package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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

import org.openrdf.model.URI;

import io.konig.core.io.ShapeFileFactory;
import io.konig.shacl.Shape;

public class GoogleAnalyticsShapeFileCreator implements ShapeFileFactory {
	private File baseDir;	
	
	public GoogleAnalyticsShapeFileCreator(File baseDir){
		this.baseDir = baseDir;		
	}
	
	@Override
	public File createFile(Shape shape) {
		baseDir.mkdir();
		URI shapeURI = (URI) shape.getId();
		String localName = shapeURI.getLocalName();
		StringBuilder builder = new StringBuilder();
		builder.append(localName);
		builder.append(".sql");			
		return new File(baseDir, builder.toString());
	}
}
