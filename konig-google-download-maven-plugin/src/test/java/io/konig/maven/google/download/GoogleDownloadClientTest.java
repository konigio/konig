package io.konig.maven.google.download;

/*
 * #%L
 * Konig Google Download Maven Plugin
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
