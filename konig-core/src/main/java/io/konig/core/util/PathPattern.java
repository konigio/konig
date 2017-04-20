package io.konig.core.util;

import org.openrdf.model.Namespace;

/*
 * #%L
 * Konig Core
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


import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.OutStep;
import io.konig.core.path.PathImpl;
import io.konig.core.path.Step;

public class PathPattern {
	private String sourcePrefix;
	private Path path;
	private URI targetClass;
	
	
	public PathPattern(String sourcePrefix, URI targetClass, URI predicate) {
		this.sourcePrefix = sourcePrefix;
		this.targetClass = targetClass;
		this.path = new PathImpl();
		path.out(predicate);
	}
	
	

	public PathPattern(String sourcePrefix, Path path, URI targetClass) {
		this.sourcePrefix = sourcePrefix;
		this.path = path;
		this.targetClass = targetClass;
	}



	public Path getPath() {
		return path;
	}



	public String getSourcePrefix() {
		return sourcePrefix;
	}


	public URI getTargetClass() {
		return targetClass;
	}
	
	

	public boolean matches(String sourcePath) {
		return sourcePath.startsWith(sourcePrefix);
	}
	
	public String transform(String sourcePath, NamespaceManager nsManager) {
		if (sourcePath.startsWith(sourcePrefix)) {
			String rest = sourcePath.substring(sourcePrefix.length());
			String fieldName = StringUtil.camelCase(rest);
			
			StringBuilder builder = new StringBuilder();
			
			for (Step step : path.asList()) {
				if (step instanceof OutStep) {
					OutStep out = (OutStep) step;
					URI predicate = out.getPredicate();
					String curie = RdfUtil.optionalCurie(nsManager, predicate);
					builder.append('/');
					builder.append(curie);
				} else {
					throw new KonigException("Step type not supported: " + step.getClass().getSimpleName());
				}
			}
			
			builder.append('/');
			if (targetClass == null) {
				builder.append(fieldName);
			} else {
				String namespace = targetClass.getNamespace();
				Namespace ns  = nsManager.findByName(namespace);
				if (ns == null) {
					builder.append('<');
					builder.append(namespace);
					builder.append(fieldName);
					builder.append('>');
				} else {
					builder.append(ns.getPrefix());
					builder.append(':');
					builder.append(fieldName);
				}
				
			}
			
			return builder.toString();
			
		}
		
		
		return null;
	}

}
