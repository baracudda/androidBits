androidBits
===========

Library of useful Android classes

## Android App Development Setup (Android Studio) ##

1. Android Studio uses Gradle to build apps. A way to use the androidBits library with
your app is to edit the global `gradle.properties` file on your local computer.
Create this file in the Gradle user home directory, typically found on Windows 7+ at
`C:\Users\[user name]\.gradle` and on Linux or Mac OS X at `~/.gradle`.

2. Edit the new `gradle.properties` file to contain the following variables.
Note the lack of quotes and the full paths below, so paths with spaces may have unknown behavior:

  For Linux / OS X:
  ```
  lib_androidBits=/path/to/androidBits/lib_androidBits
  ```
  For Windows OS (yes, the extra backslashes are required):
  ```
  lib_androidBits=C\:\\path\\to\\androidBits\\lib_androidBits
  ```
  
3. In your project's `settings.gradle` file, include the lines:
  ```
  include 'androidBits'
  project(':androidBits').projectDir = new File(lib_androidBits)
  ```

4. In your project's `build.gradle` file, include the dependency:
  ```
  dependencies {  
      compile project(':androidBits')  
  }  
  ```

