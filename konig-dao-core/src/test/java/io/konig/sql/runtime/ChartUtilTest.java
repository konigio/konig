package io.konig.sql.runtime;

import static org.junit.Assert.*;

import org.junit.Test;

public class ChartUtilTest {

	@Test
	public void test() {
		
		FieldInfo field = new FieldInfo();
		field.setName("pointsPossible");
		
		String label = ChartUtil.label(field);
		assertEquals("Points Possible", label);
	}

}
