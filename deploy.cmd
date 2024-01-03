set JAVA_HOME=C:\jdk-8\

cd ./p3c-pmd
call ./mvnw clean deploy -Dmaven.javadoc.skip=false -e -X -Psonatype-oss-release
cd ../
cd ./idea-plugin

set JAVA_HOME=C:\jdk-17\

cd ./p3c-common
rem call ../gradlew clean install sign uploadArchives -DossrhUsername="%ossrhUsername%" -DossrhPassword="%ossrhPassword%"
call ../gradlew publishToMavenLocal
cd ../

cd ./p3c-idea
call ../gradlew clean publishToMavenLocal buildPlugin
cd ../

cd ../
