mvn clean -Pproduction
rm -rf node_modules package* webpack*
mvn vaadin:prepare-frontend -Pproduction
mvn install -DskipTests -Pproduction
mvn package -DskipTests -Pproduction
