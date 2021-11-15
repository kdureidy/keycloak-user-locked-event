### Keycloak 9.0.3 Locked User Event

This is a plugin for Keycloak 9.0.3 to handle user locked event

#### Prerequisites:
1. Java 1.8
2. Keycloak 9.0.3
3. GIT access
4. Maven
5. IntelliJ IDE

Steps:

1. Build the project and generate a jar file
`mvn clean install`
2. copy generated jar file from `./target/file-name.jar` to `{KEYCLOAK-HOME}/standalone/deployments`
3. Enable Keycloak events:

    a. Login to Keycloak<br>
    b. Go to events, then Event Listeners tab.<br>
    c. enter your new event name.
