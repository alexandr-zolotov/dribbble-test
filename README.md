# dribbble-test
test-assignment

To build the app use Apache Maven. Run the command  
`mvn clean install`

After successful build run the app with the command  
`java -jar target/dribbble-stats-1.0-SNAPSHOT-jar-with-dependencies.jar <username>`  
Please pay atantion to the last part of the command - **username** parameter. You should put there person's dribbble login, not the actual name.

After successful execution you should see up to 10 Top "likers". Execution can fail due to api limits (60 requests/minute).

