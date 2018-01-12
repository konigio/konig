package io.konig.cadl.parser;

/*
 * #%L
 * konig-datacube
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

import io.konig.cadl.model.*;

public class DataCubeListener extends CADLBaseListener {

    private DataCube dataCube;
    private Dimension dimension;
    private Measure measure;
    private DimensionLevel dimensionLevel;

    @Override
    public void enterCube(CADLParser.CubeContext ctx) {
        super.enterCube(ctx);
        dataCube = new DataCube(ctx.NAME().getText());
    }

    @Override
    public void enterDimension(CADLParser.DimensionContext ctx) {
        super.enterDimension(ctx);
        dimension = new Dimension(ctx.NAME().getText());
    }

    @Override
    public void exitDimension(CADLParser.DimensionContext ctx) {
        super.exitDimension(ctx);
        dataCube.addDimension(dimension);
    }

    @Override
    public void enterLevel(CADLParser.LevelContext ctx) {
        super.enterLevel(ctx);
        String name = ctx.NAME().getText();
        dimensionLevel = new DimensionLevel(name);
    }

    @Override
    public void exitLevel(CADLParser.LevelContext ctx) {
        super.exitLevel(ctx);
        dimension.addLevel(dimensionLevel);
    }

    @Override
    public void enterAttribute(CADLParser.AttributeContext ctx) {
        super.enterAttribute(ctx);
        String name = ctx.NAME().getText();
        Datatype datatype = Datatype.valueOf(ctx.datatype().getText());
        boolean isUnique = ctx.UNIQUE() != null;
        dimensionLevel.addAttribute(new Attribute(name, datatype, isUnique));
    }

    @Override
    public void enterRollup(CADLParser.RollupContext ctx) {
        super.enterRollup(ctx);
        DimensionLevel lowerLevel = dimension.getLevel(ctx.NAME(0).getText());
        DimensionLevel upperLevel = dimension.getLevel(ctx.NAME(1).getText());
        if(upperLevel != null && lowerLevel != null) {
            lowerLevel.addRollUp(upperLevel);
        }
    }

    @Override
    public void enterMeasure(CADLParser.MeasureContext ctx) {
        super.enterMeasure(ctx);
        String name = ctx.NAME().getText();
        Datatype datatype = Datatype.valueOf(ctx.datatype().getText());
        measure = new Measure(name, datatype);
    }

    @Override
    public void exitMeasure(CADLParser.MeasureContext ctx) {
        super.exitMeasure(ctx);
        dataCube.addMeasure(measure);
    }

    public DataCube getDataCube() {
        return dataCube;
    }
}
