cd ./p3c-pmd
call mvn clean install -Dmaven.javadoc.skip=false
cd ../
cd ./idea-plugin

cd ./p3c-common
call gradle publishToMavenLocal
cd ../

cd ./p3c-idea
call gradle publishToMavenLocal
cd ../

call gradle buildPlugin

cd ../
pause
