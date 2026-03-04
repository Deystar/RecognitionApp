import { useState, useEffect } from 'react'
import { getAwardTypes, createAwardType, getStoreItems, createStoreItem, getUsers, createUser,
         getTeams, createTeam, addTeamMember, removeTeamMember } from '../api'
import { useUser } from '../context/UserContext'

function Alert({ msg }) {
  if (!msg) return null
  return <div className={`alert alert-${msg.type}`}>{msg.text}</div>
}

export default function Admin() {
  const { refresh } = useUser()
  const [tab, setTab] = useState('users')

  // Users
  const [users, setUsers]       = useState([])
  const [userName, setUserName] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [userAlert, setUserAlert] = useState(null)

  // Award types
  const [awardTypes, setAwardTypes]     = useState([])
  const [atName, setAtName]             = useState('')
  const [atDesc, setAtDesc]             = useState('')
  const [atCost, setAtCost]             = useState('')
  const [atAlert, setAtAlert]           = useState(null)

  // Store items
  const [storeItems, setStoreItems]     = useState([])
  const [siName, setSiName]             = useState('')
  const [siDesc, setSiDesc]             = useState('')
  const [siCost, setSiCost]             = useState('')
  const [siQty,  setSiQty]              = useState('')
  const [siAlert, setSiAlert]           = useState(null)

  // Teams
  const [teams, setTeams]               = useState([])
  const [teamName, setTeamName]         = useState('')
  const [teamDesc, setTeamDesc]         = useState('')
  const [teamAlert, setTeamAlert]       = useState(null)
  const [memberTeamId, setMemberTeamId] = useState('')
  const [memberUserId, setMemberUserId] = useState('')
  const [memberAlert, setMemberAlert]   = useState(null)
  const [teamDetail, setTeamDetail]     = useState(null) // { team, members }

  function load() {
    getUsers().then(setUsers)
    getAwardTypes().then(setAwardTypes)
    getStoreItems().then(setStoreItems)
    getTeams().then(setTeams)
  }
  useEffect(load, [])

  async function submitUser(e) {
    e.preventDefault(); setUserAlert(null)
    try {
      await createUser(userName, userEmail)
      setUserName(''); setUserEmail('')
      setUserAlert({ type: 'success', text: 'User created!' })
      load(); refresh()
    } catch (err) { setUserAlert({ type: 'error', text: err.message }) }
  }

  async function submitAwardType(e) {
    e.preventDefault(); setAtAlert(null)
    try {
      await createAwardType({ name: atName, description: atDesc, pointsCost: Number(atCost) })
      setAtName(''); setAtDesc(''); setAtCost('')
      setAtAlert({ type: 'success', text: 'Award type created!' })
      load()
    } catch (err) { setAtAlert({ type: 'error', text: err.message }) }
  }

  async function submitStoreItem(e) {
    e.preventDefault(); setSiAlert(null)
    try {
      await createStoreItem({ name: siName, description: siDesc, pointsCost: Number(siCost), quantityAvailable: siQty ? Number(siQty) : null })
      setSiName(''); setSiDesc(''); setSiCost(''); setSiQty('')
      setSiAlert({ type: 'success', text: 'Store item added!' })
      load()
    } catch (err) { setSiAlert({ type: 'error', text: err.message }) }
  }

  async function submitTeam(e) {
    e.preventDefault(); setTeamAlert(null)
    try {
      await createTeam({ name: teamName, description: teamDesc })
      setTeamName(''); setTeamDesc('')
      setTeamAlert({ type: 'success', text: 'Team created!' })
      load()
    } catch (err) { setTeamAlert({ type: 'error', text: err.message }) }
  }

  async function submitAddMember(e) {
    e.preventDefault(); setMemberAlert(null)
    try {
      await addTeamMember(Number(memberTeamId), Number(memberUserId))
      setMemberAlert({ type: 'success', text: 'Member added!' })
      load()
      if (teamDetail && teamDetail.teamId === Number(memberTeamId)) loadTeamDetail(Number(memberTeamId))
    } catch (err) { setMemberAlert({ type: 'error', text: err.message }) }
  }

  async function loadTeamDetail(teamId) {
    const t = teams.find(t => t.id === teamId)
    if (!t) return
    // fetch members via the /api/teams/{id} endpoint
    const data = await fetch(`/api/teams/${teamId}`).then(r => r.json())
    setTeamDetail({ teamId, name: t.name, members: data.members || [] })
  }

  async function handleRemoveMember(teamId, userId) {
    await removeTeamMember(teamId, userId)
    load()
    loadTeamDetail(teamId)
  }

  return (
    <div>
      <div className="page-title">Admin<div className="page-subtitle">Manage users, award types, store items, and teams</div></div>

      <div className="tabs">
        {['users','awards','store','teams'].map(t => (
          <button key={t} className={'tab-btn' + (tab === t ? ' active' : '')} onClick={() => setTab(t)}>
            {{ users: '👥 Users', awards: '🏅 Award Types', store: '🛍️ Store Items', teams: '🏢 Teams' }[t]}
          </button>
        ))}
      </div>

      {/* Users */}
      {tab === 'users' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
          <div className="card">
            <div className="section-title" style={{ marginBottom: 16 }}>Add User</div>
            <Alert msg={userAlert} />
            <form onSubmit={submitUser}>
              <div className="form-group"><label className="form-label">Full Name</label>
                <input className="form-input" value={userName} onChange={e => setUserName(e.target.value)} required placeholder="Jane Doe" /></div>
              <div className="form-group"><label className="form-label">Email</label>
                <input className="form-input" type="email" value={userEmail} onChange={e => setUserEmail(e.target.value)} required placeholder="jane@company.com" /></div>
              <button className="btn btn-primary btn-block" type="submit">Add User</button>
            </form>
          </div>
          <div className="card">
            <div className="section-title" style={{ marginBottom: 16 }}>All Users ({users.length})</div>
            <div className="table-wrap">
              <table><thead><tr><th>Name</th><th>Email</th><th>Since</th></tr></thead>
                <tbody>{users.map(u => (
                  <tr key={u.id}><td style={{ fontWeight: 600 }}>{u.name}</td><td style={{ color: 'var(--muted)' }}>{u.email}</td><td style={{ color: 'var(--muted)' }}>{u.createdAt?.slice(0,10)}</td></tr>
                ))}</tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Award Types */}
      {tab === 'awards' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
          <div className="card">
            <div className="section-title" style={{ marginBottom: 16 }}>Add Award Type</div>
            <Alert msg={atAlert} />
            <form onSubmit={submitAwardType}>
              <div className="form-group"><label className="form-label">Name</label>
                <input className="form-input" value={atName} onChange={e => setAtName(e.target.value)} required placeholder="Above & Beyond" /></div>
              <div className="form-group"><label className="form-label">Description</label>
                <input className="form-input" value={atDesc} onChange={e => setAtDesc(e.target.value)} placeholder="Optional description" /></div>
              <div className="form-group"><label className="form-label">Point Cost</label>
                <input className="form-input" type="number" min="1" max="20" value={atCost} onChange={e => setAtCost(e.target.value)} required placeholder="10" /></div>
              <button className="btn btn-primary btn-block" type="submit">Add Award Type</button>
            </form>
          </div>
          <div className="card">
            <div className="section-title" style={{ marginBottom: 16 }}>Award Types ({awardTypes.length})</div>
            <div className="table-wrap">
              <table><thead><tr><th>Name</th><th>Description</th><th>Cost</th></tr></thead>
                <tbody>{awardTypes.map(a => (
                  <tr key={a.id}><td style={{ fontWeight: 600 }}>{a.name}</td><td style={{ color: 'var(--muted)' }}>{a.description}</td>
                    <td><span style={{ color: 'var(--primary)', fontWeight: 600 }}>{a.pointsCost} pts</span></td></tr>
                ))}</tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Teams */}
      {tab === 'teams' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Create Team</div>
              <Alert msg={teamAlert} />
              <form onSubmit={submitTeam}>
                <div className="form-group"><label className="form-label">Team Name</label>
                  <input className="form-input" value={teamName} onChange={e => setTeamName(e.target.value)} required placeholder="Engineering" /></div>
                <div className="form-group"><label className="form-label">Description</label>
                  <input className="form-input" value={teamDesc} onChange={e => setTeamDesc(e.target.value)} placeholder="Optional description" /></div>
                <button className="btn btn-primary btn-block" type="submit">Create Team</button>
              </form>
            </div>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>All Teams ({teams.length})</div>
              <div className="table-wrap">
                <table><thead><tr><th>Name</th><th>Description</th><th>Members</th><th>Total Pts</th></tr></thead>
                  <tbody>{teams.map(t => (
                    <tr key={t.id}>
                      <td style={{ fontWeight: 600 }}>{t.name}</td>
                      <td style={{ color: 'var(--muted)' }}>{t.description || '—'}</td>
                      <td>{t.memberCount}</td>
                      <td><span style={{ color: 'var(--primary)', fontWeight: 600 }}>{t.totalPoints} pts</span></td>
                    </tr>
                  ))}</tbody>
                </table>
              </div>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Add Member to Team</div>
              <Alert msg={memberAlert} />
              <form onSubmit={submitAddMember}>
                <div className="form-group"><label className="form-label">Team</label>
                  <select className="form-select" value={memberTeamId} onChange={e => { setMemberTeamId(e.target.value); if (e.target.value) loadTeamDetail(Number(e.target.value)) }} required>
                    <option value="">Select a team…</option>
                    {teams.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                  </select></div>
                <div className="form-group"><label className="form-label">User</label>
                  <select className="form-select" value={memberUserId} onChange={e => setMemberUserId(e.target.value)} required>
                    <option value="">Select a user…</option>
                    {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                  </select></div>
                <button className="btn btn-primary btn-block" type="submit">Add Member</button>
              </form>
            </div>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>
                {teamDetail ? `Members of ${teamDetail.name}` : 'Select a team to manage members'}
              </div>
              {teamDetail && teamDetail.members.length === 0 && (
                <div className="empty" style={{ padding: '16px 0' }}><div>No members yet</div></div>
              )}
              {teamDetail && teamDetail.members.length > 0 && (
                <div className="table-wrap">
                  <table><thead><tr><th>Name</th><th>Email</th><th></th></tr></thead>
                    <tbody>{teamDetail.members.map(m => (
                      <tr key={m.id}>
                        <td style={{ fontWeight: 600 }}>{m.name}</td>
                        <td style={{ color: 'var(--muted)' }}>{m.email}</td>
                        <td><button className="btn" style={{ padding: '4px 10px', fontSize: 12, background: 'var(--red)', color: '#fff' }}
                          onClick={() => handleRemoveMember(teamDetail.teamId, m.id)}>Remove</button></td>
                      </tr>
                    ))}</tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Store Items */}
      {tab === 'store' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
          <div className="card">
            <div className="section-title" style={{ marginBottom: 16 }}>Add Store Item</div>
            <Alert msg={siAlert} />
            <form onSubmit={submitStoreItem}>
              <div className="form-group"><label className="form-label">Name</label>
                <input className="form-input" value={siName} onChange={e => setSiName(e.target.value)} required placeholder="Coffee Voucher" /></div>
              <div className="form-group"><label className="form-label">Description</label>
                <input className="form-input" value={siDesc} onChange={e => setSiDesc(e.target.value)} placeholder="Optional description" /></div>
              <div className="form-group"><label className="form-label">Point Cost</label>
                <input className="form-input" type="number" min="1" value={siCost} onChange={e => setSiCost(e.target.value)} required placeholder="25" /></div>
              <div className="form-group"><label className="form-label">Quantity <span style={{ color: 'var(--muted)', fontWeight: 400 }}>(leave blank for unlimited)</span></label>
                <input className="form-input" type="number" min="1" value={siQty} onChange={e => setSiQty(e.target.value)} placeholder="Unlimited" /></div>
              <button className="btn btn-primary btn-block" type="submit">Add Item</button>
            </form>
          </div>
          <div className="card">
            <div className="section-title" style={{ marginBottom: 16 }}>Store Items ({storeItems.length})</div>
            <div className="table-wrap">
              <table><thead><tr><th>Name</th><th>Cost</th><th>Stock</th></tr></thead>
                <tbody>{storeItems.map(s => (
                  <tr key={s.id}><td style={{ fontWeight: 600 }}>{s.name}</td>
                    <td><span style={{ color: 'var(--primary)', fontWeight: 600 }}>{s.pointsCost} pts</span></td>
                    <td style={{ color: 'var(--muted)' }}>{s.quantityAvailable === null ? '∞ unlimited' : s.quantityAvailable}</td></tr>
                ))}</tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
