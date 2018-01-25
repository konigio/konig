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
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class DataCubeShapeGenerator {

    public DataCubeShapeGenerator() { }

    static String KONIG = "http://www.konig.io/ns/core/";

    private String getClassName(String name) {
        char[] delimiters = { ' ' };
        return StringUtils.remove(WordUtils.capitalize(name, delimiters), " ");
    }

    private String getShapeName(String name) {
        return getClassName(name) + "Shape";
    }

    private URI uri(String prefix, String value) {
        return new URIImpl(prefix+value);
    }

    private URI getDatatype(Datatype datatype) {
        switch (datatype) {
            case real: return new URIImpl("http://www.w3.org/2001/XMLSchema#decimal");
            case date: return new URIImpl("http://www.w3.org/2001/XMLSchema#date");
            case string: return new URIImpl("http://www.w3.org/2001/XMLSchema#string");
            default: return null;
        }
    }

    private PropertyConstraint getMeasurePropertyConstraint(Measure measure) {
        String ns_ex = "http://example.com/schema/";
        PropertyConstraint measureProperty = new PropertyConstraint();
        measureProperty.setPath(uri(ns_ex, measure.getName()));
        measureProperty.setMinCount(0);
        measureProperty.setMaxCount(1);
        measureProperty.setDatatype(getDatatype(measure.getDatatype()));
        measureProperty.setStereotype(uri(KONIG, "measure"));
        return measureProperty;
    }

    private PropertyConstraint getDimensionLevelPropertyConstraint(DimensionLevel dimLevel) {
        String ns_ex = "http://example.com/schema/";
        PropertyConstraint dimLevelProperty = new PropertyConstraint();
        dimLevelProperty.setPath(uri(ns_ex, dimLevel.getName()));
        dimLevelProperty.setMinCount(0);
        dimLevelProperty.setMaxCount(1);
        Shape dimLevelShape = getDimensionShape(dimLevel);
        dimLevelProperty.setShape(dimLevelShape);
        return dimLevelProperty ;
    }

    private Shape getDimensionShape(DimensionLevel dimensionLevel) {
        String ns_v1 = "http://example.com/shape/";
        String ns_ex = "http://example.com/schema/";

        String dimLevelName = dimensionLevel.getName();

        URI shapeId = uri(ns_v1, getShapeName(dimLevelName));
        Shape dimLevelShape = new Shape(shapeId);

        dimLevelShape.setTargetClass(uri(ns_ex, getClassName(dimLevelName)));

        for(Attribute attr : dimensionLevel.getAttributes()) {
            PropertyConstraint levelAttributeProperty = new PropertyConstraint();
            levelAttributeProperty.setPath(uri(ns_ex, attr.getName()));
            levelAttributeProperty.setMinCount(0);
            levelAttributeProperty.setMaxCount(1);
            levelAttributeProperty.setDatatype(getDatatype(attr.getDatatype()));
            dimLevelShape.add(levelAttributeProperty);
        }

        return dimLevelShape;
    }

    public List<Shape> generateShapes(DataCube cube) {

        String ns_v1 = "http://example.com/shape/";
        String ns_ex = "http://example.com/schema/";

        List<Shape> shapes = new ArrayList<>();

        String cubeName = cube.getName();
        String shapeName = getShapeName(cubeName);

        URI shapeId = uri(ns_v1, shapeName);
        Shape cubeShape = new Shape(shapeId);
        cubeShape.setTargetClass(uri(ns_ex, getClassName(cubeName)));

        for(Measure m : cube.getMeasures()) {
            PropertyConstraint measureProperty = getMeasurePropertyConstraint(m);
            cubeShape.add(measureProperty);
        }

        for(Dimension dim: cube.getDimensions()) {
            for(DimensionLevel dimLevel : dim.getLevels()) {
                PropertyConstraint dimLevelProperty = getDimensionLevelPropertyConstraint(dimLevel);
                cubeShape.add(dimLevelProperty);
            }
        }

        shapes.add(cubeShape);
        return shapes;
    }
}
