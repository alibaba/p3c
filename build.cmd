cd ./p3c-pmd
call mvn clean install -Dmaven.javadoc.skip=false -e
cd ../
cd ./idea-plugin

cd ./p3c-common
call ../gradlew publishToMavenLocal
cd ../

cd ./p3c-idea
call ../gradlew publishToMavenLocal
cd ../

call ./gradlew buildPlugin

cd ../
pause
