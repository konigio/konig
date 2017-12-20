# Troubleshooting Tips

The deployment script is implemented using Groovy.

Groovy maintains its own cache of JAR files, under the folder at `~/.groovy/grapes`
It pulls these JAR files from remote Maven Repositories, and the local Maven Repository at `~/.m2/repository`

If you run into errors such as `Error grabbing Grapes --` or `Class not Found` try deleting these 
repositories (or selectively the offending artifacts), and rebuilding.