package io.konig.datacatalog;

import java.util.ArrayList;
import java.util.List;

/*
 * #%L
 * Konig Data Catalog
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


public class DataSourceSummary {
	
	private String datasourceName;
	private String datasourceType;
	private List<Link> artifactList=new ArrayList<>();
	
	public DataSourceSummary(String datasourceName, String datasourceType) {
		this.datasourceName = datasourceName;
		this.datasourceType = datasourceType;
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public String getDatasourceType() {
		return datasourceType;
	}

	public List<Link> getArtifactList() {
		return artifactList;
	}

	public void addArtifact(Link link) {
		artifactList.add(link);
	}
	

}
