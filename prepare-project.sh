mkdir -p node_modules/\@vaadin/flow-frontend/
cp src/main/resources/META-INF/frontend/example-style.js node_modules/\@vaadin/flow-frontend/
mvn vaadin:prepare-frontend -Pproduction
mvn install -DskipTests -Pproduction