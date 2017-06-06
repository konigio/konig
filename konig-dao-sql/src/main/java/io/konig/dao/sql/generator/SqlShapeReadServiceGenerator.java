package io.konig.dao.sql.generator;

import java.io.Writer;
import java.text.MessageFormat;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;

public class SqlShapeReadServiceGenerator {

	private SqlDataSourceProcessor dsProcessor;
	
	public SqlShapeReadServiceGenerator(SqlDataSourceProcessor dsProcessor) {
		this.dsProcessor = dsProcessor;
	}

	public void generateShapeReaderService(Shape shape, JCodeModel model) throws SqlDaoGeneratorException {
		
		List<DataSource> dsList = dsProcessor.findDataSources(shape);
		for (DataSource ds : dsList) {
			generateServiceForDataSource(model, shape, ds);
		}
	}

	private void generateServiceForDataSource(JCodeModel model, Shape shape, DataSource ds) throws SqlDaoGeneratorException {
		
		String className = dsProcessor.shapeReaderClassName(shape, ds);
		JDefinedClass jClass = model._getClass(className);
		if (jClass == null) {
			try {
				jClass = model._class(className);
			} catch (JClassAlreadyExistsException e) {
				throw new SqlDaoGeneratorException(e);
			}
			
			addExecuteMethod(model, jClass, shape, ds);
			
		}
		
		
	}

	private void addExecuteMethod(JCodeModel model, JDefinedClass jClass, Shape shape, DataSource ds) throws SqlDaoGeneratorException {
		
		if (!(ds instanceof TableDataSource)) {
			String msg = MessageFormat.format("DataSource <{0}> of shape <{1}> is not a TableDataSource.", 
					ds.getId().stringValue(), shape.getId().stringValue());
			throw new SqlDaoGeneratorException(msg);
		} 
		TableDataSource tds = (TableDataSource) ds;
		String tableId = tds.getTableIdentifier();
		
		JType shapeQueryClass = model._ref(ShapeQuery.class);
		JType writerClass = model._ref(Writer.class);
		JType formatClass = model._ref(Format.class);
		
		JMethod method = jClass.method(JMod.PUBLIC, model._ref(Void.TYPE), "execute");
		JVar queryVar = method.param(shapeQueryClass, "query");
		JVar writerVar = method.param(writerClass, "out");
		JVar formatVar = method.param(formatClass, "format");
		
		JBlock body = method.body();
		
		JType builderType = model._ref(StringBuilder.class);
		
		JVar builderVar = body.decl(builderType, "builder").init(JExpr._new(builderType));
		
		JExpression literal = JExpr.lit("SELECT * FROM " + tableId);
		
		body.add(builderVar.invoke("append").arg(literal));
		
	}

}
