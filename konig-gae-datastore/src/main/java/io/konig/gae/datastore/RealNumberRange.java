package io.konig.gae.datastore;

/*
 * #%L
 * Konig GAE Datastore
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


public class RealNumberRange {

	private Double minInclusive;
	private Double minExclusive;
	private Double maxInclusive;
	private Double maxExclusive;
	
	public Double getMinInclusive() {
		return minInclusive;
	}
	public void setMinInclusive(Double minInclusive) {
		this.minInclusive = minInclusive;
	}
	public Double getMinExclusive() {
		return minExclusive;
	}
	public void setMinExclusive(Double minExclusive) {
		this.minExclusive = minExclusive;
	}
	public Double getMaxInclusive() {
		return maxInclusive;
	}
	public void setMaxInclusive(Double maxInclusive) {
		this.maxInclusive = maxInclusive;
	}
	public Double getMaxExclusive() {
		return maxExclusive;
	}
	public void setMaxExclusive(Double maxExclusive) {
		this.maxExclusive = maxExclusive;
	}
	
	

}
