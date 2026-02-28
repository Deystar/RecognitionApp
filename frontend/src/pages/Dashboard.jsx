import { useState, useEffect } from 'react'
import { getPoints, getAwardsReceived, getAwardsGiven, getPurchases, getAwardTypes, getStoreItems } from '../api'
import { useUser } from '../context/UserContext'

export default function Dashboard() {
  const { currentUser } = useUser()
  const [pts, setPts]           = useState(null)
  const [received, setReceived] = useState([])
  const [given, setGiven]       = useState([])
  const [purchases, setPurchases] = useState([])
  const [awardTypes, setAwardTypes] = useState({})
  const [storeItems, setStoreItems] = useState({})
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    if (!currentUser) return
    setLoading(true)
    Promise.all([
      getPoints(currentUser.id),
      getAwardsReceived(currentUser.id),
      getAwardsGiven(currentUser.id),
      getPurchases(currentUser.id),
      getAwardTypes(),
      getStoreItems()
    ]).then(([p, rec, giv, pur, at, si]) => {
      setPts(p)
      setReceived(rec)
      setGiven(giv)
      setPurchases(pur)
      setAwardTypes(Object.fromEntries(at.map(a => [a.id, a])))
      setStoreItems(Object.fromEntries(si.map(s => [s.id, s])))
    }).finally(() => setLoading(false))
  }, [currentUser])

  if (!currentUser) return <p style={{ color: 'var(--muted)' }}>Select a user to view their dashboard.</p>
  if (loading)      return <p style={{ color: 'var(--muted)' }}>Loading…</p>

  const givingPct = pts ? Math.round((pts.givingBalance / pts.givingAllowance) * 100) : 0

  return (
    <div>
      <div className="page-title">
        {currentUser.name}'s Dashboard
        <div className="page-subtitle">{currentUser.email}</div>
      </div>

      <div className="stats-row">
        <div className="stat-card amber">
          <div className="stat-label">Giving Balance</div>
          <div className="stat-value">{pts?.givingBalance ?? '—'}</div>
          <div className="stat-sub">of {pts?.givingAllowance} pts this quarter</div>
          <div className="progress-bar-wrap">
            <div className="progress-bar" style={{ width: givingPct + '%', background: 'var(--amber)' }} />
          </div>
        </div>
        <div className="stat-card green">
          <div className="stat-label">Spendable Balance</div>
          <div className="stat-value">{pts?.spendableBalance ?? '—'}</div>
          <div className="stat-sub">pts to redeem in store</div>
        </div>
        <div className="stat-card indigo">
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
            <div className="section-title">Awards Received</div>
            <span style={{ fontSize: 13, color: 'var(--muted)' }}>{received.length}</span>
          </div>
          {received.length === 0
            ? <div className="empty" style={{ padding: '24px 0' }}><div>No awards received yet</div></div>
            : <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {received.slice(0, 5).map(a => (
                  <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
                    <div>
                      <span style={{ fontWeight: 600 }}>{awardTypes[a.awardTypeId]?.name ?? 'Award'}</span>
                      <span style={{ color: 'var(--muted)' }}> · {a.givenAt?.slice(0, 10)}</span>
                    </div>
                    <span style={{ color: 'var(--primary)', fontWeight: 600 }}>+{a.points} pts</span>
                  </div>
                ))}
              </div>
          }
        </div>

        <div className="card">
          <div className="section-header">
            <div className="section-title">Awards Given</div>
            <span style={{ fontSize: 13, color: 'var(--muted)' }}>{given.length}</span>
          </div>
          {given.length === 0
            ? <div className="empty" style={{ padding: '24px 0' }}><div>No awards given yet</div></div>
            : <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {given.slice(0, 5).map(a => (
                  <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
                    <div>
                      <span style={{ fontWeight: 600 }}>{awardTypes[a.awardTypeId]?.name ?? 'Award'}</span>
                      <span style={{ color: 'var(--muted)' }}> · {a.givenAt?.slice(0, 10)}</span>
                    </div>
                    <span style={{ color: 'var(--amber)', fontWeight: 600 }}>-{a.points} pts</span>
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
