# Eternal UI framework

### Objective
Handle the creation of UI for Backend and utility software.
By defining View, Component, Controller, Domain and Backend objects,
is easy to define a page of the application you should develop.

The sync between Domain object and UI is handled by the framework, 
that use the field name and the id of the UI component to link them together.

### Build and start of example
- mvn vaadin:prepare-frontend -Pproduction
- mkdir -p node_modules/\@vaadin/flow-frontend/
- cp src/main/resources/META-INF/frontend/dashboard-style.js node_modules/\@vaadin/flow-frontend/
- mvn install -DskipTests -Pproduction
- mvn spring-boot:run

