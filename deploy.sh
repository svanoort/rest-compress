cd rest-compress-lib
# Enter passphrase below
# Make sure this is in mvn settings.xml
#<server>
#      <id>sonatype</id>
#      <username>svanoort</username>
#      <password>ENTERME</password>
#</server>

# Sign and submit artifacts, but does not do it for the POM and wrong format for javadocs
mvn clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase=ENTERME -Dhttps://oss.sonatype.org/service/local/staging/deploy/maven2 -DpomFile=rest-compress-lib-0.5.pom install deploy

# Create all needed artifacts and GPG signatures to manually upload them:
mvn clean install javadoc:jar source:jar gpg:sign


# Build bundle
# Create a bundle, but does not do GPG signing
mvn clean install javadoc:jar source:jar repository:bundle-create

# Create and sign a bundle, with all the needful in it
mvn clean install javadoc:jar source:jar repository:bundle-create gpg:sign


mvn clean install javadoc:jar source:jar gpg:sign deploy