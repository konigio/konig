package io.konig.transform.sql.query;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.ShapePath;

/**
 * A structure that provides information about one table participating in a join.
 * The structure specifies a constraint on one of the following possible elements:
 * <ul>
 * <li> "id" property
 * <li> Unique key property
 * <li> IRI template
 * </ul>
 * 
 * @author Greg McFall
 *
 */
public class JoinElement extends AbstractPrettyPrintable {
	
	private ShapePath shapePath;
	private TableName tableName;
	
	public JoinElement(ShapePath shapePath, TableName tableName) {
		this.shapePath = shapePath;
		this.tableName = tableName;
	}

	public ShapePath getShapePath() {
		return shapePath;
	}

	public TableName getTableName() {
		return tableName;
	}
	
	/**
	 * Create a ValueExpression that represents the value from this element's table that must match a value from the 
	 * other table.
	 */
	public ValueExpression valueExpression() {
		return null;
	}
	
	public void print(PrettyPrintWriter out) {

		out.beginObject(this);

		out.beginObjectField("shapePath", shapePath);
		out.field("path", shapePath.getPath());
		out.field("shape", shapePath.getShape().getId());
		out.endObjectField(shapePath);
		
		out.field("tableName.fullName", tableName.getFullName());
		
		printFields(out);
		
		out.endObject();
	}

	protected void printFields(PrettyPrintWriter out) {}
}
