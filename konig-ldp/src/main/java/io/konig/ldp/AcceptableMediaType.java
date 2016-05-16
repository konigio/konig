package io.konig.ldp;

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


public class AcceptableMediaType {
	private MediaType mediaType;
	private int qValue;
	
	public AcceptableMediaType(MediaType mediaType, int qValue) {
		this.mediaType = mediaType;
		this.qValue = qValue;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public int getQValue() {
		return qValue;
	}
	
	
}
