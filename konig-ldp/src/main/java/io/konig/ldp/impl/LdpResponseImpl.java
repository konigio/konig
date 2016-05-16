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
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.ldp.LdpResponse;
import io.konig.ldp.MediaType;
import io.konig.ldp.ResourceFile;

public class LdpResponseImpl implements LdpResponse {
	private OutputStream output;
	private ResourceFile resource;
	private MediaType targetMediaType;
	private JsonGenerator json;
	

	public LdpResponseImpl(OutputStream output) {
		this.output = output;
	}

	@Override
	public OutputStream getOutputStream() {
		return output;
	}

	@Override
	public ResourceFile getResource() {
		return resource;
	}

	@Override
	public void setResource(ResourceFile resource) {
		this.resource = resource;
	}

	@Override
	public MediaType getTargetMediaType() {
		return targetMediaType;
	}

	@Override
	public void setTargetMediaType(MediaType target) {
		this.targetMediaType = target;
	}

	@Override
	public void flush() throws IOException {
		if (json != null) {
			json.flush();
		} else {
			output.flush();
		}
		
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.output = out;
	}

	@Override
	public JsonGenerator getJsonGenerator() {
		if (json == null) {
			JsonFactory factory = new JsonFactory();
			try {
				json = factory.createGenerator(output);
				json.useDefaultPrettyPrinter();
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return json;
	}

}
