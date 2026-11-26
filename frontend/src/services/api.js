/**
 * API Service for interacting with the Spring Boot backend
 */

const BASE_URL = '/api';

export async function fetchHealth() {
  try {
    const res = await fetch(`${BASE_URL}/health`);
    if (!res.ok) {
      throw new Error(`Health check returned status ${res.status}`);
    }
    return await res.json();
  } catch (err) {
    console.error('Failed to fetch backend health:', err);
    return {
      status: 'DOWN',
      error: err.message,
      cParserAvailable: false,
      cppParserAvailable: false,
      pythonParserAvailable: false
    };
  }
}

export async function parseCode(language, sourceCode) {
  const res = await fetch(`${BASE_URL}/parse`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ language, sourceCode })
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || data.error || 'Parsing failed');
  }
  return data;
}

export async function comparePrograms(payload) {
  const res = await fetch(`${BASE_URL}/compare`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || data.error || 'Comparison failed');
  }
  return data;
}

export function getJsonReportUrl(comparisonId) {
  return `${BASE_URL}/compare/${comparisonId}/report/json`;
}

export function getCsvReportUrl(comparisonId) {
  return `${BASE_URL}/compare/${comparisonId}/report/csv`;
}
