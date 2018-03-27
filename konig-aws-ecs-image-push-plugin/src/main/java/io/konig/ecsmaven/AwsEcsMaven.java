package io.konig.ecsmaven;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "push")
public class AwsEcsMaven extends AbstractMojo{

	@Parameter
	private String awsEcsRepositoryUrn;
	
	@Parameter
	private String imageName;
	
	@Parameter
	private String repositoryName;
	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try{
	 Process process = Runtime.getRuntime().exec("aws ecr create-repository --repository-name "+repositoryName);
	 BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
	 String line;
	 while((line=buffer.readLine())!=null)
		{
		 System.out.println(line);
		}
		process = Runtime.getRuntime().exec("aws ecr get-login");
 		 buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
 
 		while((line=buffer.readLine())!=null)
 		{
 			System.out.println("Docker login command from AWS"+line);
 			process = Runtime.getRuntime().exec(line);
 			buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
 				
 				while((line=buffer.readLine())!=null)
 				{
 					System.out.println(line);
 				}
 				String command1="docker tag "+imageName+" "+awsEcsRepositoryUrn+repositoryName+":latest";
 				System.out.println(command1);
 				process = Runtime.getRuntime().exec(command1);
 				buffer=new BufferedReader(new InputStreamReader(process.getInputStream()));
				 while((line=buffer.readLine())!=null)
					{
						System.out.println(line);
					}
				 command1="docker push "+awsEcsRepositoryUrn+repositoryName+":latest";
				 System.out.println(command1);
 				process = Runtime.getRuntime().exec(command1);
 				
 				 buffer=new BufferedReader(new InputStreamReader(process.getInputStream()));
 				 while((line=buffer.readLine())!=null)
 					{
 						System.out.println(line);
 					}
 		}
		}catch(IOException e)
		{
			e.getMessage();
		}
	}

}
