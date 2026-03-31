import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User, Role } from '../types'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: User | null
  isAuthenticated: boolean

  setAuth: (tokens: { accessToken: string; refreshToken: string }, user: Partial<User> & { role: Role; email: string; username: string; userId: string }) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,

      setAuth: (tokens, userInfo) =>
        set({
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          isAuthenticated: true,
          user: {
            uuid: userInfo.userId,
            username: userInfo.username,
            email: userInfo.email,
            role: userInfo.role,
            provider: 'LOCAL',
            emailVerified: false,
            createdAt: new Date().toISOString(),
          },
        }),

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken }),

      logout: () =>
        set({ accessToken: null, refreshToken: null, user: null, isAuthenticated: false }),
    }),
    {
      name: 'authforge-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
