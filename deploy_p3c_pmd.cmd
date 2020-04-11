cd ./p3c-pmd
call mvnw.cmd clean deploy -Dmaven.javadoc.skip=false -e -X -Psonatype-oss-release
cd ../
cd ./idea-plugin

cd ./p3c-common
call ../gradlew uploadArchives -DossrhUsername="%ossrhUsername%" -DossrhPassword="%ossrhPassword%"
cd ../

cd ./p3c-idea
call ../gradlew publishToMavenLocal
cd ../

call ./gradlew buildPlugin

cd ../
pause
