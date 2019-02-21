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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.konig.cadl.Dimension;
import io.konig.cadl.Level;

public class LevelRollUpAction implements Action {
	
	private WorkbookProcessor processor;
	private Map<Dimension,RollUpSetter> map = new HashMap<>();

	public LevelRollUpAction(WorkbookProcessor processor) {
		this.processor = processor;
	}

	public void register(WorkbookLocation location, Dimension dim, Level rollUpTo, String rollUpFromName) {
		RollUpSetter setter = map.get(dim);
		if (setter == null) {
			setter = new RollUpSetter(dim);
			map.put(dim, setter);
		}
		setter.add(new RollUpFrom(location, rollUpTo, rollUpFromName));
	}

	@Override
	public void execute() throws SpreadsheetException {
		
		for (RollUpSetter setter : map.values()) {
			setter.execute(processor);
		}
		
	}
	
	private static class RollUpSetter {
		private Dimension dimension;
		private List<RollUpFrom> rollUpList = new ArrayList<>();
		
		public RollUpSetter(Dimension dimension) {
			this.dimension = dimension;
		}
		
		void add(RollUpFrom r) {
			rollUpList.add(r);
		}

		void execute(WorkbookProcessor processor) throws SpreadsheetException {
			Map<String, Level> map = buildMap();
			for (RollUpFrom r : rollUpList) {
				String fromName = r.getFromName();
				Level fromLevel = map.get(fromName);
				if (fromLevel == null) {
					processor.fail(r.getLocation(), 
						"Level {0} not found in Dimension <{1}>", 
						fromName, dimension.getId());
				} else {
					fromLevel.addRollUpTo(r.getLevel());
				}
				
			}
			
		}

		private Map<String, Level> buildMap() {
			Map<String,Level> map = new HashMap<>();

			for (Level level : dimension.getLevel()) {
				map.put(level.getId().getLocalName(), level);
			}
			return map;
		}
	}
	
	private static class RollUpFrom {
		WorkbookLocation location;
		Level level;
		String fromName;
		public RollUpFrom(WorkbookLocation location, Level level, String fromName) {
			this.location = location;
			this.level = level;
			this.fromName = fromName;
		}
		public WorkbookLocation getLocation() {
			return location;
		}
		public Level getLevel() {
			return level;
		}
		public String getFromName() {
			return fromName;
		}
		
	}

}
