package io.konig.transform.sql.query;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.transform.TransformFrame;

public class SqlFrame {
	
	private TransformFrame transformFrame;
	private List<SqlAttribute> attributes = new ArrayList<>();
	private List<JoinInfo> tableList;

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
	
}
