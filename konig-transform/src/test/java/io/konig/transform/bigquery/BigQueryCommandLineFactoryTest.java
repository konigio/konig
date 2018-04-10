package io.konig.transform.bigquery;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.sql.query.BigQueryCommandLine;
import io.konig.transform.proto.AbstractShapeModelToShapeRuleTest;
import io.konig.transform.rule.ShapeRule;

public class BigQueryCommandLineFactoryTest extends AbstractShapeModelToShapeRuleTest {
	private BigQueryCommandLineFactory factory = new BigQueryCommandLineFactory();
	
	@Before
	public void setUp() throws Exception {
		useBigQueryTransformStrategy();
	}
	
	@Test
	public void testUpdateCommand() throws Exception {
		load("src/test/resources/konig-transform/transform-update");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		BigQueryCommandLine cmd = factory.updateCommand(shapeRule);
		assertTrue(cmd != null);
		
		String expected =
			"bq query --project_id={gcpProjectId} --use_legacy_sql=false "
			+ "$'UPDATE schema.PersonCurrent AS b  "
			+ "SET givenName = a.first_name  "
			+ "FROM schema.OriginPersonShape AS a  "
			+ "WHERE b.id=CONCAT(\"http://example.com/person/\", CAST(a.person_id AS STRING)) AS id'";
		
		String actual = cmd.toString().trim();
		assertEquals(expected, actual);
	}
	

	@Test
	public void testInsertCommand() throws Exception {

		load("src/test/resources/konig-transform/flattened-field");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		BigQueryCommandLine cmd = factory.insertCommand(shapeRule);
		assertTrue(cmd != null);
		
		
		String expected = 
			"bq query --project_id={gcpProjectId} --use_legacy_sql=false "
			+ "$'INSERT INTO schema.Person (address)  "
			+ "SELECT  STRUCT( zipCode AS postalCode ) AS address  "
			+ "FROM schema.OriginPersonShape'" 
			;
		String actual = cmd.toString().trim();
		
		assertEquals(expected, actual);
		
	}
}
