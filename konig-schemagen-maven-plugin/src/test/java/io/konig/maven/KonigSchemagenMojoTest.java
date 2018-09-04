package io.konig.maven;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Before;
import org.junit.Test;


public class KonigSchemagenMojoTest extends AbstractMojoTestCase {
	
	@Before
	public void setUp() throws Exception  {
        super.setUp();
    }
	 
	@Test
	public void testDdlFile() throws Exception {
		
		File pom = new File(getBasedir(), "src/test/resources/ddlFile/pom.xml");
		
		KonigSchemagenMojo mojo = (KonigSchemagenMojo) lookupMojo("generate", pom);
		
		mojo.execute();
	}
	

}
