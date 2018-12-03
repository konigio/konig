package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.shacl.Shape;

public class ShowlInwardPropertyShape extends ShowlDerivedPropertyShape {

	private static Logger logger = LoggerFactory.getLogger(ShowlInwardPropertyShape.class);
	
	public ShowlInwardPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property) {
		super(declaringShape, property);
		if (logger.isTraceEnabled()) {
			logger.trace("Created {}", this.getPath());
		}
	}

	@Override
	public char pathSeparator() {
		return '^';
	}

	@Override
	public Direction getDirection() {
		return Direction.IN;
	}
	
	@Override
	public ShowlClass getValueType(ShowlManager manager) {
		
		return getProperty().inferDomain(manager);
	}

}
