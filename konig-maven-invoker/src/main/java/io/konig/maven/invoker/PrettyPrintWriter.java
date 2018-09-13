package io.konig.maven.invoker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class PrettyPrintWriter extends PrintWriter {
	
	private int indent;
	private int indentWidth = 3;

	public PrettyPrintWriter(Writer out) {
		super(out);
	}

	public PrettyPrintWriter(OutputStream out) {
		super(out);
	}

	public PrettyPrintWriter(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public PrettyPrintWriter(File file) throws FileNotFoundException {
		super(file);
	}

	public PrettyPrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public PrettyPrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}
	
	public void pushIndent() {
		indent++;
	}
	
	public void popIndent() {
		indent--;
	}
	
	public void indent() {
		for (int i=0; i<indent*indentWidth; i++) {
			print(' ');
		}
	}



}
