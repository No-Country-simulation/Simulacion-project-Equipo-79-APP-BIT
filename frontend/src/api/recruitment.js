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

export function initiateContact({ jobId, candidateId, recruiterNotes, decisionReason }) {
  return request('/recruitment', {
    method: 'POST',
    body: JSON.stringify({ jobId, candidateId, recruiterNotes, decisionReason }),
  });
}

export function updateStatus(id, status, decisionReason) {
  const params = new URLSearchParams();
  params.set('status', status);
  if (decisionReason) params.set('decisionReason', decisionReason);
  return request(`/recruitment/${id}/status?${params.toString()}`, {
    method: 'PUT',
  });
}

export function updateNotes(id, notes) {
  return request(`/recruitment/${id}/notes`, {
    method: 'PUT',
    body: JSON.stringify({ notes }),
  });
}

export function updateRecruitmentProcess(id, payload = {}) {
  const { status, notes, decisionReason } = payload;

  if (status) {
    return updateStatus(id, status, decisionReason);
  }

  if (notes !== undefined) {
    return updateNotes(id, notes);
  }

  return Promise.resolve(null);
}

export function findByJob(jobId) {
  return request(`/recruitment?jobId=${jobId}`);
}

export function findById(id) {
  return request(`/recruitment/${id}`);
}
