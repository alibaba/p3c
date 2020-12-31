cd ./p3c-pmd
call mvn clean install -Dmaven.javadoc.skip=false -e -X -Psonatype-oss-release
call mvn sonar:sonar ^
-Dsonar.scm.exclusions.disabled=true ^
-Dsonar.projectKey=p3c-cmd ^
-Dsonar.organization=xenoamess-github ^
-Dsonar.host.url=https://sonarcloud.io ^
-Dsonar.log.level=DEBUG ^
-Dsonar.language=java ^
-Dsonar.java.source=1.8 ^
-Dsonar.sourceEncoding=UTF-8 ^
-Dsonar.java.binaries=target/classes/ ^
-Dsonar.java.test.binaries=target/test-classes/ ^
-Dsonar.java.coveragePlugin=jacoco ^
-Dsonar.jacoco.reportPaths=target/jacoco.exec ^
-Dsonar.junit.reportPaths=target/surefire-reports/ ^
-Dsonar.login=353821de77c27c633a2b3339a9cfc5ad08d66086

cd ../
cd ./idea-plugin

cd ./p3c-common
call ../gradlew publishToMavenLocal sonarqube
cd ../

cd ./p3c-idea
rem call ../gradlew publishToMavenLocal
cd ../

rem call ./gradlew buildPlugin sonarqube

cd ../
pause
