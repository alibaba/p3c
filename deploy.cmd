cd ./p3c-pmd
call mvn clean deploy -Dmaven.javadoc.skip=false -e -X -Psonatype-oss-release
cd ../
cd ./idea-plugin

cd ./p3c-common
call ../gradlew install sign uploadArchives -DossrhUsername="%ossrhUsername%" -DossrhPassword="%ossrhPassword%"
rem call ../gradlew publishToMavenLocal
cd ../

cd ./p3c-idea
call ../gradlew publishToMavenLocal
cd ../

call ./gradlew buildPlugin

cd ../
pause
