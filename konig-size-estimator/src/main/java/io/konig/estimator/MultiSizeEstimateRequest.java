package io.konig.estimator;

/*
 * #%L
 * Konig Size Estimator
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


import java.io.File;

import io.konig.maven.Parameter;

/**
 * An object that encapsulates the information needed to drive the size estimation for multiple shapes.
 * @author Greg McFall
 *
 */
public class MultiSizeEstimateRequest {

	@Parameter(property="konig.estimator.manifestFile", required=true)
	private File manifestFile;

	@Parameter(property="konig.estimator.reportFile", required=true)
	private File reportFile;
	
	@Parameter(property="konig.estimator.shapesLocation", required=true)
	private File shapesLocation;
	
	
	/**
	 * Get the location where the shapes are placed.
	 */
	public File getShapesLocation() {
		return shapesLocation;
	}

	/**
	 * Set the location where the shapes are placed.
	 */
	public void setShapesLocation(File shapesLocation) {
		this.shapesLocation = shapesLocation;
	}

	/**
	 * Get the location of the manifest file that specifies the location of the data directory for
	 * each shape to be estimated.
	 */
	public File getManifestFile() {
		return manifestFile;
	}

	/**
	 * Set the location of the manifest file that specifies the location of the data directory for
	 * each shape to be estimated.
	 */

	public void setManifestFile(File manifestFile) {
		this.manifestFile = manifestFile;
	}
	
	/**
	 * Get the location of the output file that contains a report giving the results of the size estimate for each 
	 * shape referenced in the manifest file.
	 * @return
	 */
	public File getReportFile() {
		return reportFile;
	}
	

	/**
	 * Set the location of the file that contains a report giving the results of the size estimate for each 
	 * shape referenced in the manifest file.
	 */
	public void setReportFile(File reportFile) {
		this.reportFile = reportFile;
	}
	
	

}
