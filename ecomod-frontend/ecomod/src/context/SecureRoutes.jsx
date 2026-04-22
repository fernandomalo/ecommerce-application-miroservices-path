import { useSelector } from 'react-redux'
import { Navigate, useLocation } from 'react-router-dom'
import { isBusinessRole } from '../utils/authRole'

export const SecureRoutes = ({
  children,
  requireBusiness = false,
  requireCustomer = false,
  authIntent = 'buyer',
}) => {
  const location = useLocation()
  const { token, roles } = useSelector((state) => state.auth)
  const isBusiness = isBusinessRole(roles)

  if (!token) {
    return <Navigate to={`/account?intent=${authIntent}`} state={{ from: location }} replace />
  }

  if (requireBusiness && !isBusiness) {
    return <Navigate to="/sell" replace />
  }

  if (requireCustomer && isBusiness) {
    return <Navigate to="/seller" replace />
  }

  return children
}
