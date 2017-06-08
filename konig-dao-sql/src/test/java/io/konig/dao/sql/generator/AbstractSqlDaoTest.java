package io.konig.dao.sql.generator;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sun.codemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

abstract public class AbstractSqlDaoTest {
	
	protected File destDir = new File("target/test/sql-dao");

	protected NamespaceManager nsManager = new MemoryNamespaceManager();
	protected MemoryGraph graph = new MemoryGraph(nsManager);
	protected ShapeManager shapeManager = new MemoryShapeManager();
	
	protected BasicSqlDataSourceProcessor processor = new BasicSqlDataSourceProcessor("com.example.sql");
	protected SqlShapeReadServiceGenerator generator = new SqlShapeReadServiceGenerator(processor);


	@Before
	public void setUp() {
		IOUtil.recursiveDelete(destDir);
		destDir.mkdirs();
	}
	
	protected URI uri(String value) {
		return new URIImpl(value);
	}
	

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {

		GcpShapeConfig.init();
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
	}

}
