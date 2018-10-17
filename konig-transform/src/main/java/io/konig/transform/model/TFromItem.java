package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
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


public class TFromItem {
	private TNodeShape sourceShape;
	private TJoinCondition joinCondition;
	
	public TFromItem(TNodeShape sourceShape) {
		this.sourceShape = sourceShape;
	}

	public TFromItem(TNodeShape sourceShape, TJoinCondition joinCondition) {
		this.sourceShape = sourceShape;
		this.joinCondition = joinCondition;
	}

	public TNodeShape getSourceShape() {
		return sourceShape;
	}

	public TJoinCondition getJoinCondition() {
		return joinCondition;
	}
	
	

}
