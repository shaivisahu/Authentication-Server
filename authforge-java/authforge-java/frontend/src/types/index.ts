export interface User {
  uuid: string
  username: string
  email: string
  role: Role
  provider: 'LOCAL' | 'GOOGLE' | 'GITHUB'
  emailVerified: boolean
  createdAt: string
}

export type Role = 'ROLE_USER' | 'ROLE_EDITOR' | 'ROLE_ADMIN' | 'ROLE_SUPERADMIN'

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userId: string
  email: string
  username: string
  role: Role
}

export interface ApiResponse<T> {
  success: boolean
  message: string | null
  data: T
  timestamp: string
}

export interface SignupRequest {
  username: string
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
