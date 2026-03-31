import { useAuthStore } from '../store/authStore'
import { Shield, Key, Clock, CheckCircle, User, Lock } from 'lucide-react'

function StatCard({ icon: Icon, label, value, sub, color = 'brand' }: {
  icon: any; label: string; value: string; sub?: string; color?: string
}) {
  const colorMap: Record<string, string> = {
    brand: 'text-brand-400 bg-brand-400/10',
    blue:  'text-blue-400 bg-blue-400/10',
    amber: 'text-amber-400 bg-amber-400/10',
    green: 'text-emerald-400 bg-emerald-400/10',
  }
  return (
    <div className="card flex items-start gap-4">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${colorMap[color]}`}>
        <Icon size={18} />
      </div>
      <div>
        <p className="text-xs text-slate-500 font-medium uppercase tracking-wider">{label}</p>
        <p className="text-xl font-bold text-white mt-0.5">{value}</p>
        {sub && <p className="text-xs text-slate-500 mt-0.5">{sub}</p>}
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user)
  const accessToken = useAuthStore((s) => s.accessToken)

  const roleDisplay = user?.role?.replace('ROLE_', '') ?? 'USER'
  const tokenPreview = accessToken ? `${accessToken.slice(0, 24)}...${accessToken.slice(-8)}` : '—'

  const permissions: Record<string, string[]> = {
    ROLE_USER:       ['read:posts', 'write:posts', 'read:profile'],
    ROLE_EDITOR:     ['read:posts', 'write:posts', 'delete:posts', 'read:profile', 'view:analytics'],
    ROLE_ADMIN:      ['read:posts', 'write:posts', 'delete:posts', 'read:profile', 'view:analytics', 'manage:users'],
    ROLE_SUPERADMIN: ['read:posts', 'write:posts', 'delete:posts', 'read:profile', 'view:analytics', 'manage:users', 'system:settings'],
  }
  const myPerms = permissions[user?.role ?? 'ROLE_USER'] ?? []

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">
          Good to see you, <span className="text-brand-400">{user?.username}</span> 👋
        </h1>
        <p className="text-slate-400 text-sm mt-1">Your AuthForge security dashboard</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={Shield} label="Role"       value={roleDisplay}       sub="Access level"          color="brand" />
        <StatCard icon={User}   label="Provider"   value={user?.provider ?? 'LOCAL'} sub="Auth method"   color="blue"  />
        <StatCard icon={CheckCircle} label="Email" value={user?.emailVerified ? 'Verified' : 'Unverified'} sub={user?.email} color="green" />
        <StatCard icon={Clock}  label="Token TTL"  value="15 min"            sub="Access token lifetime" color="amber" />
      </div>

      {/* Token info */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card space-y-4">
          <div className="flex items-center gap-2">
            <Key size={16} className="text-brand-400" />
            <h2 className="font-semibold text-white text-sm">Active JWT Access Token</h2>
          </div>
          <div className="bg-slate-950 rounded-lg p-3 border border-slate-800">
            <code className="text-xs text-brand-300 font-mono break-all leading-relaxed">
              {tokenPreview}
            </code>
          </div>
          <div className="grid grid-cols-2 gap-3 text-xs">
            {[
              ['Algorithm', 'HS256'],
              ['Type',      'Bearer'],
              ['Expires',   '15 min'],
              ['Issuer',    'authforge'],
            ].map(([k, v]) => (
              <div key={k} className="bg-slate-800/60 rounded-lg p-2.5">
                <div className="text-slate-500">{k}</div>
                <div className="text-slate-200 font-mono mt-0.5">{v}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="card space-y-4">
          <div className="flex items-center gap-2">
            <Lock size={16} className="text-brand-400" />
            <h2 className="font-semibold text-white text-sm">Your Permissions</h2>
          </div>
          <div className="grid grid-cols-2 gap-2">
            {['read:posts','write:posts','delete:posts','manage:users','view:analytics','system:settings'].map(perm => {
              const granted = myPerms.includes(perm)
              return (
                <div
                  key={perm}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium border ${
                    granted
                      ? 'bg-brand-500/8 border-brand-500/20 text-brand-300'
                      : 'bg-slate-800/40 border-slate-700/40 text-slate-600'
                  }`}
                >
                  <div className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${granted ? 'bg-brand-400' : 'bg-slate-600'}`} />
                  {perm}
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {/* Auth flow info */}
      <div className="card">
        <h2 className="font-semibold text-white text-sm mb-4">How your session works</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            { step: '01', title: 'Login', desc: 'Credentials validated against bcrypt hash (cost=12). Timing-safe comparison prevents enumeration.' },
            { step: '02', title: 'JWT Issued', desc: 'HS256 access token (15min) + opaque refresh token (7d) stored in DB. Refresh token is rotated on each use.' },
            { step: '03', title: 'Auto Refresh', desc: 'When access token expires, the client silently exchanges the refresh token. Reuse attack detection revokes all sessions.' },
          ].map(({ step, title, desc }) => (
            <div key={step} className="flex gap-3">
              <div className="text-2xl font-black text-brand-500/20 font-mono flex-shrink-0">{step}</div>
              <div>
                <div className="text-sm font-semibold text-white">{title}</div>
                <div className="text-xs text-slate-500 mt-1 leading-relaxed">{desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
