package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


public interface SheetProcessor extends Comparable<SheetProcessor> {
	
	/**
	 * Get a list of other SheetProcessors that must be executed before this one.
	 */
	SheetProcessor[] dependsOn();
	
	
	SheetColumn[] getColumns();
	
	SheetColumn findColumnByName(String name);
	
	void visit(SheetRow row) throws SpreadsheetException;
	
	/**
	 * Test whether this SheetProcessor depends on another processor, transitively.
	 * @param other The other SheetProcessor
	 * @return True if this sheet depends on the other sheet, or any sheet that the
	 * other sheet depends on, recursively.
	 */
	public boolean transitiveDependsOn(SheetProcessor other);
}
