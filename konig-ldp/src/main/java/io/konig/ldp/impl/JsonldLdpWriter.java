package io.konig.ldp.impl;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.impl.BasicContext;
import io.konig.core.io.JsonldGraphWriter;
import io.konig.core.io.KonigWriteException;
import io.konig.core.io.impl.JsonldGraphWriterImpl;
import io.konig.ldp.LdpException;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.LdpWriter;
import io.konig.ldp.MediaType;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceFile;

public class JsonldLdpWriter implements LdpWriter {

	@Override
	public void write(LdpResponse response) throws LdpException, IOException {
		
		
		ResourceFile resource = response.getResource();
		String contentType = resource.getContentType();
		MediaType target = response.getTargetMediaType();
		
		if ("application/ld+json".equals(contentType) && 
			target!=null &&
			contentType.equals(target.getFullName()) &&
			resource.getEntityBody() != null) {
			
			response.getOutputStream().write(resource.getEntityBody());
			
		} else if (resource.isRdfSource()) {
			
			// TODO: Evaluate vendor-specific media type and select
			// an appropriate JSON-LD context.
			
			// For now, use an empty JSON-LD context
			Context context = new BasicContext(null);
			
			JsonGenerator json = response.getJsonGenerator();
			RdfSource source = resource.asRdfSource();

			JsonldGraphWriter graphWriter = new JsonldGraphWriterImpl();
			try {
				graphWriter.write(source.createGraph(), context, json);
			} catch (KonigWriteException e) {
				throw new LdpException(e);
			}
			
		} else {
			throw new LdpException("Entity body must be an instanceof RDF Source");
		}

	}

	

}
