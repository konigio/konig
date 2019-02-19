package io.konig.core.showl;

import java.util.Collections;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.shacl.Shape;

public class ExplicitDerivedFromSelector implements ShowlSourceNodeSelector {
	private static final Logger logger = LoggerFactory.getLogger(ExplicitDerivedFromSelector.class);

	@Override
	public Set<Shape> selectCandidateSources(ShowlNodeShape targetShape) {
		Set<Shape> result = targetShape.getShape().getExplicitDerivedFrom();
		if (!result.isEmpty()) {
			return result;
		}
		
		ShowlPropertyShape accessor = targetShape.getAccessor();
		if (accessor != null) {
			for (ShowlPropertyShape candidate : accessor.getProperty().getPropertyShapes()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Adding {} as candidate of {}", candidate.getPath(), targetShape.getPath());
				}
			}
		}
		
		
		return Collections.emptySet();
	}

}