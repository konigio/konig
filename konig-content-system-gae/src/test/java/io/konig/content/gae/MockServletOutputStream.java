package io.konig.content.gae;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class MockServletOutputStream extends ServletOutputStream {
	public ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public void write(int i) throws IOException {
		baos.write(i);
	}

	public String toString() {
		return new String(baos.toByteArray());
	}
}