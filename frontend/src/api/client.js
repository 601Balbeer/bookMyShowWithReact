const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

/** Sends a request to the BMS API and returns its JSON response. */
export async function apiRequest(path, options = {}) {
  const token = localStorage.getItem('bms-token')
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (!response.ok) {
    const responseBody = await response.text()
    let message = responseBody
    try {
      const parsedBody = JSON.parse(responseBody)
      message = parsedBody.message || parsedBody.detail || parsedBody.error || responseBody
    } catch {
      // Keep the response text when the backend does not return JSON.
    }
    if (response.status === 401) {
      message = path === '/users/login'
        ? 'Email or password is incorrect.'
        : 'Your session is no longer valid. Please sign in again.'
    }
    throw new Error(message || `Request failed (${response.status})`)
  }
  if (response.status === 204) return null
  return response.json()
}
