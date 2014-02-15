Formats the JSON objects in a stream or file.

Typical usage is to tail a logfile in which we want to highlight and format any JSON content. 

Properties:

* Tries to detect and format all JSON fragments in the input, just echoes it if JSON is malformed
* Uses indentation and colors
* Uses colors for Bash shell (and cygwin)
* Java application
* Uses Jackson parser which is included in the jar
* Builds with maven3

**Example**

input:  

![input](https://bitbucket.org/bartswen/bsonify/raw/master/input.png)

output:  

![output](https://bitbucket.org/bartswen/bsonify/raw/master/output.png)

**Install**

1. Download the jar file (or build from source) into a known location, for example ~/java

**Build from source**

1. Install [maven](http://maven.apache.org/)
1. `mvn package`

**Usage examples**

- In the project dir: `cat target/test-classes/test-input.txt | java -jar target/bsonify-0.1.jar`
- Jar installed in ~/java: `tail logfile.txt | java -jar ~/java/bsonify-0.2.jar`

**Todo**

- Multithreaded unit test to simulate a tail
- Dark and light options, light is default
- Compact option prints fields and values in one line, specify line length
- Debug option which prints the error, for example when the formatter encounters '{asdf}' in the stream it prints: '{<JSON ISSUE: Unexpected character ('a' (code 97)): was expecting double-quote to start field name>asdf}

**Design**

Use Jackson because it provides a low level JSON pull parser. Pull feature is important because it passes the control over the parser to the program. The formatter needs to be able to stop parsing a JSON fragment in the stream when it decides that it is done.

