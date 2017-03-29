package io.konig.datacatalog;

import java.io.PrintWriter;
import java.io.Writer;

public class PageResponseImpl implements PageResponse {
	private PrintWriter writer;

	public PageResponseImpl(Writer out) {
		writer = (out instanceof PrintWriter) ?
			(PrintWriter) out : new PrintWriter(out);
	}

	@Override
	public PrintWriter getWriter() {
		return writer;
	}

}
