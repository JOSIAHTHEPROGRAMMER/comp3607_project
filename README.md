# comp3607_project

COMP 3607 Object Oriented Programming II, 2026/2027 Semester 1 (group project).

A BlueJ extension that records what a student does while working in BlueJ (project and class changes, typing and idle time, compilations, program runs) and exports each session as a JSON file and a PDF report.

## Requirements

- JDK 21
- Maven 3.6.3 or newer
- BlueJ 6.0.1 (the extension API jar is not published to Maven Central)
- An internet connection for the first build (Maven downloads the other dependencies from Maven Central)

Check what you have installed:

bash
java -version
mvn -v

Before the first build, install the BlueJ extension API jar from the BlueJ 6.0.1 installation (`lib/bluej.jar`) into your local Maven repository:

bash

mvn org.apache.maven.plugins:maven-install-plugin:3.1.3:install-file \
  -Dfile="/path/to/BlueJ/lib/bluej.jar" \
  -DgroupId=org.bluej -DartifactId=bluej -Dversion=6.0.1 -Dpackaging=jar

## Running the tests

From the project root (the folder that contains `pom.xml`):

bash
mvn clean test

This compiles the project, runs every JUnit 5 test and writes a code coverage report. A successful run ends like this:

Tests run: 337, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS

The number of tests grows as tests are added. What matters is **0 failures, 0 errors and BUILD SUCCESS**.

Notes:

- BlueJ does not need to be installed or running. The tests run independently of the BlueJ GUI.
- A few tests (file watching) take a couple of seconds.
- The first run may print PDFBox "font cache" messages. They are harmless.

### Running part of the test suite

bash

# one test class

mvn test -Dtest=SummaryBuilderTest

# several test classes

mvn test -Dtest=SummaryBuilderTest,CompositeSinkTest

# one test method

mvn test -Dtest=SummaryBuilderTest#emptyBuilderGivesAllZeros

### Viewing the coverage report

mvn clean test generates a JaCoCo report. Open it in a browser:

bash
open target/site/jacoco/index.html # macOS
start target\site\jacoco\index.html # Windows
xdg-open target/site/jacoco/index.html # Linux

The page lists coverage per package and class. The target is 75% or more. The latest full run measured **96% of instructions and 91% of branches**.

Coverage exclusions. The classes in adapter/, plus HelloExtension and App, are excluded from the coverage calculation (see the JaCoCo <excludes> in pom.xml). They call the BlueJ API directly and cannot run without BlueJ. The logic behind them (trackers, event models, serialiser, report code) is unit tested without BlueJ.

## What is tested

| Required area | Packages | What the tests check |

| Telemetry parser and event listeners | `lifecycle`, `compile`, `execution`, `editor_interactions`, `model` | Actions are recorded as event objects, compile errors are classified, idle gaps and file edits are detected, observers and sinks forward events and survive failing listeners |
| JSON serialiser | `json` | Output is valid, time-stamped JSON (parsed back with Gson) with session metadata (student ID placeholder, duration, compilation counts), all event fields, special characters and per-project filtering |
| Report data aggregators | `json` (summary), `pdf_exporter.metrics` | Compile totals and success counts, error frequency by category, intervals between failures, run outcomes, active time vs idle time |
| PDF report | `pdf_exporter` and its sub-packages | Report sections, the 2 to 4 page rule, charts, and an end-to-end test that generates a real PDF and reads its numbers back |

Test data: the tests build their own sample sessions in code (compile errors, successful builds, runs, class creations, key presses). There are no external data files.

The tests live in `src/test/java/comp3607_project/`, in packages that mirror `src/main/java/comp3607_project/`.

## Building the extension jar

bash
mvn clean package

The jar is created in `target/`.

## Project layout

| Package | Purpose |

| `adapter` | Connects BlueJ's listeners to the trackers |
| `lifecycle` | Project and class creation, modification and deletion |
| `compile` | Compilation successes and errors (codes, messages, line numbers) |
| `execution` | Program runs and runtime exceptions |
| `editor_interactions` | Typing cadence, idle time and edit frequency |
| `model` | Shared event interface and event sinks |
| `json` | Session summary and JSON export |
| `pdf_exporter` | Metrics, journals, charts and the PDF report |

## Troubleshooting

- `release version 21 not supported`\*\*: your JDK is older than 21. Install JDK 21 and check with `java -version`.
- `mvn: command not found`\*\*: install Maven and check with `mvn -v`.
- Dependency download errors on the first build\*\*: connect to the internet and run `mvn clean test` again.
