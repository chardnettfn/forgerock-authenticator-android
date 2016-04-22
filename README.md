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
