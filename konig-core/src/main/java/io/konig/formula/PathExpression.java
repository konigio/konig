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
import java.util.Iterator;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class PathExpression extends AbstractFormula implements PrimaryExpression {
	
	private List<PathStep> stepList = new ArrayList<>();
	
	public void add(PathStep step) {
		stepList.add(step);
	}
	
	public List<PathStep> getStepList() {
		return stepList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		if (!stepList.isEmpty()) {
			Iterator<PathStep> sequence = stepList.iterator();
			PathStep first = sequence.next();
			Direction dir = first.getDirection();
			if (dir == Direction.IN) {
				dir.print(out);
			}
			out.print(first.getTerm());
			while (sequence.hasNext()) {
				PathStep step = sequence.next();
				step.print(out);
			}
		}

	}

}
