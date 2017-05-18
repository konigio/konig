package io.konig.core.json;

/*
 * #%L
 * Konig Core
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;

public class KonigJsonIndenter implements Indenter {
	
	String indent = "  ";
	String newline = "\n";

	@Override
	public void writeIndentation(JsonGenerator jg, int level) throws IOException {
		jg.writeRaw(newline);
	      for (int i = 0; i < level; i++) {
	        jg.writeRaw(indent);
	      }
	}

	@Override
	public boolean isInline() {
		return false;
	}

}
