import { jwtDecode } from 'jwt-decode'
import { normalizeRole } from './authRole'

const AUTH_STORAGE_KEY = 'ecomod_auth'

export function getStoredAuth() {
  if (typeof window === 'undefined') {
    return null
  }

  try {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function persistAuth(session) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearStoredAuth() {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function buildSessionFromToken(accessToken, fallbackRoles = '') {
  const decoded = jwtDecode(accessToken)
  const normalizedRole = normalizeRole(decoded?.roles ?? fallbackRoles)

  return {
    token: accessToken,
    email: decoded?.sub ?? '',
    roles: normalizedRole,
    userId: decoded?.user_id ?? null,
  }
}
