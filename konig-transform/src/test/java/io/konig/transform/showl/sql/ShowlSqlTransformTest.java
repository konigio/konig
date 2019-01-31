package io.konig.transform.showl.sql;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.MappingStrategy;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;

public class ShowlSqlTransformTest {

	private ShowlManager showlManager;
	private NamespaceManager nsManager;
	private MappingStrategy strategy = new MappingStrategy();
	private ShowlSqlTransform transform = new ShowlSqlTransform();
	
	private InsertStatement insert(String resourcePath, String shapeId) throws Exception {

		GcpShapeConfig.init();
		load(resourcePath);
		URI shapeIri = uri(shapeId);
		ShowlNodeShape node = showlManager.getNodeShape(shapeIri).findAny();

		strategy.selectMappings(node);
		
		return transform.createInsert(node, GoogleBigQueryTable.class);
	}
	
	@Ignore
	public void testTabular() throws Exception {
		
		InsertStatement insert = insert(
			"src/test/resources/ShowlSqlTransformTest/tabular", 
			"http://example.com/ns/shape/PersonTargetShape");
		
		assertEquals("schema.PersonTarget", insert.getTargetTable().getTableName());
		
		List<ColumnExpression> columns = insert.getColumns();
		assertEquals(2, columns.size());
		SelectExpression select = insert.getSelectQuery();
		
		List<ValueExpression> values = select.getValues();
		assertEquals("CONCAT(\"http://example.com/person/\", a.person_id) AS id", values.get(0).toString());
		assertEquals("a.first_name AS givenName", values.get(1).toString());
		
		List<TableItemExpression> from = select.getFrom().getTableItems();
		assertEquals(1, from.size());
		
		assertEquals("schema.PersonSource AS a", from.get(0).toString());
		
		
	}
	
	@Test
	public void testTabularJoin() throws Exception {
		
		InsertStatement insert = insert(
			"src/test/resources/ShowlSqlTransformTest/tabular-join-transform", 
			"http://example.com/ns/shape/PersonTargetShape");
		
		
		
		System.out.println(insert.toString());
		
	}

	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String filePath) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(filePath);
		nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph(nsManager);
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
		showlManager = new ShowlManager();
		showlManager.load(shapeManager, reasoner);
		
	}

}
