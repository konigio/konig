package io.konig.schemagen;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


public class MySqlDatatype {

	 public static final int SIGNED_TINYINT_MIN = -128;
	  public static final int SIGNED_TINYINT_MAX =  127;
	  
	  public static final int UNSIGNED_TINYINT_MIN = 0;
	  public static final int UNSIGNED_TINYINT_MAX = 255;
	  
	  public static final int SIGNED_SMALLINT_MIN = -32768;
	  public static final int SIGNED_SMALLINT_MAX =  32767;
	  
	  public static final int UNSIGNED_SMALLINT_MIN = 0;
	  public static final int UNSIGNED_SMALLINT_MAX = 65535;
	  
	  public static final int SIGNED_MEDIUMINT_MIN = -8388608;
	  public static final int SIGNED_MEDIUMINT_MAX =  8388607;
	  
	  public static final int UNSIGNED_MEDIUMINT_MIN = 0;
	  public static final int UNSIGNED_MEDIUMINT_MAX = 16777215;
	  
	  public static final int SIGNED_INT_MIN = -2147483648;
	  public static final int SIGNED_INT_MAX =  2147483647;
	  
	  public static final long UNSIGNED_INT_MIN = 0;
	  public static final long UNSIGNED_INT_MAX = 4294967295L;
	  
	  public static final long SIGNED_BIGINT_MIN = -9223372036854775808L;
	  public static final long SIGNED_BIGINT_MAX =  9223372036854775807L;
}
