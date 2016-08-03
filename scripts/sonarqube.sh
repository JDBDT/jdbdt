mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar \
    -Dsonar.host.url=https://sonarqube.com \
    -Dsonar.login=$SONARQUBE_TOKEN
