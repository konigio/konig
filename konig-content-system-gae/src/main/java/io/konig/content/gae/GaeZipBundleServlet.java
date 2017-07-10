package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
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


import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.konig.content.AssetBundleKey;
import io.konig.content.ContentAccessException;

public class GaeZipBundleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ZipInputStream zipInput = new ZipInputStream(req.getInputStream());

		MemoryZipArchive zipArchive = new MemoryZipArchive(zipInput);
		
		AssetBundleKey bundleKey = ContentSystemUtil.bundleKey(req);

		GaeContentSystem contentSystem = new GaeContentSystem();
		try {
			contentSystem.saveBundle(bundleKey, zipArchive);
			resp.setStatus(HttpServletResponse.SC_OK);
			
		} catch (ContentAccessException e) {
			throw new ServletException(e);
		}
		
	}

}
