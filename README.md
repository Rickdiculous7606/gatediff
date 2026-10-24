# GateDiff — API Gateway Migration Testing CLI

[![Build & Test](https://github.com/gatediff/gatediff/actions/workflows/ci.yml/badge.svg)](https://github.com/gatediff/gatediff/actions)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

**GateDiff** is an open-source, developer-friendly command-line tool and testing lab designed to validate API gateway migrations (e.g., migrating 25+ microservices from **Kong** to **Gravitee**, **Apigee**, or **AWS API Gateway**).

It enables engineering teams to verify that a target API gateway behaves with strict behavioral equivalence to the source gateway — comparing HTTP status codes, JSON payload trees, selected headers, and response latencies before cutover.

---

## Table of Contents

1. [Problem Statement](#problem-statement)
2. [What GateDiff Does & Does Not Do](#what-gatediff-does--does-not-do)
3. [Prerequisites](#prerequisites)
4. [Windows & PowerShell Setup](#windows--powershell-setup)
5. [IntelliJ IDEA Import Instructions](#intellij-idea-import-instructions)
6. [Quick Start (Local Mock Lab)](#quick-start-local-mock-lab)
7. [CLI Commands Reference](#cli-commands-reference)
8. [Configuration Schema (migration.yaml)](#configuration-schema-migrationyaml)
9. [Interpreting Failure Reports](#interpreting-failure-reports)
10. [CI/CD & Exit Code Policy](#cicd--exit-code-policy)
11. [Architecture & Package Structure](#architecture--package-structure)
12. [Security & Responsible Usage](#security--responsible-usage)

---

## Problem Statement

When organizations migrate their API estate from legacy or proprietary API gateways to new platforms:
- Teams often manage 25+ microservices with differing route mappings, path prefixes, headers, and payload structures.
- Route mapping and validation configuration are heavily manual and error-prone.
- Subtle regressions (missing response fields, altered status codes, unintended type changes, dropped headers) slip into production.
- Cutover without automated dual-run assertion creates substantial operational risk.

GateDiff solves this by automating:
1. **OpenAPI Specification Import**: Parsing endpoints, methods, and response schemas.
2. **Operation Mapping Engine**: Suggesting matches between source and target gateways with confidence scores and ambiguity detection.
3. **YAML Test Suite Generation**: Generating reproducible, versioned validation suites.
4. **Dual HTTP Execution**: Sending equivalent requests in parallel to both gateways.
5. **Deep Semantic JSON Diffs**: Identifying path-specific mismatches (e.g., `$.status: expected ACTIVE but received INACTIVE`) while safely ignoring dynamic fields (e.g. timestamps, request IDs).
6. **Multi-Format Reporting**: Terminal summaries, machine-readable JSON, and standalone HTML reports with deterministic CI exit codes.

---

## What GateDiff Does & Does Not Do

### GateDiff DOES:
- Send equivalent requests to source and target endpoints.
- Compare HTTP status codes, headers (case-insensitively), response durations, and JSON bodies.
- Parse JSON trees semantically (whitespace and key order differences do **not** cause false positives).
- Enforce strict array ordering by default.
- Support path-based ignores (e.g., `$.requestId`, `$.meta.timestamp`, wildcard `$.meta.*`).
- Detect both missing fields and unexpected additional fields.
- Provide a zero-external-dependency local mock lab simulating Kong vs. Gravitee behavior.
- Support Windows (PowerShell/CMD), macOS, and Linux.

### GateDiff DOES NOT:
- Export or reverse-engineer proprietary gateway database internals or proprietary control plane binaries.
- Automatically modify your remote gateway routes or firewalls.
- Blindly retry non-idempotent operations (POST, PUT, DELETE are never retried automatically).
- Execute arbitrary shell scripts or code from YAML configuration files.

---

## Prerequisites

- **Java Development Kit (JDK) 21** or higher (OpenJDK, Eclipse Temurin, Amazon Corretto, or Oracle JDK).
- **Apache Maven 3.8+** (or use the Maven Wrapper).
- **Git**.

Verify your installation:
```bash
java -version
mvn -version
```

---

## Windows & PowerShell Setup

GateDiff was architected specifically to provide a clean, predictable developer experience on Windows with PowerShell.

### 1. Install JDK 21 on Windows (via winget or manual)
Using Windows Package Manager (PowerShell as Administrator):
```powershell
winget install EclipseAdoptium.Temurin.21.JDK
winget install Apache.Maven
```

### 2. Verify in a fresh PowerShell window
```powershell
java -version
mvn -version
```

### 3. Clone and Build
```powershell
git clone https://github.com/gatediff/gatediff.git
cd gatediff
mvn clean test
mvn package -DskipTests
```
The executable JAR is generated at `target\gatediff.jar`.

### 4. Create a Convenient PowerShell Alias (Optional)
In your PowerShell profile (`$PROFILE`):
```powershell
function gatediff { java -jar "$HOME\gatediff\target\gatediff.jar" @args }
```

---

## IntelliJ IDEA Import Instructions

For beginners importing into IntelliJ IDEA on Windows or macOS:

1. Launch **IntelliJ IDEA**.
2. Select **File -> Open...** (or click **Open** on the Welcome screen).
3. Navigate to the root `gatediff` directory containing `pom.xml`.
4. Click **OK** and choose **Open as Project**.
5. IntelliJ will automatically detect the Maven project and download dependencies.
6. Verify the Project SDK:
   - Go to **File -> Project Structure -> Project**.
   - Ensure **SDK** is set to **21** (e.g. `temurin-21` or `openjdk-21`).
7. Run the test suite inside IntelliJ:
   - In the Project view, navigate to `src/test/java`.
   - Right-click on the `java` folder and select **Run 'All Tests'**.
   - All 38+ tests will run and display green checkmarks in the Run tool window.

---

## Quick Start (Local Mock Lab)

GateDiff includes a fictional API migration lab running on lightweight embedded Java HTTP servers. No Docker, cloud accounts, or third-party gateways are required.

### Step 1: Start the Local Lab
Open Terminal / PowerShell:
```bash
java -jar target/gatediff.jar mock start --scenario MATCHING
```
This starts:
- **Source Gateway Mock (Kong)** on `http://localhost:8081`
- **Target Gateway Mock (Gravitee)** on `http://localhost:8082`

### Step 2: Validate the Configuration
In a second terminal window:
```bash
java -jar target/gatediff.jar config validate --file examples/configs/migration.yaml
```
Output:
```text
Validating configuration: examples/configs/migration.yaml
[PASS] Configuration is valid (Version 1, 1 service(s) configured).
```

### Step 3: Run the Migration Comparison
```bash
java -jar target/gatediff.jar run \
  --config examples/configs/migration.yaml \
  --output-json target/reports/latest.json \
  --output-html target/reports/latest.html
```

### Step 4: Simulate a Gateway Regression
Start the mock lab in `VALUE_MISMATCH` mode:
```bash
java -jar target/gatediff.jar mock start --scenario VALUE_MISMATCH
```
Run `gatediff run --config examples/configs/migration.yaml`. GateDiff immediately catches the regression:
```text
[FAIL] customer-api | GET /api/customers/C-101 (8ms)
       * Source: HTTP 200 (4ms)
       * Target: HTTP 200 (4ms)
       * MISMATCH $.status: expected ACTIVE but received INACTIVE

STATUS: MIGRATION MISMATCH REGRESSIONS DETECTED (Exit code: 1)
```

---

## CLI Commands Reference

| Command | Description | Example |
| :--- | :--- | :--- |
| `gatediff --help` | Display CLI options and subcommands | `java -jar target/gatediff.jar --help` |
| `gatediff --version` | Display application version | `java -jar target/gatediff.jar --version` |
| `gatediff mock start` | Start local fictional mock gateway lab | `gatediff mock start --scenario MATCHING` |
| `gatediff config validate` | Validate YAML configuration syntax | `gatediff config validate --file migration.yaml` |
| `gatediff import openapi` | Inspect and summarize OpenAPI 3.x spec | `gatediff import openapi --input specs/api.yaml` |
| `gatediff map` | Suggest route mappings with confidence | `gatediff map --source src.yaml --target tgt.yaml` |
| `gatediff generate` | Generate migration YAML from mappings | `gatediff generate -s src.yaml -t tgt.yaml -o out.yaml` |
| `gatediff run` | Execute test comparisons across gateways | `gatediff run --config migration.yaml` |
| `gatediff report` | Render JSON test report as HTML/Terminal | `gatediff report --input report.json --format html` |

---

## Configuration Schema (migration.yaml)

```yaml
version: 1

project:
  name: customer-api-migration
  description: Validating Kong to Gravitee migration
  environment: local

defaults:
  timeoutMs: 5000       # HTTP connection and request timeout
  retries: 1            # Safe retries for idempotent GET requests only
  compare:
    status: true        # Assert HTTP status codes match
    jsonBody: true      # Deep JSON semantics comparison
    additionalFields: true  # Fail if target gateway emits unexpected extra fields
    ignoredJsonPaths:   # Paths ignored from diffing (timestamps, request IDs)
      - "$.requestId"
      - "$.meta.timestamp"
    headers:            # Headers compared case-insensitively
      - "content-type"

services:
  - id: customer-api
    name: Customer Management API
    sourceBaseUrl: "http://localhost:8081"
    targetBaseUrl: "http://localhost:8082"
    openApiSpec: "examples/specs/customer-api.yaml"
    tests:
      - id: get-customer-c101
        name: "Retrieve Active Customer C-101"
        method: GET
        path: "/api/customers/C-101"
        expectedStatus: 200

      - id: create-customer
        name: "Create Customer"
        method: POST
        path: "/api/customers"
        body: '{"name":"Acme Logistics","tier":"STANDARD"}'
        headers:
          Content-Type: application/json
        expectedStatus: 201
```

---

## Interpreting Failure Reports

When a comparison fails, GateDiff highlights the exact JSON path and mismatch type:

```text
[FAIL] customer-api | GET /api/customers/C-101
       * Source: HTTP 200 (4ms)
       * Target: HTTP 200 (3ms)
       * MISMATCH $.status: expected ACTIVE but received INACTIVE
       * MISMATCH $.tier: missing expected field 'tier'
       * MISMATCH $.loyaltyPoints: unexpected additional field 'loyaltyPoints' with value 450
```

- `$.status: expected ACTIVE but received INACTIVE`: Value mismatch in JSON body.
- `$.tier: missing expected field 'tier'`: Target gateway response dropped an expected property.
- `unexpected additional field '...'`: Target gateway returned extra unmapped fields.
- `header.content-type: expected 'application/json' but received 'text/plain'`: Header policy mismatch.

---

## CI/CD & Exit Code Policy

GateDiff adheres to a strict exit code policy designed for automated CI pipelines (GitHub Actions, GitLab CI, Jenkins, Azure DevOps):

| Exit Code | Meaning | CI Impact |
| :---: | :--- | :--- |
| **0** | **SUCCESS**: All executed comparisons passed equivalence assertions. | Pipeline proceeds |
| **1** | **COMPARISON FAILED**: One or more response differences or regressions detected. | Pipeline fails |
| **2** | **INVALID CONFIGURATION**: Schema error, missing required field, or invalid command usage. | Pipeline fails |
| **3** | **INFRASTRUCTURE ERROR**: Gateway endpoint unreachable, timeout, or DNS failure. | Pipeline fails |

---

## Architecture & Package Structure

```text
src/
  main/java/io/gatediff/
    GateDiffMain.java                 # CLI entry point
    cli/
      GateDiffCommand.java            # Picocli root command
      RunCommand.java                 # 'run' subcommand
      MockCommand.java                # 'mock' subcommand
      ConfigValidateCommand.java      # 'config validate' subcommand
      ImportOpenApiCommand.java       # 'import openapi' subcommand
      MapCommand.java                 # 'map' subcommand
      GenerateCommand.java            # 'generate' subcommand
      ReportCommand.java              # 'report' subcommand
      ExitCodes.java                  # Exit code constants (0, 1, 2, 3)
    model/
      HttpRequestSpec.java            # Request specification
      HttpResponseSnapshot.java        # Response status, headers, body, latency
      FieldMismatch.java              # JSONPath mismatch details
      ComparisonResult.java           # Aggregated comparison result
      TestExecutionResult.java        # Individual test outcome
      TestSuiteReport.java            # Entire test suite summary
    comparison/
      ComparisonEngine.java           # Deep JSON semantic comparison & header checks
    http/
      HttpRunner.java                 # Java 21 HttpClient dual runner
    config/
      GateDiffConfig.java             # Version 1 root YAML schema
      ConfigValidator.java            # Schema & semantic validator
      ConfigLoader.java               # Jackson YAML deserializer
    openapi/
      OpenApiParser.java              # OpenAPI 3.x YAML/JSON parser
      OpenApiOperation.java           # Operation metadata model
      OpenApiSpecSummary.java         # API summary model
    mapping/
      MappingSuggestionEngine.java    # Route matching heuristics & ambiguity detector
      OperationMapping.java           # Source-to-target mapping model
      YamlConfigGenerator.java        # Automated migration.yaml generator
    mock/
      LocalMockLab.java               # Embedded JDK HttpServer mock lab
      MockScenario.java               # Lab test scenarios (matching, mismatch, etc.)
    reporting/
      TerminalReporter.java           # ANSI terminal summary
      JsonReporter.java               # Machine-readable JSON report
      HtmlReporter.java               # Standalone interactive HTML report
```

---

## Security & Responsible Usage

- **Localhost Bound**: The built-in mock lab binds exclusively to `127.0.0.1`. It never listens on public network interfaces.
- **Credential Redaction**: Authorization tokens, API keys, and sensitive headers are redacted from console and report outputs.
- **No Arbitrary Code Execution**: The YAML parser only instantiates typed configuration beans. No arbitrary command execution or script evaluation is permitted.
- **Safe Retries**: Non-idempotent operations (POST, PUT, DELETE) are never retried automatically.
- **SSRF Awareness**: When validating remote gateway URLs in production environments, ensure tests run within appropriate network perimeters and VPCs.

---

## License

Licensed under the **Apache License, Version 2.0**. See [LICENSE](LICENSE) for details.
