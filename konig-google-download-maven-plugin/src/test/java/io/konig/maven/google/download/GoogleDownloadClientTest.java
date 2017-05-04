package io.konig.maven.google.download;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;

public class GoogleDownloadClientTest {

	@Ignore
	public void test() throws Exception {
		
		String documentId = "1mhL1hylgRJuMBft0sHg7onKwxscdlIJRuzNlF_iDKK0";
		File saveAs = new File("target/downloads/data-model-template.xlsx");

//		String documentId = "1ba2_4tubQaGsN8_kJw3Gab8sL07g4BZgFUtNSkTuAPY";
//		File saveAs = new File("target/downloads/delivery-excellence-model.xlsx");
		
		saveAs.delete();
		GoogleDownloadClient client = new GoogleDownloadClient();
		client.execute(documentId, saveAs);
		
		assertTrue(saveAs.exists());
	}

}
