package io.konig.data.app.generator;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.runtime.EntityStructure;
import io.konig.sql.runtime.FieldInfo;
import io.konig.yaml.Yaml;

public class EntityStructureGeneratorTest {

	@Test
	public void test() throws Exception {
		
		Shape shape = new Shape(uri("http://example.com/shape/WorkShape"));
		PropertyConstraint p = new PropertyConstraint(Schema.dateCreated);
		p.setDatatype(XMLSchema.DATETIME);
		shape.add(p);
		
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setTableReference(new BigQueryTableReference(null, "schema", "CreativeWork"));
		shape.addShapeDataSource(table);
		
		EntityStructureGenerator generator = new EntityStructureGenerator();
		
		EntityStructure e = generator.toEntityStructure(shape);
		
		String yaml = Yaml.toString(e);
		
		EntityStructure e2 = Yaml.read(EntityStructure.class, yaml);
		
		List<FieldInfo> fieldList = e2.getFields();
		assertEquals(1, fieldList.size());
		FieldInfo field = fieldList.get(0);
		assertEquals(XMLSchema.DATETIME, field.getFieldType());
	}

	private Resource uri(String value) {
		
		return new URIImpl(value);
	}

}
