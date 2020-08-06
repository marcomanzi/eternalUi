mkdir -p node_modules/\@vaadin/flow-frontend/
cp src/main/resources/META-INF/resources/frontend/example-style.js node_modules/\@vaadin/flow-frontend/
mvn vaadin:prepare-frontend -Pproduction
mvn install -DskipTests -Pproduction
mvn spring-boot:run

