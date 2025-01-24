
# Set the next release version
mvn versions:set -DnewVersion=0.1.0
git add pom.xml
git commit -m "Prepare release 0.1.0"

# Set the next development version
mvn versions:set -DnewVersion=0.2-SNAPSHOT
git add pom.xml
git commit -m "Prepare for next development iteration"
