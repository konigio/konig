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

public class MultiplicativeExpression extends AbstractFormula {
	
	private UnaryExpression left;
	private List<Factor> multiplierList = new ArrayList<>();

	public MultiplicativeExpression(UnaryExpression left) {
		this.left = left;
	}

	public UnaryExpression getLeft() {
		return left;
	}

	@Override
	public PrimaryExpression asPrimaryExpression() {
		if (multiplierList==null || multiplierList.isEmpty()) {
			return left.getPrimary();
		}
		return null;
	}
	
	@Override
	public MultiplicativeExpression clone() {
		MultiplicativeExpression other = new MultiplicativeExpression(left.clone());
		for (Factor factor : multiplierList) {
			other.add(factor.clone());
		}
		return other;
	}
	
	public List<Factor> getMultiplierList() {
		return multiplierList;
	}

	public void add(Factor multiplier) {
		multiplierList.add(multiplier);
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		left.print(out);
		for (Factor m : multiplierList) {
			m.print(out);
		}
		
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		left.dispatch(visitor);
		for (Factor f : multiplierList) {
			f.dispatch(visitor);
		}
		visitor.exit(this);
		
	}

}
