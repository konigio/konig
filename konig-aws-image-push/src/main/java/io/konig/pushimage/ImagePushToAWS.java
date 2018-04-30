package io.konig.pushimage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImagePushToAWS {

	public static void main(String[] args) throws Exception {
		
		String awsAccountID = System.getProperty("aws-account-id");	
		String awsRegion = System.getProperty("aws-region");	
		if(awsAccountID == null || awsRegion == null) {
			throw new Exception("AWS Account ID or Region not found.  Please define the "
					+ "'aws-account-id' and 'aws-region' in system property");
		}
		
		Process process = Runtime.getRuntime().exec("aws ecr get-login");
		BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while((line=buffer.readLine())!=null)
		{
			System.out.println("Docker login command from AWS"+line);
			process = Runtime.getRuntime().exec(line);
			buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				while((line=buffer.readLine())!=null)
				{
					System.out.println(line);
				}
				
				process = Runtime.getRuntime().exec("docker tag fabric8/konig-docker-aws-etl-base "+awsAccountID+".dkr.ecr."+awsRegion+".amazonaws.com/konig-docker-aws-etl-base:latest");
				process = Runtime.getRuntime().exec("docker push "+awsAccountID+".dkr.ecr."+awsRegion+".amazonaws.com/konig-docker-aws-etl-base:latest");
				 buffer=new BufferedReader(new InputStreamReader(process.getInputStream()));
				 while((line=buffer.readLine())!=null)
					{
						System.out.println(line);
					}
		}
		 
		
	}

}
