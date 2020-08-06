# Eternal UI framework

### Objective
Handle the creation of UI for Backend and utility software.
By defining View, Component, Controller, Domain and Backend objects,
is easy to define a page of the application you should develop.
More power has been introduced based on conventions, so now you can attach logic
to you UI elements just by using the name of the UI Element + Clicked/Changed/DataProvider

The sync between Domain object and UI is handled by the framework, 
that use the field name and the id of the UI component to link them together.
With this framework you can really write the UI and handle just the variations on a model, the framework
will work to bring the domain on the UI seamlessy.

### Build and start of example
- mkdir -p node_modules/\@vaadin/flow-frontend/
- cp src/main/resources/META-INF/resources/frontend/example-style.js node_modules/\@vaadin/flow-frontend/
- mvn vaadin:prepare-frontend -Pproduction
- mvn install -DskipTests -Pproduction
- mvn spring-boot:run
or just run: start-example.sh

