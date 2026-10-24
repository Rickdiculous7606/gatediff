# GateDiff Architecture & Design Guide

## Overview

GateDiff is architected around the principle of **strict behavioral equivalence testing**. When migrating microservices between gateways (e.g. Kong, Gravitee, Apigee, AWS API Gateway), behavioral drift can occur at multiple layers:
1. Routing & URL rewriting
2. HTTP status code transformations
3. Header injection/stripping
4. Payload serialization & field omissions
5. Latency & rate-limiting policies

GateDiff decouples testing into modular, independently testable layers.

```
┌────────────────────────────────────────────────────────┐
│                      GateDiff CLI                      │
│        (Picocli Commands: run, mock, config, map)      │
└──────────────────────────┬─────────────────────────────┘
                           │
       ┌───────────────────┼───────────────────┐
       ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐    ┌──────────────┐
│ConfigLoader  │   │OpenApiParser │    │LocalMockLab  │
│  & Validator │   │ & Mapper     │    │(Kong vs Grav)│
└──────┬───────┘   └──────┬───────┘    └──────────────┘
       │                  │
       ▼                  ▼
┌─────────────────────────────────┐
│           HttpRunner            │
│  (Java 21 java.net.http.Client) │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│        ComparisonEngine         │
│  - JSONPath Diff (Jackson)      │
│  - Status & Headers Comparator  │
│  - Ignored Paths & Strict Order │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│     Reporters (Terminal,        │
│     JSON, HTML, Exit Codes)     │
└─────────────────────────────────┘
```

## Core Design Tenets

1. **Deterministic Comparison Engine**:
   JSON objects with reordered keys (e.g. `{"a":1,"b":2}` vs `{"b":2,"a":1}`) are considered equal. Arrays, however, preserve order by default.
2. **Actionable JSONPath Reporting**:
   Failures point to unambiguous paths like `$.customers[0].status: expected ACTIVE but received INACTIVE`.
3. **Safe Retries Only**:
   Only idempotent methods (`GET`, `HEAD`, `OPTIONS`) can be retried on connection timeouts. `POST`, `PUT`, `DELETE` are never retried.
4. **Zero Magic / No Silently Skipped Cases**:
   Ambiguous mappings in OpenAPI are reported as `AMBIGUOUS` with candidates, rather than silently choosing a match.
