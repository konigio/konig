package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
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

import java.io.File;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.showl.ShowlNodeShape;

public class BeamTransformRequest {

	private String groupId;
	private String artifactBaseId;
	private String version;
	private File projectDir;
	private List<ShowlNodeShape> nodeList;

	public String getGroupId() {
		return groupId;
	}
	
	public String parentArtifactId() {
		return artifactBaseId + "-beam";
	}
	
	public String artifactId(URI targetShapeId) {
		String localName = targetShapeId.getLocalName();
		
		if (localName.endsWith("Shape")) {
			localName = localName.substring(0, localName.length()-5);
		}
		return artifactBaseId + "-" + localName.toLowerCase() + "-beam";
	}

	public String getArtifactBaseId() {
		return artifactBaseId;
	}

	public String getVersion() {
		return version;
	}
	
	public File projectDir(URI targetShapeId) {
		return new File(projectDir, artifactId(targetShapeId));
	}

	public File getProjectDir() {
		return projectDir;
	}

	public List<ShowlNodeShape> getNodeList() {
		return nodeList;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String groupId;
		private String artifactId;
		private String version;
		private File projectDir;
		private List<ShowlNodeShape> nodeList;

		public Builder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public Builder artifactBaseId(String artifactId) {
			this.artifactId = artifactId;
			return this;
		}

		public Builder version(String version) {
			this.version = version;
			return this;
		}

		public Builder projectDir(File projectDir) {
			this.projectDir = projectDir;
			return this;
		}

		public Builder nodeList(List<ShowlNodeShape> nodeList) {
			this.nodeList = nodeList;
			return this;
		}

		public BeamTransformRequest build() {
			return new BeamTransformRequest(this);
		}
	}

	private BeamTransformRequest(Builder builder) {
		this.groupId = builder.groupId;
		this.artifactBaseId = builder.artifactId;
		this.version = builder.version;
		this.projectDir = builder.projectDir;
		this.nodeList = builder.nodeList;
	}
}
