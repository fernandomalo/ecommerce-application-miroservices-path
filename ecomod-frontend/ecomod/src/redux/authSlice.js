import { createSlice } from '@reduxjs/toolkit'
import { getStoredAuth } from '../utils/authSession'

const storedAuth = getStoredAuth()

const initialState = storedAuth ?? {
  token: '',
  email: '',
  roles: '',
  userId: null,
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setSession: (state, action) => {
      state.token = action.payload.token
      state.email = action.payload.email
      state.roles = action.payload.roles
      state.userId = action.payload.userId
    },
    clearSession: () => ({
      token: '',
      email: '',
      roles: '',
      userId: null,
    }),
  },
})

export const { setSession, clearSession } = authSlice.actions
export default authSlice.reducer
