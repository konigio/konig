package io.konig.validation;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Collection;

public class Sum {

	public static int whereTrue(boolean...value) {
		int count = 0;
		for (boolean bool : value) {
			if (bool) {
				count++;
			}
		}
		return count;
	}
	
	public static int whereNonNull(Object...objects) {
		int count = 0;
		for (Object x : objects) {
			if (x != null) {
				count++;
			}
		}
		return count;
	}
	
	@SafeVarargs
	public static int errorCount(Collection<? extends ReportElement>... element) {
		int count = 0;
		
		for (Collection<? extends ReportElement> collection : element) {
			if (collection != null) {
				for (ReportElement e : collection) {
					if (e != null) {
						count += e.errorCount();
					}
				}
			}
		}
		return count;
	}
	
	public static int size(Collection<?>...collections) {
		int count = 0;
		for (Collection<?> list : collections) {
			if (list != null) {
				count += list.size();
			}
		}
		
		return count;
	}

}
