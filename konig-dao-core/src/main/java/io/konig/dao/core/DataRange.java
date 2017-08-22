package io.konig.dao.core;

/*
 * #%L
 * Konig DAO Core
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


/**
 * @author Greg McFall
 *
 */
public class DataRange {
	
	private FieldPath path;
	private BoundaryPoint startPoint;
	private BoundaryPoint endPoint;
	
	public DataRange(FieldPath path, BoundaryPoint startPoint, BoundaryPoint endPoint) {
		this.path = path;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		
		startPoint.setRange(this);
		endPoint.setRange(this);
	}
	public FieldPath getPath() {
		return path;
	}
	public BoundaryPoint getStartPoint() {
		return startPoint;
	}
	public BoundaryPoint getEndPoint() {
		return endPoint;
	}
	
	

}
