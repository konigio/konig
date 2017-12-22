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

public class GeneralAdditiveExpression extends AbstractFormula implements AdditiveExpression {

	private MultiplicativeExpression left;
	private List<Addend> addendList = new ArrayList<>();

	public GeneralAdditiveExpression(MultiplicativeExpression left) {
		this.left = left;
	}
	
	static public GeneralAdditiveExpression wrap(PrimaryExpression primary) {

		UnaryExpression unary = new UnaryExpression(primary);
		MultiplicativeExpression mult = new MultiplicativeExpression(unary);
		return new GeneralAdditiveExpression(mult);
	}

	public MultiplicativeExpression getLeft() {
		return left;
	}

	public void add(Addend addend) {
		addendList.add(addend);
	}

	public List<Addend> getAddendList() {
		return addendList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		left.print(out);
		for (Addend addend : addendList) {
			addend.print(out);
		}
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		left.dispatch(visitor);
		for (Addend a : addendList) {
			a.dispatch(visitor);
		}
		visitor.exit(this);
		
	}

}
