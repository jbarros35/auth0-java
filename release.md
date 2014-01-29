This brief guide will explain the necessary steps required to deploy to [Maven Central](http://search.maven.org/).

![I'd prefer to starve](http://www.m1key.me/blog_images/maven_for_food.jpg)

## Prerequisites
1. Maven installed in you machine. If that's not the case you can do:
   ```
    brew install maven
  ```
2. GPG installed in your machine:
  ```
    brew install gpg
  ```
3. Make sure you have Auth0's Java Private Key. Ask in the chat for it.
4. Install **key** into your machine GPG by doing: 
  ```
    gpg --import key
  ```
  You should be seen an output similar to this:
  ```
    gpg: Total number processed: 1
    gpg:       secret keys read: 1
    gpg:  secret keys unchanged: 1
  ```
5. Clone [auth0-java](https://github.com/auth0/auth0-java) repository.
6. Make sure you have an account at [Sonatype](sonatype.org). You can create one [here](https://issues.sonatype.org/secure/Dashboard.jspa).

## Before Building
1. Make necessary changes on `auth0-java` project.
2. On the `auth0-java` root directory do `mvn clean install`
3. Go to the `sample` folder and modify the `pom.xml` to point to the new `auth0-java` version. The `auth0-java` version if 
4. Make sure that the example works with the new changes by running `mvn clean install org.mortbay.jetty:jetty-maven-plugin:run` and point your browser to http://localhost:8080/login.
5. If the example works, commit and push the changes on the `sample` folder.

## Building
1. Make necessary changes and commit them. It's really important that no files are left staged.
2. On the `auth0-java` root directory do `mvn release:prepare`
3. After doing that do `mvn release:perform -Darguments="-Dgpg.keyname=XXXXXXXX -Dgpg.passphrase=\"XXXXXXXX\" -Psonatype-oss-release"`
4. Then, go to [Sonatype OSS Staging section](https://oss.sonatype.org/index.html#stagingRepositories). Log in with your Sonatype user.
5. Look for a repository named `comauth0` or similar. It should be at the end of the list. Click there and make sure that the artifacts that were built on your computer match those that appear on the list. If that's not the case you will need to erase the artifacts and do the deploy again.
6. Click on that repository and click on the `Close` option. After doing that, wait a couple of seconds till it verifies the artifacts. If everything is ok click on the `Promote` options and in a couple of minutes it should be reaching Maven Central (you will be receiving emails with updates to the mailing account associated with Sonatype site). In case it fails, you will need to repeat th procedure until meeting all the repository requirements.

## Reverting changes if release:prepare worked but release:perform failed
1. Remove local tag `git tag -d auth0-servlet-VERSION.MINOR`
2. Remove remote tag `git push origin :refs/tags/auth0-servlet-1.0`
3. Go back two commits (check before using `git log`): `git reset —hard HEAD~2`
4. Force a push to Origin `git push -f origin master`
5. Remove release generated files `trash release.properties pom.xml.releaseBackup`

## Acid Test
If everything worked you should be able to run the example without a local copy:

1. Remove the local Auth0 folder from the repository that can found on: `~/.m2/repository/com/auth0/`
2. Go to the `sample` directory inside your `auth0-java` root directory.
3. Execute `mvn clean install`. 
4. If it worked and downloaded the dependencies you are set. If not, you may need to wait Maven Central to refresh (it can take up to 2 hours). Check your email for anything related with Sonatype in case something failed.

## Last but not least
Make sure to add the release, javadoc and source jar (and their .asc counterparts) to Github. It should be something similar to https://github.com/auth0/auth0-java/releases/tag/binary-1.0.
