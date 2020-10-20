mvn clean -Pproduction
rm -rf node_modules package* webpack*
mvn vaadin:prepare-frontend -Pproduction
mvn package -DskipTests -Pproduction
mvn -DperformRelease=true deploy -Pproduction
