# Release Workflow

Releases are versioned using Semantic Versioning [SemVer](http://semver.org).

- The first release starts with version 0.1.0.
- The main branch always contains the current _-SNAPSHOT_ version, which represents the last release version suffixed with _-SNAPSHOT_.

## Release Process

To create a release:

- Set the correct release version.
- Push the changes to GitHub.
- A GitHub Action will automatically:
  -  Create a Git tag for the release.
  -  Trigger a build to deploy the released Maven artifacts to GitHub Packages.

## Step-by-Step Guide

#### 1. Set the Next Release Version.

The release version is the current version without the _-SNAPSHOT_ suffix.
Run the following command:
```
mvn versions:set -DnewVersion=0.1.4
```

Update current release version in the `docs/getting-started.md` file.

remove snapshot repositories from pom.xml file:

#### 2. Push Changes to Trigger GitHub Actions. 
Stage and commit your changes, then push them to GitHub:
```
git add .
git commit -m "Prepare release 0.1.1"
git push origin
```
This will trigger the GitHub Actions workflow to create the release. 

#### 3. Set the Next Development Version

After the release, set the version for the next development iteration. 
The development version is the next version with the _-SNAPSHOT_ suffix.

Run the following command:
```
mvn versions:set -DnewVersion=0.1.4-SNAPSHOT
```

#### 4. Commit and Push the Development Version

Stage, commit, and push the changes:

```
git add .
git commit -m "Prepare for next development iteration"
git push origin
```

### Summary

* Use mvn versions:set to update the version.
* Push to GitHub to trigger release automation.
* Increment the version to the next _-SNAPSHOT_ after the release.
