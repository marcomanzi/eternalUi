mkdir -p node_modules/\@vaadin/flow-frontend/
mvn vaadin:prepare-frontend -Pproduction
mvn install -DskipTests -Pproduction
cp src/main/resources/META-INF/frontend/example-style.js node_modules/\@vaadin/flow-frontend/
mvn install -DskipTests -Pproduction
mvn package -DskipTests -Pproduction
