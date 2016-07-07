#Building the app
Note that this app requires a google-services.json file in order to be built. The file should be placed in the app folder.
If the file has not yet been generated, go to https://developers.google.com/cloud-messaging/android/start and follow the second step.

#Running tests
In order to run the tests, run the following command:

./gradlew generateCoverage

This will both run the unit tests, and generate a coverage report.

The unit test report is located at:
{baseFolder}/app/build/reports/tests/debug/index.html

The coverage report is located at:
{baseFolder}/app/build/reports/jacoco/generateCoverage/html/index.html

Note that the coverage report will not be generated if there are any test failures.

The target is to have a large coverage for non android classes (e.g. non ui and database classes)

# Instrumentation tests

To run the instrumentation tests, first connect and unlock the test devices, or start emulators.
Then, run: ./gradlew spoon
The report for this should appear in : 
{baseFolder}/app/build/spoon/debug/index.html

