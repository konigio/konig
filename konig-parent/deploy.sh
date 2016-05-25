#!/bin/bash   

declare -a PROJECT=(konig-parent konig-core konig-shacl konig-schemagen konig-schemagen-maven-plugin konig-ldp konig-ldp-maven-plugin konig-war)

cd ..

for p in "${PROJECT[@]}"; do
	cd $p
	echo Deploying $p
	mvn clean deploy -DperformRelease=true
  cd ..
done

cd konig-parent