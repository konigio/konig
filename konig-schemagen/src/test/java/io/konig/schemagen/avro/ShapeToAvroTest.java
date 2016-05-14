package io.konig.schemagen.avro;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.konig.schemagen.avro.impl.SimpleAvroDatatypeMapper;

public class ShapeToAvroTest {

	@Test
	public void test() throws Exception {
		
		File sourceDir = new File("src/test/resources/shapes");
		File targetDir = new File("target/avro");
		
		remove(targetDir);
		
		
		ShapeToAvro generator = new ShapeToAvro(new SimpleAvroDatatypeMapper());
		
		generator.generateAvro(sourceDir, targetDir, targetDir, null);
		
		
		File personFile = new File("target/avro/io.konig.shape.v1.schema.Person.avsc");
		
		assertTrue(personFile.exists());
		
		
		
		
		
	}

	private void remove(File target) {
		if (target.isDirectory()) {
			for (File file : target.listFiles()) {
				remove(file);
			}
		}
		target.delete();
	}

}
