package io.konig.formula;

import java.util.List;

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


import io.konig.core.io.PrettyPrintWriter;

public class PredicateObjectList extends AbstractFormula {
	
	private PathExpression path;
	private ObjectList objectList;

	public PredicateObjectList(PathExpression path, ObjectList objectList) {
		this.path = path;
		this.objectList = objectList;
	}

	public IriValue getVerb() {
		List<PathStep> list = path.getStepList();
		if (list.size() == 1) {
			PathStep step = list.get(0);
			if (step instanceof DirectionStep) {
				DirectionStep dirStep = (DirectionStep) step;
				if (dirStep.getDirection() == Direction.OUT) {
					return dirStep.getTerm();
				}
			}
		}
		return null;
	}

	
	@Override
	public PredicateObjectList clone() {
		return new PredicateObjectList(path.clone(), objectList.clone());
	}
	public PathExpression getPath() {
		return path;
	}

	public ObjectList getObjectList() {
		return objectList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		PathTerm predicate = predicatePath();
		if (predicate != null) {
			predicate.print(out);
		} else {
			path.print(out);
		}
		out.print(' ');
		objectList.print(out);

	}

	private PathTerm predicatePath() {

		if (path.getStepList().size()==1) {
			PathStep step = path.getStepList().get(0);
			if (step instanceof DirectionStep) {
				DirectionStep dirStep = (DirectionStep) step;
				if (dirStep.getDirection()==Direction.OUT) {
					return dirStep.getTerm();
				}
			}
		}
		return null;
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		path.dispatch(visitor);
		objectList.dispatch(visitor);
		visitor.exit(this);

	}

}
