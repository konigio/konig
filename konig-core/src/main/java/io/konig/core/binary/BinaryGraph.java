package io.konig.core.binary;

/*
 * #%L
 * konig-core
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


public class BinaryGraph {
	public static final short VERSION_1 = 1;
	
	public static final int SIZE_BYTE = 1;
	public static final int SIZE_SHORT = 2;
	public static final int SIZE_INT = 4;
	
	public static final int NODE_INDIVIDUAL = 1;
	public static final int NODE_PREFIX = 2;
	public static final int NODE_PREDICATE_PLAIN = 4;
	public static final int NODE_PREDICATE_DATATYPE = 8;
	public static final int NODE_PREDICATE_LANGUAGE = 16;
	public static final int NODE_PREDICATE_MULTIPLE_DATATYPES = 32;
	public static final int NODE_PREDICATE_VARIABLE_LANGUAGE = 64;
	public static final int NODE_PREDICATE_INDIVIDUAL = 128;
	
	public static final int LITERAL = 
		NODE_PREDICATE_PLAIN |
		NODE_PREDICATE_DATATYPE |
		NODE_PREDICATE_LANGUAGE |
		NODE_PREDICATE_MULTIPLE_DATATYPES |
		NODE_PREDICATE_VARIABLE_LANGUAGE;
	
}
