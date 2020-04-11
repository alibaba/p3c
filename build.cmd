cd ./p3c-pmd
call mvn clean install -Dmaven.javadoc.skip=false -e
cd ../
cd ./idea-plugin

cd ./p3c-common
call ../gradlew publishToMavenLocal --scan -s
cd ../

cd ./p3c-idea
call ../gradlew publishToMavenLocal --scan -s
cd ../

call ./gradlew buildPlugin --scan -s

cd ../
pause
