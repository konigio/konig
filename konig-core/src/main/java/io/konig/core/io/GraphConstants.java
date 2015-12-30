package io.konig.core.io;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


public class GraphConstants {

	static final short VERSION = 1;
	
	static final byte EOF = 0;
	static final byte TERM = 1;
	static final byte QNAME = 2;
	static final byte IRI = 3; 
	static final byte BNODE = 4;
	static final byte RESOURCE = 5;
	static final byte LITERAL_TERM = 6;
	static final byte LITERAL_QNAME = 7;
	static final byte LITERAL_IRI = 8;
	static final byte LANG = 9;
	static final byte PLAIN = 10;
	static final byte GRAPH = 11;
	
	static final String[] LABEL = {
		"",
		"TERM",
		"QNAME",
		"IRI",
		"BNODE",
		"RESOURCE",
		"LITERAL_TERM",
		"LITERAL_QNAME",
		"LITERAL_IRI",
		"LANG",
		"PLAIN",
		"GRAPH"
	};
}
