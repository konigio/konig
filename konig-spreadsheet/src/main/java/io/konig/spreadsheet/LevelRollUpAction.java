package io.konig.spreadsheet;

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
