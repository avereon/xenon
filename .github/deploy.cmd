REM Use Maven to verify the build, but do not deploy it to the repository
mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml

