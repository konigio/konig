package io.konig.transform.bigquery;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import io.konig.transform.factory.AbstractShapeRuleFactoryTest;

public class BigQueryTransformGeneratorTest extends AbstractShapeRuleFactoryTest  {
	File outDir = new File("target/test/bigquery-transform");
	BigQueryTransformGenerator generator = new BigQueryTransformGenerator(shapeManager, outDir, owlReasoner);

	@Test
	public void test() throws Throwable {
		FileUtils.deleteDirectory(outDir);
		load("src/test/resources/konig-transform/bigquery-transform");
		generator.generateAll();
		
		List<Throwable> errorList = generator.getErrorList();
		if (errorList != null && !errorList.isEmpty()) {
			throw errorList.get(0);
		}
	}

}
