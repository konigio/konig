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


import io.konig.core.io.PrettyPrintWriter;

public class DirectedStep extends AbstractFormula implements PathStep {
	private Direction direction;
	private PathTerm term;

	public DirectedStep(Direction direction, PathTerm term) {
		this.direction = direction;
		this.term = term;
	}

	public Direction getDirection() {
		return direction;
	}

	public PathTerm getTerm() {
		return term;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		if (!(term instanceof VariableTerm)) {
			direction.print(out);
		}
		term.print(out);
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		direction.dispatch(visitor);
		term.dispatch(visitor);
		visitor.exit(this);
	}

	@Override
	public DirectedStep deepClone() {
		return new DirectedStep(direction, (PathTerm) term.deepClone());
	}

}
