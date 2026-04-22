import { configureStore } from '@reduxjs/toolkit'
import authReducer from './authSlice'
import { clearStoredAuth, persistAuth } from '../utils/authSession'

export const store = configureStore({
  reducer: {
    auth: authReducer,
  },
})

store.subscribe(() => {
  const auth = store.getState().auth

  if (auth.token) {
    persistAuth(auth)
    return
  }

  clearStoredAuth()
})
