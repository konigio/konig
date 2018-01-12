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

import io.konig.cadl.model.DataCube;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("Duplicates")
public class DataCubeParserTest {

    @Test
    public void testParserSimpleCube() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/cubes/simple.cadl");
        try(Reader reader = new InputStreamReader(is)) {
            DataCubeParser parser = new DataCubeParser();
            DataCube cube = parser.parse(reader);
            assertNotNull(cube);
            assertEquals("foo", cube.getName());
        }
    }

    @Test
    public void testParserAirQualityCube() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/cubes/airQuality.cadl");
        try(Reader reader = new InputStreamReader(is)) {
            DataCubeParser parser = new DataCubeParser();
            DataCube cube = parser.parse(reader);
            assertNotNull(cube);
            assertEquals("AirQuality", cube.getName());
        }
    }
}
