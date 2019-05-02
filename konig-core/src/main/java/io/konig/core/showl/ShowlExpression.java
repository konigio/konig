package io.konig.core.showl;

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


import java.util.Set;

/**
 * An expression that constructs the value of a target property within the 
 * context of an ETL process.
 * 
 * @author Greg McFall
 *
 */
public interface ShowlExpression {
	
	public String displayValue();

	/**
	 * Collect from this expression all properties declared by the specified source NodeShape, or any 
	 * NodeShape nested within the source NodeShape.
	 * @param sourceNodeShape
	 * @param set  The set to which the properties should be added.
	 */
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set);
	
	public void addProperties(Set<ShowlPropertyShape> set);
}
