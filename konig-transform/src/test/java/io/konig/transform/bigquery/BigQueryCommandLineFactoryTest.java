package io.konig.transform.bigquery;
import static org.junit.Assert.*;
import org.junit.Test;

import org.openrdf.model.URI;

import io.konig.sql.query.BigQueryCommandLine;
import io.konig.transform.factory.AbstractShapeRuleFactoryTest;
import io.konig.transform.rule.ShapeRule;

public class BigQueryCommandLineFactoryTest extends AbstractShapeRuleFactoryTest {
	private BigQueryCommandLineFactory factory = new BigQueryCommandLineFactory();

	@Test
	public void testFlattenedField() throws Exception {

		load("src/test/resources/konig-transform/flattened-field");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeRule shapeRule = createShapeRule(shapeId);
		BigQueryCommandLine cmd = factory.insertCommand(shapeRule);
		assertTrue(cmd != null);
		
	}
}
