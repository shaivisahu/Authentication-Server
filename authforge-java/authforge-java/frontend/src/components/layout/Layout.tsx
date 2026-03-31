import { Link, useLocation } from 'react-router-dom'
import { Shield, LayoutDashboard, Users, LogOut, ChevronDown } from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { useAuthStore } from '../../store/authStore'

interface LayoutProps { children: React.ReactNode }

export default function Layout({ children }: LayoutProps) {
  const { logout, user } = useAuth()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const isAdmin = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_SUPERADMIN'

  const navItems = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    ...(isAdmin ? [{ to: '/admin', icon: Users, label: 'Admin' }] : []),
  ]

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      {/* Navbar */}
      <nav className="bg-slate-900 border-b border-slate-800 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <div className="flex items-center gap-8">
              <Link to="/dashboard" className="flex items-center gap-2.5">
                <div className="w-8 h-8 bg-brand-500 rounded-lg flex items-center justify-center">
                  <Shield size={16} className="text-slate-950" />
                </div>
                <span className="font-bold text-white text-lg tracking-tight">AuthForge</span>
              </Link>
              {/* Nav links */}
              <div className="hidden md:flex items-center gap-1">
                {navItems.map(({ to, icon: Icon, label }) => (
                  <Link
                    key={to} to={to}
                    className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                      location.pathname === to
                        ? 'bg-slate-800 text-white'
                        : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                    }`}
                  >
                    <Icon size={15} />
                    {label}
                  </Link>
                ))}
              </div>
            </div>

            {/* User menu */}
            <div className="relative">
              <button
                onClick={() => setMenuOpen(!menuOpen)}
                className="flex items-center gap-2.5 bg-slate-800 hover:bg-slate-700 px-3 py-2 rounded-lg transition-colors"
              >
                <div className="w-7 h-7 bg-brand-500/20 rounded-full flex items-center justify-center text-brand-400 text-xs font-bold">
                  {user?.username?.charAt(0).toUpperCase()}
                </div>
                <div className="hidden sm:block text-left">
                  <div className="text-sm font-medium text-white">{user?.username}</div>
                  <div className="text-xs text-slate-400">{user?.role?.replace('ROLE_', '')}</div>
                </div>
                <ChevronDown size={14} className="text-slate-400" />
              </button>

              {menuOpen && (
                <div className="absolute right-0 mt-2 w-52 bg-slate-800 border border-slate-700 rounded-xl shadow-xl overflow-hidden animate-fade-in">
                  <div className="px-4 py-3 border-b border-slate-700">
                    <div className="text-sm font-medium text-white">{user?.username}</div>
                    <div className="text-xs text-slate-400 truncate">{user?.email}</div>
                  </div>
                  <div className="p-1">
                    <button
                      onClick={() => { setMenuOpen(false); logout(false) }}
                      className="w-full flex items-center gap-2.5 px-3 py-2.5 text-sm text-red-400 hover:bg-red-400/10 rounded-lg transition-colors"
                    >
                      <LogOut size={14} />
                      Sign out
                    </button>
                    <button
                      onClick={() => { setMenuOpen(false); logout(true) }}
                      className="w-full flex items-center gap-2.5 px-3 py-2.5 text-sm text-slate-400 hover:bg-slate-700 rounded-lg transition-colors"
                    >
                      <LogOut size={14} />
                      Sign out all devices
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* Main content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  )
}
