import { API_BASE_URL } from '../config/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {        
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    const responseText = await response.clone().text();

    try {
      const errorBody = responseText ? JSON.parse(responseText) : null;
      message = errorBody?.message ?? errorBody?.detail ?? message;
    } catch {
      if (responseText) {
        message = responseText;
      }
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export function listJobs() {
  return request('/jobs');
}

export function getJobById(jobId) {
  return request(`/jobs/${jobId}`);
}

export function createJob(job) {
  return request('/jobs', {
    method: 'POST',
    body: JSON.stringify(job),
  });
}

export function findMatches(requestBody) {
  return request('/jobs/matches', {
    method: 'POST',
    body: JSON.stringify(requestBody),
  });
}
