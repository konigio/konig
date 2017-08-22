package io.konig.sql.runtime;

import java.io.IOException;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import java.io.Writer;

import io.konig.dao.core.Chart;
import io.konig.dao.core.ChartKey;
import io.konig.dao.core.ChartSeriesFactory;
import io.konig.dao.core.ChartWriter;
import io.konig.dao.core.ChartWriterFactory;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeReadService;
import io.konig.dao.core.SimpleChartFactory;

abstract public class SqlShapeReadService extends SqlGenerator implements ShapeReadService {
	
	private EntityStructureService structureService;

	public SqlShapeReadService(EntityStructureService structureService) {
		this.structureService = structureService;
	}

	@Override
	public void execute(ShapeQuery query, Writer output, Format format) throws DaoException {

		EntityStructure struct = structureService.structureOfShape(query.getShapeId());
		String view = query.getView();
		if (view != null) {
			ChartKey chartKey = ChartKey.fromMediaType(view);
			if (chartKey != null) {
				SimpleChartFactory factory = new SimpleChartFactory(getChartSeriesFactory());
				Chart chart = factory.createChart(query, struct);
				ChartWriterFactory writerFactory = new ChartWriterFactory();
				
				ChartWriter writer = writerFactory.createChartWriter(chart, output);
				try {
					writer.writeChart(chart);
					return;
				} catch (IOException e) {
					throw new DaoException(e);
				}
			}
		}

		String sql = toSql(struct, query);
		executeSql(struct, sql, output, format);
	}
	
	abstract ChartSeriesFactory getChartSeriesFactory();

	abstract protected void executeSql(EntityStructure struct, String sql, Writer output, Format format) throws DaoException;

	
}
