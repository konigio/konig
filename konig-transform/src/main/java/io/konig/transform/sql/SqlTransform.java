package io.konig.transform.sql;

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


import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.model.TNodeShape;

public class SqlTransform {
	private TNodeShape targetShape;
	private InsertStatement insert;
	
	public SqlTransform(TNodeShape targetShape) {
		this.targetShape = targetShape;
	}
	public TNodeShape getTargetShape() {
		return targetShape;
	}
	
	public SelectExpression getSelectExpression() {
		return insert.getSelectQuery();
	}
	
	public InsertStatement getInsert() {
		return insert;
	}
	
	public void setInsert(InsertStatement insert) {
		this.insert = insert;
	}

}
