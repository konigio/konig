package io.konig.aws.datasource;

import java.util.ArrayList;
import java.util.List;

/*
 * #%L
 * Konig AWS Model
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


public class DbCluster {
	private String dbClusterName;
	private String instanceClass;
	private String storageEncrypted;
	private String replicationSourceIdentifier;
	private String preferredMaintenanceWindow;
	private String preferredBackupWindow;
	private String dbSubnetGroupName;
	private String engineVersion;
	private String engine;
	private String dbClusterId;
	private String databaseName;
	private String backupRetentionPeriod;
	private List<String> availabilityZones;
	public String getDbClusterId() {
		return dbClusterId;
	}
	public void setDbClusterId(String dbClusterId) {
		this.dbClusterId = dbClusterId;
	}
	public String getDbClusterName() {
		return dbClusterName;
	}
	public void setDbClusterName(String dbClusterName) {
		this.dbClusterName = dbClusterName;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	public String getEngineVersion() {
		return engineVersion;
	}
	public void setEngineVersion(String engineVersion) {
		this.engineVersion = engineVersion;
	}
	public String getInstanceClass() {
		return instanceClass;
	}
	public void setInstanceClass(String instanceClass) {
		this.instanceClass = instanceClass;
	}
	public String getBackupRetentionPeriod() {
		return backupRetentionPeriod;
	}
	public void setBackupRetentionPeriod(String backupRetentionPeriod) {
		this.backupRetentionPeriod = backupRetentionPeriod;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getDbSubnetGroupName() {
		return dbSubnetGroupName;
	}
	public void setDbSubnetGroupName(String dbSubnetGroupName) {
		this.dbSubnetGroupName = dbSubnetGroupName;
	}
	public String getPreferredBackupWindow() {
		return preferredBackupWindow;
	}
	public void setPreferredBackupWindow(String preferredBackupWindow) {
		this.preferredBackupWindow = preferredBackupWindow;
	}
	public String getPreferredMaintenanceWindow() {
		return preferredMaintenanceWindow;
	}
	public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
		this.preferredMaintenanceWindow = preferredMaintenanceWindow;
	}
	public String getReplicationSourceIdentifier() {
		return replicationSourceIdentifier;
	}
	public void setReplicationSourceIdentifier(String replicationSourceIdentifier) {
		this.replicationSourceIdentifier = replicationSourceIdentifier;
	}
	public String getStorageEncrypted() {
		return storageEncrypted;
	}
	public void setStorageEncrypted(String storageEncrypted) {
		this.storageEncrypted = storageEncrypted;
	}
	public List<String> getAvailabilityZones() {
		return availabilityZones;
	}
	public void setAvailabilityZones(List<String> availabilityZones) {
		this.availabilityZones = availabilityZones;
	}
	public void addAvailabilityZone(String availabilityZone){
		if(availabilityZones==null)
			availabilityZones=new ArrayList<String>();
		availabilityZones.add(availabilityZone);
	}
	
	
}
