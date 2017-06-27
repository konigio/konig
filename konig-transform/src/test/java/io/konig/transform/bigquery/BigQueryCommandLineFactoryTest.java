package io.konig.transform.bigquery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.sql.query.BigQueryCommandLine;
import io.konig.transform.factory.AbstractShapeRuleFactoryTest;
import io.konig.transform.rule.ShapeRule;

public class BigQueryCommandLineFactoryTest extends AbstractShapeRuleFactoryTest {
	private BigQueryCommandLineFactory factory = new BigQueryCommandLineFactory();
	
	@Before
	public void setUp() throws Exception {
		useBigQueryTransformStrategy();
	}
	
	@Test
	public void testUpdateCommand() throws Exception {
		load("src/test/resources/konig-transform/transform-update");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeRule shapeRule = createShapeRule(shapeId);
		
		BigQueryCommandLine cmd = factory.updateCommand(shapeRule);
		assertTrue(cmd != null);
		
		String expected =
			"bq query --project_id={gcpProjectId} --use_legacy_sql=false "
			+ "$'UPDATE schema.PersonCurrent AS b  "
			+ "SET givenName = a.first_name  "
			+ "FROM schema.OriginPersonShape AS a  "
			+ "WHERE b.id=CONCAT(\"http://example.com/person/\", CAST(a.person_id AS STRING))'";
		
		String actual = cmd.toString().trim();
		assertEquals(expected, actual);
	}
	

	@Test
	public void testInsertCommand() throws Exception {

		load("src/test/resources/konig-transform/flattened-field");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeRule shapeRule = createShapeRule(shapeId);
		
		BigQueryCommandLine cmd = factory.insertCommand(shapeRule);
		assertTrue(cmd != null);
		
		
		String expected = 
			"bq query --project_id={gcpProjectId} --use_legacy_sql=false "
			+ "$'INSERT schema.Person (address)  "
			+ "SELECT  STRUCT( zipCode AS postalCode ) AS address  "
			+ "FROM schema.OriginPersonShape'" 
			;
		String actual = cmd.toString().trim();
		
		assertEquals(expected, actual);
		
	}
}
