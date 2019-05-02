package io.konig.formula;

import java.io.StringWriter;

import io.konig.core.io.PrettyPrintWriter;

public class FormulaUtil {

	public static String simpleString(Formula formula) {

		StringWriter writer = new StringWriter();
		PrettyPrintWriter pretty = new PrettyPrintWriter(writer);
		pretty.setSuppressContext(true);
		formula.print(pretty);
		pretty.close();
		return writer.toString();
	}

}
