package io.konig.dao.sql.generator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;

import com.sun.codemodel.JCodeModel;

import io.konig.shacl.Shape;

public class SqlShapeReadServiceGeneratorTest extends AbstractSqlDaoTest {

	
	@Test
	public void testExecute() throws Exception {
		load("src/test/resources/dao-sql/rename-fields");
		
		URI shapeId = uri("http://example.com/shapes/BqPersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		JCodeModel model = new JCodeModel();
		generator.generateShapeReaderService(shape, model);
		model.build(destDir);
		
		// TODO: add assertions.
	}

}
