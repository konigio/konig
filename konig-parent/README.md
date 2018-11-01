# Purpose

This project is the Maven "parent" for all Konig projects.  It defines global build
properties that are shared across the projects such as the version of each dependency.

The project also provides tools for deploying projects to Maven Central.

# Release Process

## Verify that there are no uncommitted changes

```
cd konig
git status
```

## Update the version number

```
cd konig-parent
mvn versions:set -DnewVersion=1.2.1
mvn install -N
mvn versions:commit
```

Obviously, you will replace `1.2.1` with the actual value of the version number for
the current release.

## Verify the build

```
mvn clean install
```

## Push changes to the git repo

In Eclipse, commit changes with the message: "Release candidate 1.2.1"

## Deploy to Maven Central

```
	mvn clean deploy -DperformRelease=true
```
## Tag the release


In Repositories view, right-click `Tags` and choose `Create Tag...`

> Enter the tag name: v1.2.1
> Enter a message: "Version 1.2.1"

Right click the newly created tag and choose `Push tag...`

