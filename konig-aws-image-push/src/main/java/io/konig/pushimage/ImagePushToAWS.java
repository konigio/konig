package io.konig.pushimage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImagePushToAWS {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
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
				
				process = Runtime.getRuntime().exec("docker tag fabric8/konig-docker-aws-etl-base 220459826988.dkr.ecr.us-east-1.amazonaws.com/ecstest:konig-docker-aws-etl-base");
				process = Runtime.getRuntime().exec("docker push 220459826988.dkr.ecr.us-east-1.amazonaws.com/ecstest:konig-docker-aws-etl-base");
				 buffer=new BufferedReader(new InputStreamReader(process.getInputStream()));
				 while((line=buffer.readLine())!=null)
					{
						System.out.println(line);
					}
		}
		 
		
	}

}
