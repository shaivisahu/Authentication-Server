import { useEffect, useState, useCallback } from 'react'
import { Users, Shield, Trash2, RefreshCw } from 'lucide-react'
import toast from 'react-hot-toast'
import { adminApi } from '../api/auth'
import type { User } from '../types'

const ROLES = ['ROLE_USER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPERADMIN']

const roleBadge: Record<string, string> = {
  ROLE_USER:       'badge-green',
  ROLE_EDITOR:     'badge-blue',
  ROLE_ADMIN:      'badge-yellow',
  ROLE_SUPERADMIN: 'badge-red',
}

export default function AdminPage() {
  const [users, setUsers]     = useState<User[]>([])
  const [total, setTotal]     = useState(0)
  const [page, setPage]       = useState(0)
  const [loading, setLoading] = useState(true)

  const fetchUsers = useCallback(async () => {
    setLoading(true)
    try {
      const res = await adminApi.listUsers(page, 10)
      setUsers(res.data.data.content)
      setTotal(res.data.data.totalElements)
    } catch {
      toast.error('Failed to load users')
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => { fetchUsers() }, [fetchUsers])

  const handleRoleChange = async (uuid: string, role: string) => {
    try {
      await adminApi.updateRole(uuid, role)
      toast.success('Role updated')
      fetchUsers()
    } catch { toast.error('Failed to update role') }
  }

  const handleDelete = async (uuid: string, username: string) => {
    if (!confirm(`Delete user "${username}"? This cannot be undone.`)) return
    try {
      await adminApi.deleteUser(uuid)
      toast.success('User deleted')
      fetchUsers()
    } catch { toast.error('Failed to delete user') }
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Users size={22} className="text-brand-400" />
            User Management
          </h1>
          <p className="text-slate-400 text-sm mt-1">{total} total users</p>
        </div>
        <button onClick={fetchUsers} className="btn-secondary w-auto px-4">
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          Refresh
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center py-16">
            <RefreshCw size={20} className="animate-spin text-brand-400" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-800">
                  {['User', 'Role', 'Provider', 'Verified', 'Joined', 'Actions'].map(h => (
                    <th key={h} className="px-5 py-3.5 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {users.map(user => (
                  <tr key={user.uuid} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-brand-500/10 flex items-center justify-center text-brand-400 font-bold text-xs flex-shrink-0">
                          {user.username.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <div className="font-medium text-white">{user.username}</div>
                          <div className="text-slate-500 text-xs">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <select
                        value={user.role}
                        onChange={e => handleRoleChange(user.uuid, e.target.value)}
                        className="bg-slate-800 border border-slate-700 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-1 focus:ring-brand-500"
                      >
                        {ROLES.map(r => (
                          <option key={r} value={r}>{r.replace('ROLE_', '')}</option>
                        ))}
                      </select>
                    </td>
                    <td className="px-5 py-4">
                      <span className={`badge ${user.provider === 'GOOGLE' ? 'badge-blue' : 'badge-green'}`}>
                        {user.provider}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <span className={`badge ${user.emailVerified ? 'badge-green' : 'badge-yellow'}`}>
                        {user.emailVerified ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-slate-400 text-xs">
                      {new Date(user.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-5 py-4">
                      <button
                        onClick={() => handleDelete(user.uuid, user.username)}
                        className="text-slate-600 hover:text-red-400 transition-colors p-1.5 rounded-lg hover:bg-red-400/10"
                        title="Delete user"
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {total > 10 && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-800">
            <span className="text-xs text-slate-500">
              Page {page + 1} of {Math.ceil(total / 10)}
            </span>
            <div className="flex gap-2">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                className="text-xs btn-secondary w-auto px-3 py-1.5 disabled:opacity-40">
                Previous
              </button>
              <button onClick={() => setPage(p => p + 1)} disabled={(page + 1) * 10 >= total}
                className="text-xs btn-secondary w-auto px-3 py-1.5 disabled:opacity-40">
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
