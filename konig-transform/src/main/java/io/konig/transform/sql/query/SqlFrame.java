package io.konig.transform.sql.query;

/*
 * #%L
 * Konig Transform
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

import org.openrdf.model.URI;

import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.transform.MappedId;
import io.konig.transform.ShapePath;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformFrame;

public class SqlFrame  {
	
	private TransformFrame transformFrame;
	private List<SqlAttribute> attributes = new ArrayList<>();
	private List<JoinInfo> tableList;
	
	private TableName targetTableName;
	private boolean aliasRequired;

	public SqlFrame(TransformFrame transformFrame) {
		this.transformFrame = transformFrame;
	}

	public void add(SqlAttribute a) {
		attributes.add(a);
	}

	public List<SqlAttribute> getAttributes() {
		return attributes;
	}
	
	public SqlAttribute getAttribute(URI predicate) {
		for (SqlAttribute attr : attributes) {
			if (predicate.equals(attr.getAttribute().getPredicate())) {
				return attr;
			}
		}
		return null;
	}

	public List<JoinInfo> getTableList() {
		return tableList;
	}

	public void setTableList(List<JoinInfo> tableList) {
		this.tableList = tableList;
	}

	public TransformFrame getTransformFrame() {
		return transformFrame;
	}

	public TableNameExpression getTargetTable() {
		return new TableNameExpression(targetTableName.getFullName());
	}


	public TableName getTableName(MappedId mappedId) throws ShapeTransformException {
		ShapePath s = mappedId.getShapePath();
		for (JoinInfo join : tableList) {
			JoinElement left = join.getLeft();
			
			if (left != null) {
				ShapePath p = left.getShapePath();
				if (s.equals(p)) {
					return left.getTableName();
				}
			}
			
			JoinElement right = join.getRight();
			ShapePath p = right.getShapePath();
			if (s.equals(p)) {
				return right.getTableName();
			}
			
		}
		throw new ShapeTransformException("Failed to find TableName for shape: " + transformFrame.getTargetShape().getId().stringValue());
	}

	public boolean isAliasRequired() {
		return aliasRequired || tableList.size()>1;
	}

	public void setAliasRequired(boolean aliasRequired) {
		this.aliasRequired = aliasRequired;
	}

	public TableName getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(TableName targetTableName) {
		this.targetTableName = targetTableName;
	}

	public TableItemExpression getTableItem() {
		return targetTableName.getItem();
	}

	
	
	
	
}
