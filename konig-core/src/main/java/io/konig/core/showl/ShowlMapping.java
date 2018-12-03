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


public class ShowlMapping {
	
	private ShowlJoinCondition joinCondition;
	private ShowlPropertyShape leftProperty;
	private ShowlPropertyShape rightProperty;
	
	public ShowlMapping(ShowlJoinCondition joinCondition, ShowlPropertyShape leftProperty,
			ShowlPropertyShape rightProperty) {
		this.joinCondition = joinCondition;
		this.leftProperty = leftProperty;
		this.rightProperty = rightProperty;
		
		leftProperty.addMapping(this);
		rightProperty.addMapping(this);
	}

	public ShowlJoinCondition getJoinCondition() {
		return joinCondition;
	}

	public ShowlPropertyShape getLeftProperty() {
		return leftProperty;
	}

	public ShowlPropertyShape getRightProperty() {
		return rightProperty;
	}
	
	public ShowlPropertyShape findOther(ShowlPropertyShape p) {
		return 
			p==leftProperty ? rightProperty :
			p==rightProperty ? leftProperty :
			null;
	}
	

}
