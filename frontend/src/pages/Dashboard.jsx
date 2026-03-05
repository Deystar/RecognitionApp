import { useState, useEffect } from 'react'
import { getPoints, getShoutOutsReceived, getShoutOutsGiven, getPurchases, getStoreItems, getUsers, getTeams } from '../api'
import { useUser } from '../context/UserContext'

export default function Dashboard() {
  const { currentUser } = useUser()
  const [pts, setPts]               = useState(null)
  const [received, setReceived]     = useState([])
  const [given, setGiven]           = useState([])
  const [purchases, setPurchases]   = useState([])
  const [storeItems, setStoreItems] = useState({})
  const [userMap, setUserMap]       = useState({})
  const [teamMap, setTeamMap]       = useState({})
  const [loading, setLoading]       = useState(true)

  useEffect(() => {
    if (!currentUser) return
    setLoading(true)
    Promise.all([
      getPoints(currentUser.id),
      getShoutOutsReceived(currentUser.id),
      getShoutOutsGiven(currentUser.id),
      getPurchases(currentUser.id),
      getStoreItems(),
      getUsers(),
      getTeams(),
    ]).then(([p, rec, giv, pur, si, users, teams]) => {
      setPts(p)
      setReceived(rec)
      setGiven(giv)
      setPurchases(pur)
      setStoreItems(Object.fromEntries(si.map(s => [s.id, s])))
      setUserMap(Object.fromEntries(users.map(u => [u.id, u])))
      setTeamMap(Object.fromEntries(teams.map(t => [t.id, t])))
    }).finally(() => setLoading(false))
  }, [currentUser])

  if (!currentUser) return <p style={{ color: 'var(--muted)' }}>Select a user to view their dashboard.</p>
  if (loading)      return <p style={{ color: 'var(--muted)' }}>Loading…</p>

  const givingPct = pts && pts.givingAllowance > 0
    ? Math.min(100, Math.round(((pts.givingAllowance - pts.givingBalance) / pts.givingAllowance) * 100))
    : 0

  function shoutOutLabel(so) {
    if (so.recipientTeamId != null) return teamMap[so.recipientTeamId]?.name ?? `Team #${so.recipientTeamId}`
    if (so.recipientUserId != null) return userMap[so.recipientUserId]?.name ?? `User #${so.recipientUserId}`
    return 'Unknown'
  }

  return (
    <div>
      <div className="page-title">
        {currentUser.name}'s Dashboard
        <div className="page-subtitle">{currentUser.email}</div>
      </div>

      <div className="stats-row">
        <div className="stat-card amber">
          <div className="stat-label">Shout-Outs Remaining</div>
          <div className="stat-value">{pts?.givingBalance ?? '—'}</div>
          <div className="stat-sub">of {pts?.givingAllowance ?? '—'} this period</div>
          <div className="progress-bar-wrap">
            <div className="progress-bar" style={{ width: givingPct + '%', background: 'var(--amber)' }} />
          </div>
        </div>
        <div className="stat-card green">
          <div className="stat-label">Spendable Balance</div>
          <div className="stat-value">{pts?.spendableBalance ?? '—'}</div>
          <div className="stat-sub">pts to redeem in store</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Earned</div>
          <div className="stat-value">{pts?.totalEarned ?? '—'}</div>
          <div className="stat-sub">pts received all time</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Spent</div>
          <div className="stat-value">{pts?.totalSpent ?? '—'}</div>
          <div className="stat-sub">pts redeemed in store</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>

        <div className="card">
          <div className="section-header">
            <div className="section-title">Shout-Outs Received</div>
            <span style={{ fontSize: 13, color: 'var(--muted)' }}>{received.length}</span>
          </div>
          {received.length === 0
            ? <div className="empty" style={{ padding: '24px 0' }}><div>No shout-outs received yet</div></div>
            : <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {received.slice(0, 5).map(so => (
                  <div key={so.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
                    <div>
                      <span style={{ fontWeight: 600 }}>from {userMap[so.giverId]?.name ?? `User #${so.giverId}`}</span>
                      {so.recipientTeamId != null && (
                        <span style={{ color: 'var(--muted)' }}> · via {teamMap[so.recipientTeamId]?.name}</span>
                      )}
                      <span style={{ color: 'var(--muted)' }}> · {so.givenAt?.slice(0, 10)}</span>
                      {so.message && <div style={{ color: 'var(--muted)', fontSize: 12, marginTop: 2 }}>"{so.message}"</div>}
                    </div>
                    <span style={{ color: 'var(--primary)', fontWeight: 600, whiteSpace: 'nowrap' }}>+{so.points} pts</span>
                  </div>
                ))}
              </div>
          }
        </div>

        <div className="card">
          <div className="section-header">
            <div className="section-title">Shout-Outs Given</div>
            <span style={{ fontSize: 13, color: 'var(--muted)' }}>{given.length}</span>
          </div>
          {given.length === 0
            ? <div className="empty" style={{ padding: '24px 0' }}><div>No shout-outs given yet</div></div>
            : <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {given.slice(0, 5).map(so => (
                  <div key={so.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
                    <div>
                      <span style={{ fontWeight: 600 }}>to {shoutOutLabel(so)}</span>
                      {so.recipientTeamId != null && (
                        <span style={{ color: 'var(--muted)' }}> (team)</span>
                      )}
                      <span style={{ color: 'var(--muted)' }}> · {so.givenAt?.slice(0, 10)}</span>
                      {so.message && <div style={{ color: 'var(--muted)', fontSize: 12, marginTop: 2 }}>"{so.message}"</div>}
                    </div>
                    <span style={{ color: 'var(--amber)', fontWeight: 600, whiteSpace: 'nowrap' }}>-{so.points} pts</span>
                  </div>
                ))}
              </div>
          }
        </div>

        <div className="card" style={{ gridColumn: '1 / -1' }}>
          <div className="section-header">
            <div className="section-title">Recent Purchases</div>
            <span style={{ fontSize: 13, color: 'var(--muted)' }}>{purchases.length}</span>
          </div>
          {purchases.length === 0
            ? <div className="empty" style={{ padding: '24px 0' }}><div>No store purchases yet</div></div>
            : <div className="table-wrap">
                <table>
                  <thead><tr><th>Item</th><th>Points Spent</th><th>Date</th></tr></thead>
                  <tbody>
                    {purchases.map(p => (
                      <tr key={p.id}>
                        <td>{storeItems[p.storeItemId]?.name ?? 'Item #' + p.storeItemId}</td>
                        <td style={{ color: 'var(--red)', fontWeight: 600 }}>-{p.pointsSpent} pts</td>
                        <td style={{ color: 'var(--muted)' }}>{p.purchasedAt?.slice(0, 10)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
          }
        </div>

      </div>
    </div>
  )
}
