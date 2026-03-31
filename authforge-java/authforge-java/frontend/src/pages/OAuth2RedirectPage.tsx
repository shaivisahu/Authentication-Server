import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuthStore } from '../store/authStore'
import { authApi } from '../api/auth'
import { Shield } from 'lucide-react'

export default function OAuth2RedirectPage() {
  const [params] = useSearchParams()
  const navigate  = useNavigate()
  const { setAuth, logout } = useAuthStore()

  useEffect(() => {
    const token        = params.get('token')
    const refreshToken = params.get('refreshToken')
    const error        = params.get('error')

    if (error) {
      toast.error(decodeURIComponent(error))
      navigate('/login')
      return
    }

    if (!token || !refreshToken) {
      toast.error('OAuth2 login failed — missing tokens')
      navigate('/login')
      return
    }

    // Fetch user profile with the new token
    ;(async () => {
      try {
        useAuthStore.setState({ accessToken: token, refreshToken, isAuthenticated: false })
        const res = await authApi.me()
        const user = res.data.data
        setAuth({ accessToken: token, refreshToken }, {
          userId:   user.uuid,
          email:    user.email,
          username: user.username,
          role:     user.role,
        })
        toast.success('Signed in with Google!')
        navigate('/dashboard')
      } catch {
        logout()
        toast.error('Failed to load profile after OAuth2 login')
        navigate('/login')
      }
    })()
  }, [])

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center">
      <div className="text-center space-y-4">
        <div className="w-14 h-14 bg-brand-500/10 rounded-2xl flex items-center justify-center mx-auto">
          <Shield size={28} className="text-brand-400 animate-pulse" />
        </div>
        <p className="text-slate-400 text-sm">Completing sign in…</p>
      </div>
    </div>
  )
}
