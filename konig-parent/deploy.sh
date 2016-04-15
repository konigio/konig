#!/bin/bash   

PROJECT=(konig-core konig-shacl konig-schemagen konig-schemagen-maven-plugin)

cd ..

for p in $PROJECT; do
	cd $p
	echo Deploying $p
	mvn clean deploy -DperformRelease=true
  cd ..
done

cd konig-parent