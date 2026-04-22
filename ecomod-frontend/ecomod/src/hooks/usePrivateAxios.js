import { useEffect } from 'react'
import { useSelector } from 'react-redux'
import { api } from '../services/api'
import { useRefresh } from './useRefresh'

export const usePrivateAxios = () => {
  const token = useSelector((state) => state.auth.token)
  const refresh = useRefresh()

  useEffect(() => {
    const requestInterceptor = api.interceptors.request.use(
      (config) => {
        if (token && !config.headers.Authorization) {
          config.headers.Authorization = `Bearer ${token}`
        }

        return config
      },
      (error) => Promise.reject(error),
    )

    const responseInterceptor = api.interceptors.response.use(
      (response) => response,
      async (error) => {
        const previousRequest = error?.config

        if (
          previousRequest &&
          !previousRequest._retry &&
          [401, 403].includes(error?.response?.status)
        ) {
          previousRequest._retry = true
          const nextToken = await refresh()
          previousRequest.headers.Authorization = `Bearer ${nextToken}`
          return api(previousRequest)
        }

        return Promise.reject(error)
      },
    )

    return () => {
      api.interceptors.request.eject(requestInterceptor)
      api.interceptors.response.eject(responseInterceptor)
    }
  }, [refresh, token])

  return api
}
