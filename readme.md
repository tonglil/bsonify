Formats the JSON objects in a stream or file.

Typical usage is to tail a logfile in which we want to highlight and format any JSON content. 

Properties:

* Tries to detect and format all JSON fragments in the input, just echoes it if JSON is malformed
* Uses indentation and colors
* Uses colors for Bash shell (and cygwin)
* Java application
* Uses Jackson parser which is included in the jar
* Builds with maven

**Example output**

TODO (show array and object)

**Install**

1. Download the jar file (or build from source)
1. Make it available on path

**Build from source**

1. Install [maven](http://maven.apache.org/)
1. `mvn package`

**Usage examples**

    tail logfile.txt | java -jar jsonify-0.1.jar
    cat target/test-classes/test-input.txt | java -jar target/jsonify-0.1.jar

**Todo**

- Fix echo part of JSON content after it is formatted (stream)
- Dark and light options, light is default
- Compact option prints fields and values in one line, specify line length
- Debug option which prints the error, for example when the formatter encounters '{asdf}' in the stream it prints: '{<JSON ISSUE: Unexpected character ('a' (code 97)): was expecting double-quote to start field name>asdf}

**Design**

Use Jackson because it provides a low level JSON pull parser. Pull feature is important because it passes the control over the parser to the program. The formatter needs to be able to stop parsing a JSON fragment in the stream when it decides that it is done.

