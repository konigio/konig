package io.konig.transform.sql.query;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.IriTemplateInfo;
import io.konig.transform.ShapePath;

public class JoinIriTemplate extends JoinElement {
	
	private IriTemplateInfo templateInfo;

	public JoinIriTemplate(ShapePath shapePath, TableName tableName, IriTemplateInfo templateInfo) {
		super(shapePath, tableName);
		this.templateInfo = templateInfo;
	}

	@Override
	public ValueExpression valueExpression() {
		TableName tableName = getTableName();
		return QueryBuilder.idValue(tableName, templateInfo);
	}

	@Override
	protected void printFields(PrettyPrintWriter out) {
		
		out.field("templatInfo.template", templateInfo.getTemplate().toString());
		
	}

}
