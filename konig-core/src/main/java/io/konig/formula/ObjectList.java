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

public class ObjectList extends AbstractFormula {
	
	private List<Expression> list;
	
	public ObjectList(List<Expression> list) {
		this.list = list;
	}
	
	public List<Expression> getExpressions() {
		return list;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		String comma = "";
		for (Expression e : list) {
			out.print(comma);
			e.print(out);
			comma = ",";
		}

	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (Expression e : list) {
			e.dispatch(visitor);
		}
		visitor.exit(this);

	}

	@Override
	public ObjectList deepClone() {
		List<Expression> cloneList = new ArrayList<>();
		for (Expression e : list) {
			cloneList.add((Expression)e.deepClone());
		}
		return new ObjectList(cloneList);
	}

}
