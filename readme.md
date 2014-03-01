Formats the JSON objects in a stream or file.

Typical usage is to tail a logfile where we want to highlight and format any JSON content.

Properties:

* Detects and tries to pretty-print all JSON fragments in the input
* Swallows everything, it just copies the non-JSON parts
* Uses colors for Bash shell (and cygwin)
* Requires Java 5+
* Uses [Jackson parser](http://jackson.codehaus.org/) which is included in the executable
* Builds with maven3

**Example**

input:  

![input](https://bitbucket.org/bartswen/bsonify/raw/master/input.png)

output:  

![output](https://bitbucket.org/bartswen/bsonify/raw/master/output.png)

**Install**

1. If not present, install Java. Check Java version with `java -version`
1. Download the [bsonify jar file](https://bitbucket.org/bartswen/bsonify/downloads/bsonify.jar) (or build from source) into a known location, for example ~/java

**Build from source**

1. Install [maven](http://maven.apache.org/)
1. Execute `mvn package`
1. The product is target/bsonify.jar

**Usage examples**

Example in the bsonify project:  
`cat target/test-classes/test-input.txt | java -jar target/bsonify.jar`

Jar installed in ~/java:  
`tail logfile.txt | java -jar ~/java/bsonify.jar`

In cygwin use windows path for jar file:  
`tail logfile.txt | java -jar "c:/java/bsonify.jar"`

In cygwin when grepping, use --line-buffered:  
`tail -F logfile.txt | grep --line-buffered 'SomeLogger' | java -jar "c:/java/bsonify.jar"`

**Usage**

    Usage: java -jar bsonify.jar [OPTION]... [FILE]...
    Format JSON fragments in FILE, or standard input, to standard output. Copies non-JSON content.
    Requires Java 5+

      -mono                    monochrome, no colors
      -dark                    use dark colors, use this on a light background
      -light                   use light colors (default), use this on a dark background
      -compact                 print JSON object properties on one line
          --help     display this help and exit
          --version  output version information and exit

    With no FILE, read standard input.


**Design**

*Parser*

Bsonify uses the Jackson library to read the JSON fragments. Jackson provides a low level JSON pull parser. The pull feature is important because it passes the control over the parser to the program. The formatter needs to have the control to be able to stop parsing a JSON fragment in the stream when it decides that it is done.

*Main components*

The `MainReader` accepts any input. It just echoes all non-JSON content. While it reads the stream it tries to find JSON fragments by scanning for '[' and '{'. When it encounters a possible JSON fragment `MainReader` delegates to the `JsonFormatter`. The `JsonFormatter` prints the formatted fragment and returns the number of characters it has read from the stream. To determine the number of read characters the `JsonFormatter` needs to peek into the Jackson parser to fetch its state. The `MainReader` then points the stream to the character following the JSON fragment and continues its loop.

*Formatting JSON*

The `Renderer` is responsible for the actual JSON formtting and coloring. Formatting a JSON 'token' depends on the type of the previous token. I ended up defining a matrix of all token combinations. This is reflected by the switch statements in the `renderXxx()` operations.

*Test*

Most tests are high level. The [SUT](http://xunitpatterns.com/SUT.html) mostly comprises all main components. One interesting test is the `MainReaderTest.testFormatStreaming()`. It simulates the streaming mode where one thread of execution writes to the stream, and another thread reads from it by running the `MainReader.formatStream()`.

**Related**

A great JSON commandline tool is [trentm json](https://github.com/trentm/json). It contains links to other tools if you actually need to process the JSON.

**TODO**

- Bug: object in array after string or other value throws exception

