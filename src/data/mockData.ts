export interface ScenarioConfig {
  id: string;
  name: string;
  description: string;
  sourceStatus: number;
  targetStatus: number;
  sourceDurationMs: number;
  targetDurationMs: number;
  sourceHeaders: Record<string, string>;
  targetHeaders: Record<string, string>;
  sourceBody: Record<string, any>;
  targetBody: Record<string, any>;
  expectedMismatches: Array<{
    path: string;
    type: string;
    message: string;
  }>;
}

export const LAB_SCENARIOS: Record<string, ScenarioConfig> = {
  MATCHING: {
    id: 'MATCHING',
    name: 'Matching (Equivalence)',
    description: 'Source and target return identical status 200, matching headers, and matching JSON body.',
    sourceStatus: 200,
    targetStatus: 200,
    sourceDurationMs: 42,
    targetDurationMs: 38,
    sourceHeaders: {
      'content-type': 'application/json; charset=utf-8',
      'x-gateway-provider': 'Kong-Enterprise-3.4',
    },
    targetHeaders: {
      'content-type': 'application/json; charset=utf-8',
      'x-gateway-provider': 'Gravitee-APIM-4.2',
    },
    sourceBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
      name: 'Acme Corp',
      requestId: 'req-src-984',
    },
    targetBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
      name: 'Acme Corp',
      requestId: 'req-tgt-112',
    },
    expectedMismatches: [],
  },
  VALUE_MISMATCH: {
    id: 'VALUE_MISMATCH',
    name: 'Value Mismatch',
    description: 'Target gateway returns "INACTIVE" status instead of "ACTIVE".',
    sourceStatus: 200,
    targetStatus: 200,
    sourceDurationMs: 45,
    targetDurationMs: 40,
    sourceHeaders: {
      'content-type': 'application/json; charset=utf-8',
      'x-gateway-provider': 'Kong-Enterprise-3.4',
    },
    targetHeaders: {
      'content-type': 'application/json; charset=utf-8',
      'x-gateway-provider': 'Gravitee-APIM-4.2',
    },
    sourceBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
      name: 'Acme Corp',
      requestId: 'req-src-984',
    },
    targetBody: {
      id: 'C-101',
      status: 'INACTIVE',
      tier: 'STANDARD',
      name: 'Acme Corp',
      requestId: 'req-tgt-112',
    },
    expectedMismatches: [
      {
        path: '$.status',
        type: 'VALUE_MISMATCH',
        message: 'expected ACTIVE but received INACTIVE',
      },
    ],
  },
  STATUS_MISMATCH: {
    id: 'STATUS_MISMATCH',
    name: 'Status Code Mismatch',
    description: 'Target gateway returns HTTP 404 instead of HTTP 200.',
    sourceStatus: 200,
    targetStatus: 404,
    sourceDurationMs: 35,
    targetDurationMs: 25,
    sourceHeaders: {
      'content-type': 'application/json; charset=utf-8',
    },
    targetHeaders: {
      'content-type': 'application/json; charset=utf-8',
    },
    sourceBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
    },
    targetBody: {
      error: 'Resource not found in target gateway',
      code: 'GATEWAY_NOT_FOUND',
    },
    expectedMismatches: [
      {
        path: '$.status',
        type: 'STATUS_MISMATCH',
        message: 'expected HTTP 200 but received HTTP 404',
      },
    ],
  },
  MISSING_FIELD: {
    id: 'MISSING_FIELD',
    name: 'Missing Field in Target',
    description: 'Target gateway transformation policy dropped the "tier" property.',
    sourceStatus: 200,
    targetStatus: 200,
    sourceDurationMs: 40,
    targetDurationMs: 32,
    sourceHeaders: { 'content-type': 'application/json' },
    targetHeaders: { 'content-type': 'application/json' },
    sourceBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
      name: 'Acme Corp',
    },
    targetBody: {
      id: 'C-101',
      status: 'ACTIVE',
      name: 'Acme Corp',
    },
    expectedMismatches: [
      {
        path: '$.tier',
        type: 'MISSING_FIELD',
        message: "missing expected field 'tier'",
      },
    ],
  },
  ADDITIONAL_FIELD: {
    id: 'ADDITIONAL_FIELD',
    name: 'Unexpected Additional Field',
    description: 'Target gateway emitted an unexpected extra field "loyaltyPoints".',
    sourceStatus: 200,
    targetStatus: 200,
    sourceDurationMs: 44,
    targetDurationMs: 41,
    sourceHeaders: { 'content-type': 'application/json' },
    targetHeaders: { 'content-type': 'application/json' },
    sourceBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
    },
    targetBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
      loyaltyPoints: 450,
    },
    expectedMismatches: [
      {
        path: '$.loyaltyPoints',
        type: 'ADDITIONAL_FIELD',
        message: "unexpected additional field 'loyaltyPoints' with value 450",
      },
    ],
  },
  SERVER_ERROR: {
    id: 'SERVER_ERROR',
    name: 'Target Gateway 500 Error',
    description: 'Target gateway plugin failed and responded with HTTP 500.',
    sourceStatus: 200,
    targetStatus: 500,
    sourceDurationMs: 38,
    targetDurationMs: 110,
    sourceHeaders: { 'content-type': 'application/json' },
    targetHeaders: { 'content-type': 'application/json' },
    sourceBody: {
      id: 'C-101',
      status: 'ACTIVE',
      tier: 'STANDARD',
    },
    targetBody: {
      error: 'Internal Server Error in target gateway policy engine',
    },
    expectedMismatches: [
      {
        path: '$.status',
        type: 'STATUS_MISMATCH',
        message: 'expected HTTP 200 but received HTTP 500',
      },
    ],
  },
  HEADER_MISMATCH: {
    id: 'HEADER_MISMATCH',
    name: 'Header Mismatch',
    description: 'Target returns content-type text/plain instead of application/json.',
    sourceStatus: 200,
    targetStatus: 200,
    sourceDurationMs: 39,
    targetDurationMs: 37,
    sourceHeaders: { 'content-type': 'application/json; charset=utf-8' },
    targetHeaders: { 'content-type': 'text/plain' },
    sourceBody: { id: 'C-101', status: 'ACTIVE' },
    targetBody: { id: 'C-101', status: 'ACTIVE' },
    expectedMismatches: [
      {
        path: 'header.content-type',
        type: 'HEADER_MISMATCH',
        message: "header 'content-type' expected 'application/json; charset=utf-8' but received 'text/plain'",
      },
    ],
  },
};

