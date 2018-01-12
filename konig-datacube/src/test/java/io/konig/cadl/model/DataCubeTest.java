package io.konig.cadl.model;

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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataCubeTest {

    @Test
    public void testDataCube() {
        DataCube cube = new DataCube("testCube");

        Dimension a = new Dimension("a");

        DimensionLevel a1 = new DimensionLevel("a1");
        a1.addAttribute(new Attribute("name", Datatype.string, true));
        a.addLevel(a1);

        DimensionLevel a2 = new DimensionLevel("a1");
        a2.addAttribute(new Attribute("name", Datatype.string, true));
        a.addLevel(a2);

        a2.addRollUp(a1);

        Dimension b = new Dimension("b");

        cube.setDimensions(Arrays.asList(a, b));

        Measure m = new Measure("m", Datatype.real);

        cube.addMeasure(m);

        assertNotNull(cube);
        assertEquals("testCube", cube.getName());
        assertEquals(2, cube.getDimensions().size());
    }

}
