package io.konig.transform.bigquery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
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
		load("src/test/resources/konig-transform/join-by-id");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeRule shapeRule = createShapeRule(shapeId);
		
		BigQueryCommandLine cmd = factory.updateCommand(shapeRule);
		assertTrue(cmd != null);
		
		String expected =
			"bq query --project_id={gcpProjectId} "
			+ "--use_legacy_sql=false $'UPDATE schema.Person  "
			+ "SET  alumniOf = b.alumniOf,  givenName = a.givenName  "
			+ "FROM   schema.PersonNameShape AS a   "
			+ "JOIN  schema.PersonAlumniOfShape AS b  "
			+ "ON  a.id=b.id'";
		
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