export const SAMPLE_MIGRATION_YAML = `# GateDiff API Gateway Migration Validation Suite
version: 1

project:
  name: fictional-customer-migration
  description: Migration verification from Kong Gateway (8081) to Gravitee APIM (8082)
  environment: local

defaults:
  timeoutMs: 5000
  retries: 1
  compare:
    status: true
    jsonBody: true
    additionalFields: true
    ignoredJsonPaths:
      - "$.requestId"
      - "$.meta.timestamp"
    headers:
      - "content-type"

services:
  - id: customer-api
    name: Fictional Customer Service
    sourceBaseUrl: "http://localhost:8081"
    targetBaseUrl: "http://localhost:8082"
    openApiSpec: "examples/specs/customer-api.yaml"
    tests:
      - id: get-customer-c101
        name: "Retrieve Active Customer C-101"
        method: GET
        path: "/api/customers/C-101"
        expectedStatus: 200

      - id: list-all-customers
        name: "List All Customers"
        method: GET
        path: "/api/customers"
        expectedStatus: 200

      - id: create-customer
        name: "Create Customer Record"
        method: POST
        path: "/api/customers"
        body: '{"name":"Acme Logistics","tier":"STANDARD"}'
        headers:
          Content-Type: application/json
        expectedStatus: 201

      - id: delete-customer
        name: "Delete Customer Record"
        method: DELETE
        path: "/api/customers/C-101"
        expectedStatus: 204
`;

export const MAPPED_OPERATIONS = [
  {
    status: 'CONFIRMED',
    confidence: 100,
    source: 'GET /api/customers',
    target: 'GET /api/customers',
    reason: 'Exact method (GET) and matching path (/api/customers)',
    operationId: 'listCustomers',
  },
  {
    status: 'CONFIRMED',
    confidence: 100,
    source: 'POST /api/customers',
    target: 'POST /api/customers',
    reason: 'Exact method (POST) and matching path (/api/customers)',
    operationId: 'createCustomer',
  },
  {
    status: 'CONFIRMED',
    confidence: 100,
    source: 'GET /api/customers/{id}',
    target: 'GET /api/customers/{id}',
    reason: 'Exact method (GET) and matching path (/api/customers/{id})',
    operationId: 'getCustomerById',
  },
  {
    status: 'CONFIRMED',
    confidence: 100,
    source: 'DELETE /api/customers/{id}',
    target: 'DELETE /api/customers/{id}',
    reason: 'Exact method (DELETE) and matching path (/api/customers/{id})',
    operationId: 'deleteCustomer',
  },
  {
    status: 'SUGGESTED',
    confidence: 75,
    source: 'GET /api/v1/customers/{id}/orders',
    target: 'GET /api/customers/{id}/orders',
    reason: 'Matching method with normalized path prefix rewrite (/api/v1 -> /api)',
    operationId: 'getCustomerOrders',
  },
  {
    status: 'AMBIGUOUS',
    confidence: 50,
    source: 'GET /api/search',
    target: '[Multiple Candidates]',
    reason: 'Matches both GET /api/v2/search and GET /api/filter with equal score',
    candidates: ['GET /api/v2/search', 'GET /api/filter'],
  },
  {
    status: 'UNRESOLVED',
    confidence: 0,
    source: 'POST /api/admin/cache-purge',
    target: 'NONE',
    reason: 'No matching route in target specification (legacy Kong-only admin endpoint)',
  },
];
