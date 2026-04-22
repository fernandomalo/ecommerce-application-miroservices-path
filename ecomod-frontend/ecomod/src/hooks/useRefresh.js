import { useCallback } from 'react'
import { useDispatch } from 'react-redux'
import { api } from '../services/api'
import { setSession } from '../redux/authSlice'
import { buildSessionFromToken } from '../utils/authSession'

export const useRefresh = () => {
  const dispatch = useDispatch()

  const refresh = useCallback(async () => {
    const response = await api.post('/auth/refresh')
    const session = buildSessionFromToken(response.data.accessToken)
    dispatch(setSession(session))
    return session.token
  }, [dispatch])

  return refresh
}
