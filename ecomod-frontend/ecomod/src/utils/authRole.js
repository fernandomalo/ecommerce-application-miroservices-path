export function normalizeRole(rawRoles) {
  if (Array.isArray(rawRoles)) {
    if (rawRoles.includes('BUSINESS')) return 'BUSINESS'
    if (rawRoles.includes('CUSTOMER')) return 'CUSTOMER'
    return rawRoles[0] ?? ''
  }

  if (typeof rawRoles !== 'string') {
    return ''
  }

  const roles = rawRoles
    .split(/[,\s]+/)
    .map((role) => role.trim())
    .filter(Boolean)

  if (roles.includes('BUSINESS')) return 'BUSINESS'
  if (roles.includes('CUSTOMER')) return 'CUSTOMER'

  return roles[0] ?? rawRoles
}

export function isBusinessRole(rawRoles) {
  return normalizeRole(rawRoles) === 'BUSINESS'
}

export function getDefaultRouteForRole(rawRoles) {
  return isBusinessRole(rawRoles) ? '/seller' : '/me'
}

export function getRouteForIntent(intent, rawRoles) {
  if (intent === 'seller') {
    return isBusinessRole(rawRoles) ? '/seller' : '/sell'
  }

  return getDefaultRouteForRole(rawRoles)
}

export function resolvePostLoginRoute({ requestedPath, intent, rawRoles }) {
  if (!requestedPath || requestedPath === '/account') {
    return getRouteForIntent(intent, rawRoles)
  }

  if (requestedPath === '/seller' || requestedPath === '/inventory') {
    return isBusinessRole(rawRoles) ? '/seller' : '/sell'
  }

  if (requestedPath === '/sell') {
    return isBusinessRole(rawRoles) ? '/seller' : '/sell'
  }

  if ((requestedPath === '/me' || requestedPath === '/cart') && isBusinessRole(rawRoles)) {
    return '/seller'
  }

  return requestedPath
}
