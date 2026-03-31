import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuthStore } from '../store/authStore'
import { authApi } from '../api/auth'
import type { LoginRequest, SignupRequest } from '../types'

export function useAuth() {
  const { setAuth, logout: storeLogout, refreshToken, user, isAuthenticated } = useAuthStore()
  const navigate = useNavigate()

  const signup = useCallback(async (data: SignupRequest) => {
    const res = await authApi.signup(data)
    const { accessToken, refreshToken: rt, ...userInfo } = res.data.data
    setAuth({ accessToken, refreshToken: rt }, userInfo as any)
    toast.success('Account created! Welcome.')
    navigate('/dashboard')
  }, [setAuth, navigate])

  const login = useCallback(async (data: LoginRequest) => {
    const res = await authApi.login(data)
    const { accessToken, refreshToken: rt, ...userInfo } = res.data.data
    setAuth({ accessToken, refreshToken: rt }, userInfo as any)
    toast.success(`Welcome back, ${userInfo.username}!`)
    navigate('/dashboard')
  }, [setAuth, navigate])

  const logout = useCallback(async (allSessions = false) => {
    try {
      if (refreshToken) await authApi.logout(refreshToken, allSessions)
    } catch (_) { /* ignore */ }
    storeLogout()
    navigate('/login')
    toast.success('Logged out successfully')
  }, [refreshToken, storeLogout, navigate])

  return { signup, login, logout, user, isAuthenticated }
}
