import { useState, useEffect } from 'react'
import { getConfig, setShoutOutValue, setTeamShoutOutValue, setShoutOutAllowance, setResetInterval, getStoreItems, createStoreItem, getUsers, createUser,
         getTeams, getTeamMemberships, createTeam, deleteTeam, addTeamMember, removeTeamMember } from '../api'
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

  // Settings
  const [sovCurrent, setSovCurrent]     = useState(null)
  const [sovInput, setSovInput]         = useState('')
  const [sovAlert, setSovAlert]         = useState(null)
  const [tsovCurrent, setTsovCurrent]   = useState(null)
  const [tsovInput, setTsovInput]       = useState('')
  const [tsovAlert, setTsovAlert]       = useState(null)
  const [allowanceCurrent, setAllowanceCurrent] = useState(null)
  const [allowanceInput, setAllowanceInput]     = useState('')
  const [allowanceAlert, setAllowanceAlert]     = useState(null)
  const [riqInput, setRiqInput]   = useState('2')      // reset interval quantity
  const [riuInput, setRiuInput]   = useState('WEEK')   // reset interval unit
  const [riAlert, setRiAlert]     = useState(null)

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
  const [membershipMap, setMembershipMap] = useState({}) // { userId: ['Team A', 'Team B'] }

  function load() {
    getUsers().then(setUsers)
    getConfig().then(cfg => {
      setSovCurrent(cfg.shoutOutValue); setSovInput(String(cfg.shoutOutValue))
      setTsovCurrent(cfg.teamShoutOutValue); setTsovInput(String(cfg.teamShoutOutValue))
      setAllowanceCurrent(cfg.shoutOutAllowance); setAllowanceInput(String(cfg.shoutOutAllowance))
      setRiqInput(String(cfg.resetIntervalQuantity))
      setRiuInput(cfg.resetIntervalUnit)
    })
    getStoreItems().then(setStoreItems)
    getTeams().then(setTeams)
    getTeamMemberships().then(memberships => {
      const map = {}
      memberships.forEach(({ userId, teamName }) => {
        if (!map[userId]) map[userId] = []
        map[userId].push(teamName)
      })
      setMembershipMap(map)
    })
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

  async function submitShoutOutValue(e) {
    e.preventDefault(); setSovAlert(null)
    try {
      const v = Number(sovInput)
      await setShoutOutValue(v)
      setSovCurrent(v)
      setSovAlert({ type: 'success', text: 'Individual shout-out value updated!' })
    } catch (err) { setSovAlert({ type: 'error', text: err.message }) }
  }

  async function submitTeamShoutOutValue(e) {
    e.preventDefault(); setTsovAlert(null)
    try {
      const v = Number(tsovInput)
      await setTeamShoutOutValue(v)
      setTsovCurrent(v)
      setTsovAlert({ type: 'success', text: 'Team shout-out value updated!' })
    } catch (err) { setTsovAlert({ type: 'error', text: err.message }) }
  }

  async function submitAllowance(e) {
    e.preventDefault(); setAllowanceAlert(null)
    try {
      const v = Number(allowanceInput)
      await setShoutOutAllowance(v)
      setAllowanceCurrent(v)
      setAllowanceAlert({ type: 'success', text: 'Shout-out allowance updated!' })
    } catch (err) { setAllowanceAlert({ type: 'error', text: err.message }) }
  }

  async function submitResetInterval(e) {
    e.preventDefault(); setRiAlert(null)
    try {
      await setResetInterval(Number(riqInput), riuInput)
      setRiAlert({ type: 'success', text: 'Reset interval updated!' })
    } catch (err) { setRiAlert({ type: 'error', text: err.message }) }
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

  async function handleDeleteTeam(teamId, teamName) {
    if (!window.confirm(`Delete team "${teamName}"? This will also remove all memberships and awards.`)) return
    try {
      await deleteTeam(teamId)
      if (teamDetail && teamDetail.teamId === teamId) setTeamDetail(null)
      if (memberTeamId === String(teamId)) setMemberTeamId('')
      load()
    } catch (err) {
      setTeamAlert({ type: 'error', text: err.message })
    }
  }

  return (
    <div>
      <div className="page-title">Admin<div className="page-subtitle">Manage users, settings, store items, and teams</div></div>

      <div className="tabs">
        {['users','settings','store','teams'].map(t => (
          <button key={t} className={'tab-btn' + (tab === t ? ' active' : '')} onClick={() => setTab(t)}>
            {{ users: '👥 Users', settings: '⚙️ Settings', store: '🛍️ Store Items', teams: '🏢 Teams' }[t]}
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
              <table><thead><tr><th>Name</th><th>Email</th><th>Since</th><th>Team(s)</th></tr></thead>
                <tbody>{users.map(u => (
                  <tr key={u.id}>
                    <td style={{ fontWeight: 600 }}>{u.name}</td>
                    <td style={{ color: 'var(--muted)' }}>{u.email}</td>
                    <td style={{ color: 'var(--muted)' }}>{u.createdAt?.slice(0,10)}</td>
                    <td style={{ color: membershipMap[u.id] ? 'inherit' : 'var(--muted)' }}>
                      {membershipMap[u.id] ? membershipMap[u.id].join(', ') : '—'}
                    </td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Settings */}
      {tab === 'settings' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Individual Shout-Out Value</div>
              <Alert msg={sovAlert} />
              <form onSubmit={submitShoutOutValue}>
                <div className="form-group">
                  <label className="form-label">Points per Individual Shout-Out</label>
                  <input className="form-input" type="number" min="1" value={sovInput}
                    onChange={e => setSovInput(e.target.value)} required placeholder="10" />
                </div>
                <button className="btn btn-primary btn-block" type="submit">Save</button>
              </form>
            </div>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Current Settings</div>
              <table><tbody>
                <tr>
                  <td style={{ color: 'var(--muted)', paddingRight: 24 }}>Individual shout-out value</td>
                  <td><span style={{ color: 'var(--primary)', fontWeight: 600 }}>
                    {sovCurrent !== null ? `${sovCurrent} pts` : '—'}
                  </span></td>
                </tr>
                <tr>
                  <td style={{ color: 'var(--muted)', paddingRight: 24, paddingTop: 8 }}>Team shout-out value</td>
                  <td style={{ paddingTop: 8 }}><span style={{ color: 'var(--primary)', fontWeight: 600 }}>
                    {tsovCurrent !== null ? `${tsovCurrent} pts` : '—'}
                  </span></td>
                </tr>
              </tbody></table>
              <p style={{ marginTop: 16, color: 'var(--muted)', fontSize: 13 }}>
                Individual shout-outs cost the giver the individual value and the recipient earns that many points.
                Team shout-outs cost the giver the team value; each eligible member earns that many points.
              </p>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Team Shout-Out Value</div>
              <Alert msg={tsovAlert} />
              <form onSubmit={submitTeamShoutOutValue}>
                <div className="form-group">
                  <label className="form-label">Points per Team Shout-Out (per member)</label>
                  <input className="form-input" type="number" min="1" value={tsovInput}
                    onChange={e => setTsovInput(e.target.value)} required placeholder="5" />
                </div>
                <button className="btn btn-primary btn-block" type="submit">Save</button>
              </form>
            </div>
            <div className="card" style={{ display: 'flex', alignItems: 'flex-start' }}>
              <p style={{ color: 'var(--muted)', fontSize: 13, margin: 0 }}>
                When a team receives a shout-out, every eligible member (excluding the giver if they belong to the team)
                earns this many points. The giver uses one shout-out from their allowance.
              </p>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.6fr', gap: 24 }}>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Shout-Out Allowance</div>
              <Alert msg={allowanceAlert} />
              <form onSubmit={submitAllowance}>
                <div className="form-group">
                  <label className="form-label">Shout-Outs per Period</label>
                  <input className="form-input" type="number" min="1" value={allowanceInput}
                    onChange={e => setAllowanceInput(e.target.value)} required placeholder="10" />
                </div>
                <button className="btn btn-primary btn-block" type="submit">Save</button>
              </form>
            </div>
            <div className="card">
              <div className="section-title" style={{ marginBottom: 16 }}>Reset Period</div>
              <Alert msg={riAlert} />
              <form onSubmit={submitResetInterval}>
                <div className="form-group">
                  <label className="form-label">Reset shout-out balance every</label>
                  <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                    <select className="form-select" style={{ width: 100 }} value={riqInput}
                      onChange={e => setRiqInput(e.target.value)}>
                      {Array.from({ length: 100 }, (_, i) => i + 1).map(n => (
                        <option key={n} value={n}>{n}</option>
                      ))}
                    </select>
                    <select className="form-select" style={{ flex: 1 }} value={riuInput}
                      onChange={e => setRiuInput(e.target.value)}>
                      <option value="DAY">Day(s)</option>
                      <option value="WEEK">Week(s)</option>
                      <option value="MONTH">Month(s)</option>
                      <option value="QUARTER">Quarter(s)</option>
                      <option value="YEAR">Year(s)</option>
                    </select>
                  </div>
                </div>
                <p style={{ color: 'var(--muted)', fontSize: 12, marginBottom: 12 }}>
                  Currently: {allowanceCurrent ?? '—'} shout-out{allowanceCurrent !== 1 ? 's' : ''} every {riqInput} {riuInput.charAt(0) + riuInput.slice(1).toLowerCase()}(s)
                </p>
                <button className="btn btn-primary btn-block" type="submit">Save</button>
              </form>
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
                <table><thead><tr><th>Name</th><th>Description</th><th>Members</th><th>Total Pts</th><th></th></tr></thead>
                  <tbody>{teams.map(t => (
                    <tr key={t.id}>
                      <td style={{ fontWeight: 600 }}>{t.name}</td>
                      <td style={{ color: 'var(--muted)' }}>{t.description || '—'}</td>
                      <td>{t.memberCount}</td>
                      <td><span style={{ color: 'var(--primary)', fontWeight: 600 }}>{t.totalPoints} pts</span></td>
                      <td><button className="btn" style={{ padding: '4px 10px', fontSize: 12, background: 'var(--red)', color: '#fff' }}
                        onClick={() => handleDeleteTeam(t.id, t.name)}>Delete</button></td>
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
