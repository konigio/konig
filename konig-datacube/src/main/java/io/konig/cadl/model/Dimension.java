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

import java.util.ArrayList;
import java.util.List;

public class Dimension {

    private String name;
    private List<DimensionLevel> levels = new ArrayList<>();

    public Dimension(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<DimensionLevel> getLevels() {
        return levels;
    }

    public void setLevels(List<DimensionLevel> levels) {
        this.levels = levels;
    }

    public void addLevel(DimensionLevel level) {
        this.levels.add(level);
    }

    public DimensionLevel getLevel(String name) {
        for(DimensionLevel level : levels) {
            if(level.getName().equals(name)) {
                return level;
            }
        }
        return null;
    }

}
