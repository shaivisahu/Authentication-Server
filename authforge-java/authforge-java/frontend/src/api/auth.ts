import api from './client'
import type { ApiResponse, AuthTokens, LoginRequest, SignupRequest, User, PageResponse } from '../types'

export const authApi = {
  signup: (data: SignupRequest) =>
    api.post<ApiResponse<AuthTokens>>('/auth/signup', data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthTokens>>('/auth/login', data),

  refresh: (refreshToken: string) =>
    api.post<ApiResponse<AuthTokens>>('/auth/refresh', { refreshToken }),

  logout: (refreshToken: string, allSessions = false) =>
    api.post<ApiResponse<void>>(`/auth/logout?allSessions=${allSessions}`, { refreshToken }),

  me: () =>
    api.get<ApiResponse<User>>('/user/profile'),
}

export const adminApi = {
  listUsers: (page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<User>>>(`/admin/users?page=${page}&size=${size}`),

  updateRole: (uuid: string, role: string) =>
    api.patch<ApiResponse<void>>(`/admin/users/${uuid}/role?role=${role}`),

  deleteUser: (uuid: string) =>
    api.delete<ApiResponse<void>>(`/admin/users/${uuid}`),
}
