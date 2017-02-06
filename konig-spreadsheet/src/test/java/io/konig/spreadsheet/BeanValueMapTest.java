package io.konig.spreadsheet;

import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.core.util.BeanValueMap;
import io.konig.core.util.SimpleValueFormat;

public class BeanValueMapTest {

	@Test
	public void test() {
		
		ShapeBean bean = new ShapeBean();
		bean.setClassLocalName("Person");
		bean.setClassNamespacePrefix("schema");
		bean.setShapeLocalName("ReportingPersonShape");
		bean.setShapeNamespacePrefix("shape");
		
		BeanValueMap map = new BeanValueMap(bean);
		
		SimpleValueFormat format = new SimpleValueFormat("{classNamespacePrefix}/{classLocalName}/{shapeNamespacePrefix}/{shapeLocalName}");
		
		String actual = format.format(map);
		
		String expected = "schema/Person/shape/ReportingPersonShape";
		
		assertEquals(expected, actual);
	}

}
