
Releases numbers are based on  https://semver.org/

The first release starts with 0.1.0 

# Create release

Current Version: 0.1.0-SNAPSHOT
## Set the next release version
```
mvn versions:set -DnewVersion=0.1.0
git add pom.xml
git commit -m "Prepare release 0.1.0"
git tag v0.1.0
git push origin v0.1.0
```

# Set the next development version
```
mvn versions:set -DnewVersion=0.1.1-SNAPSHOT
git add pom.xml
git commit -m "Prepare for next development iteration"
```
