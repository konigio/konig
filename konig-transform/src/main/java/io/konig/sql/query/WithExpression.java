package io.konig.sql.query;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.rule.SimpleResultSet;
import io.konig.transform.sql.factory.SqlUtil;

public class WithExpression extends AbstractExpression {

	private SimpleResultSet resultSet;
	
	public WithExpression(SimpleResultSet resultSet) {
		this.resultSet = resultSet;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		URI[] propertyNameList = resultSet.getPropertyNames();
		out.print("WITH ");
		out.print(resultSet.getChannel().getName());
		out.println(" AS (");
		out.pushIndent();
		
		// For now, we'll create our own private OwlReasoner.
		// TODO: Consider passing the OwlReasoner to the constructor.
		MemoryGraph graph = new MemoryGraph();
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		boolean first = true;
		for (Value[] valueArray : resultSet.getValueList()) {
			
			if (!first) {
				out.println();
				out.indent();
				out.println("UNION ALL");
			} else {
				first = false;
			}
			out.indent();
			out.print("SELECT ");
			String comma = "";
			for (int i=0; i<valueArray.length; i++) {
				out.print(comma);
				comma = ", ";
				URI propertyName = propertyNameList[i];
				Value value = valueArray[i];
				try {
					ValueExpression e = SqlUtil.valueExpression(reasoner, value);
					e.print(out);
					out.print(" AS ");
					out.print(propertyName.getLocalName());
				} catch (TransformBuildException e) {
					throw new RuntimeException(e);
				}
			}
		}
		out.println();
		out.popIndent();
		out.println(")");
		
		

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		// TODO Auto-generated method stub

	}

}
