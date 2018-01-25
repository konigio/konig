package io.konig.cadl;

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
import io.konig.shacl.Shape;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataCubeShapeGeneratorTest {

    private static DataCube getSimpleDataCube() {
        DataCube cube = new DataCube("testCube");
        Measure b = new Measure("b", Datatype.real);
        cube.addMeasure(b);

        Dimension a = new Dimension("a");

        DimensionLevel a1 = new DimensionLevel("a1");
        a1.addAttribute(new Attribute("name", Datatype.string, true));
        a.addLevel(a1);

        DimensionLevel a2 = new DimensionLevel("a2");
        a2.addAttribute(new Attribute("name", Datatype.string, true));
        a2.addAttribute(new Attribute("category", Datatype.string, false));
        a.addLevel(a2);

        a2.addRollUp(a1);

        cube.addDimension(a);

        return cube;
    }

    @Test
    public void testSimpleCube() {
        DataCube cube = getSimpleDataCube();
        DataCubeShapeGenerator generator = new DataCubeShapeGenerator();
        List<Shape> shapes = generator.generateShapes(cube);

        assertNotNull(shapes);
        assertEquals(1, shapes.size());
        assertEquals("http://example.com/shape/TestCubeShape", shapes.get(0).getId().toString());

        System.out.println(shapes.get(0).toString());
    }
}
