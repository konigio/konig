package io.konig.core.showl;

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

public class ShowlEqualStatement implements ShowlStatement {
	
	private ShowlExpression left;
	private ShowlExpression right;
	
	public ShowlEqualStatement(ShowlExpression left, ShowlExpression right) {
		this.left = left;
		this.right = right;
	}

	public ShowlExpression getLeft() {
		return left;
	}

	public ShowlExpression getRight() {
		return right;
	}
	
	public String toString() {
		return left.displayValue() + " = " + right.displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNode, Set<ShowlPropertyShape> set) {
		left.addDeclaredProperties(sourceNode, set);
		right.addDeclaredProperties(sourceNode, set);
	}

	public ShowlExpression otherExpression(ShowlExpression e) {
		
		return 
			left==e  ? right :
			right==e ? left :
			null;
	}
	
	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		left.addProperties(set);
		right.addProperties(set);
	}

}
