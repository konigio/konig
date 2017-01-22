package io.konig.transform.sql.query;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.path.PathFactory;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.transform.PropertyTransform;
import io.konig.transform.ShapeTransformModel;
import io.konig.transform.TransformElement;

public class SqlQueryBuilderTest {
	
	protected NamespaceManager nsManager;
	protected PathFactory pathFactory;
	
	@Before
	public void setUp() {

		nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("alias", "http://example.com/ns/alias/");
		nsManager.add("person", "http://example.com/person/");
		pathFactory = new PathFactory(nsManager);
	}

	@Test
	public void test() throws Exception {
		
		
		Shape targetShape = new Shape();
		targetShape.setBigQueryTableId("registrar.Person");
		targetShape.setNodeKind(NodeKind.IRI);
		
		Shape sourceShape = new Shape();
		sourceShape.setBigQueryTableId("mdm.User");
		sourceShape.setIriTemplate(new IriTemplate("{person}{person_id}"));
		
		ShapeTransformModel model = new ShapeTransformModel(targetShape);
		TransformElement source = new TransformElement(sourceShape);
		model.add(source);
		
		source.add(property("/alias:first_name", "/schema:givenName"));
		source.add(property("/alias:last_name", "/schema:familyName"));
		source.add(property("/alias:address_locality", "/schema:address/schema:addressLocality"));
		source.add(property("/alias:address_region", "/schema:address/schema:addressRegion"));
		
		SqlQueryBuilder builder = new SqlQueryBuilder(nsManager);
		
		BigQueryCommandLine expr = builder.toBigQueryCommandLine(model);
		
		String expected =
			"bq query --project_id=${projectId}"
			+ " --destination_table=registrar.Person"
			+ " --use_legacy_sql=false"
			+ " \"SELECT CONCAT(\"http://example.com/person/\", person_id) AS id,"
			+ " first_name AS givenName, last_name AS familyName,"
			+ " STRUCT(address_locality AS addressLocality,"
			+ " address_region AS addressRegion)"
			+ " AS address FROM mdm.User\"";
		
		assertEquals(expected, expr.toString());
	}
	
	private PropertyTransform property(String source, String target) {
		
		Path sourcePath = pathFactory.createPath(source);
		Path targetPath = pathFactory.createPath(target);
		return new PropertyTransform(sourcePath, targetPath);
	}


}
