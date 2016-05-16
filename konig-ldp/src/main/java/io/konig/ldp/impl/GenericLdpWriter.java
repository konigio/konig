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

import io.konig.ldp.LdpException;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.LdpWriter;
import io.konig.ldp.MediaType;

public class GenericLdpWriter implements LdpWriter {

	@Override
	public void write(LdpResponse response) throws LdpException, IOException {
		
		MediaType mediaType = response.getTargetMediaType();
		if (mediaType == null) {
			throw new LdpException("Target media type is not defined");
		}
		
		String contentType = mediaType.getFullName();
		if (contentType.startsWith("text/turtle")) {
			TurtleLdpWriter turtle = new TurtleLdpWriter();
			turtle.write(response);
			
		} else if (contentType.startsWith("application/ld+json")) {
			JsonldLdpWriter jsonld = new JsonldLdpWriter();
			jsonld.write(response);
			
		} else {
			throw new LdpException("Target media type not supported: " + contentType);
		}

	}

}
