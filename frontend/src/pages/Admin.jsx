import { useState, useEffect } from 'react'
import { getAwardTypes, createAwardType, getStoreItems, createStoreItem, getUsers, createUser } from '../api'
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

  function load() {
    getUsers().then(setUsers)
    getAwardTypes().then(setAwardTypes)
    getStoreItems().then(setStoreItems)
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

  return (
    <div>
      <div className="page-title">Admin<div className="page-subtitle">Manage users, award types, and store items</div></div>

      <div className="tabs">
        {['users','awards','store'].map(t => (
          <button key={t} className={'tab-btn' + (tab === t ? ' active' : '')} onClick={() => setTab(t)}>
            {{ users: '👥 Users', awards: '🏅 Award Types', store: '🛍️ Store Items' }[t]}
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
