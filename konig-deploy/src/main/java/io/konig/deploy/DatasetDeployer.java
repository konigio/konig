package io.konig.deploy;

public class DatasetDeployer {

	public static void main(String[] args) {
		
		DatasetService datasetService = new DatasetService();
		datasetService.processRequest(null, "D:/Tmp/Dataset/");

	}

}
