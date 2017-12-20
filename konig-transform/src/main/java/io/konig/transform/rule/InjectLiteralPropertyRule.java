package io.konig.transform.rule;

/*
 * #%L
 * Konig Transform
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


import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

/**
 * A rule which asserts that a given property has a fixed, literal value that 
 * must be injected at runtime.
 * @author Greg McFall
 *
 */
public class InjectLiteralPropertyRule extends FixedValuePropertyRule {

	public InjectLiteralPropertyRule(DataChannel channel, URI predicate, Literal value) {
		super(channel, predicate, value);
	}
	
	

}
