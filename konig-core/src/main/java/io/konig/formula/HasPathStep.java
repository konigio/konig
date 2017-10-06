package io.konig.formula;

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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class HasPathStep extends PathStep {
	
	private List<PredicateObjectList> constraints;

	public HasPathStep(List<PredicateObjectList> constraints) {
		this.constraints = constraints;
	}

	public List<PredicateObjectList> getConstraints() {
		return constraints;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print('[');
		String delim = "";
		for (PredicateObjectList po : constraints) {
			out.print(delim);
			po.print(out);
			delim = "; ";
		}
		out.print(']');
		
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (PredicateObjectList po : constraints) {
			po.dispatch(visitor);
		}
		visitor.exit(this);
		
	}

}
