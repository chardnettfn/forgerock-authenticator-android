# ForgeRock Authenticator for Android
  
## About
The ForgeRock Authenticator provides a secure method for users to access their accounts managed by ForgeRock's OpenAM. 
This works in combination with OpenAM's "ForgeRock Authenticator (OATH)" and "ForgeRock Authenticator (Push)" authentication modules.
The supported version of the binary is available on the Play Store.

## Building the App
* Download the project's code (`git clone ...`)
* This app requires a google-services.json file in order to be built. The file should be placed in the app folder.
If the file has not yet been generated, go to the [cloud messaging setup guide](https://developers.google.com/cloud-messaging/android/start) 
and follow "Step 2. Get a configuration file".
* Open the project using Android Studio. The app can be built and loaded onto an emulator or phone using the 'app' run configuration.

## Running Unit Tests
In order to run the tests, run the following command:`./gradlew generateCoverage`

This will run the unit tests and generate a coverage report.

The unit test report is located at:
`{baseFolder}/app/build/reports/tests/debug/index.html`

The coverage report is located at:
`{baseFolder}/app/build/reports/jacoco/generateCoverage/html/index.html`

Note that the coverage report will not be generated if there are any test failures.

The target is to have a large coverage for non android classes (e.g. non ui and database classes)

## Instrumentation Tests

To run the instrumentation tests, first connect and unlock the test devices, or start emulators.
Then, run: `./gradlew spoon`

The instrumentation test report is located at:
`{baseFolder}/app/build/spoon/debug/index.html`

## Notes
* When debugging, be sure to disable code coverage, as it interferes with the debugger and prevents access to variables. 
Do this by opening app.gradle, and updating the value of `testCoverageEnabled` to false.
* When adding, removing or updating 3rd party dependencies, be sure to update the
[Third Party README](THIRDPARTYREADME.txt).

* * *

The contents of this file are subject to the terms of the Common Development and
Distribution License (the License). You may not use this file except in compliance with the
License.

You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
specific language governing permission and limitations under the License.

When distributing Covered Software, include this CDDL Header Notice in each file and include
the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
Header, with the fields enclosed by brackets [] replaced by your own identifying
information: "Portions copyright [year] [name of copyright owner]".

Copyright 2016 ForgeRock AS.
