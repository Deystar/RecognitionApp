import { useState, useEffect } from 'react'
import { getStoreItems, getPoints, purchaseItem, getConfig } from '../api'
import { useUser } from '../context/UserContext'

export default function Store() {
  const { currentUser }       = useUser()
  const [items, setItems]               = useState([])
  const [pts, setPts]                   = useState(null)
  const [loading, setLoading]           = useState(true)
  const [alerts, setAlerts]             = useState({})
  const [externalEnabled, setExternalEnabled] = useState(false)

  function loadData() {
    Promise.all([
      getStoreItems(),
      currentUser ? getPoints(currentUser.id) : Promise.resolve(null),
      getConfig()
    ]).then(([i, p, cfg]) => {
      setItems(i)
      setPts(p)
      setExternalEnabled(cfg?.externalStoreEnabled ?? false)
    }).finally(() => setLoading(false))
  }

  useEffect(() => { loadData() }, [currentUser])

  async function handlePurchase(item) {
    if (!currentUser) return
    setAlerts(prev => ({ ...prev, [item.id]: null }))
    try {
      await purchaseItem(currentUser.id, item.id)
      setAlerts(prev => ({ ...prev, [item.id]: { type: 'success', text: `✅ Redeemed!` } }))
      loadData()
    } catch (err) {
      setAlerts(prev => ({ ...prev, [item.id]: { type: 'error', text: err.message } }))
    }
  }

  if (loading) return <p style={{ color: 'var(--muted)' }}>Loading store…</p>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 }}>
        <div className="page-title" style={{ marginBottom: 0 }}>
          Company Store
          <div className="page-subtitle">Redeem your earned points for rewards</div>
        </div>
        {pts && (
          <div className="stat-card green" style={{ minWidth: 170 }}>
            <div className="stat-label">Your Balance</div>
            <div className="stat-value">{pts.spendableBalance}</div>
            <div className="stat-sub">pts available to spend</div>
          </div>
        )}
      </div>

      {items.length === 0
        ? <div className="empty"><div className="empty-icon">🛍️</div><div className="empty-text">No items in the store yet — add some in Admin!</div></div>
        : <div className="store-grid">
            {items.map(item => {
              const outOfStock = item.quantityAvailable !== null && item.quantityAvailable <= 0
              const canAfford  = pts && pts.spendableBalance >= item.pointsCost
              const alert      = alerts[item.id]
              return (
                <div className="store-card" key={`${item.source}-${item.id}`}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <div className="store-card-name" style={{ margin: 0 }}>{item.name}</div>
                    {item.source === 'external' && (
                      <span className="badge badge-external">External</span>
                    )}
                  </div>
                  {item.description && <div className="store-card-desc">{item.description}</div>}
                  <div className="store-card-cost">
                    {item.pointsCost} <span>pts</span>
                  </div>
                  {item.quantityAvailable !== null && (
                    <div className={'store-card-stock' + (outOfStock ? ' out-of-stock' : '')}>
                      {outOfStock ? 'Out of stock' : `${item.quantityAvailable} remaining`}
                    </div>
                  )}
                  {alert && <div className={`alert alert-${alert.type}`} style={{ margin: 0, padding: '8px 12px' }}>{alert.text}</div>}
                  <button
                    className={'btn btn-success btn-block'}
                    disabled={!currentUser || outOfStock || !canAfford}
                    onClick={() => handlePurchase(item)}
                  >
                    {outOfStock ? 'Out of Stock' : !canAfford ? 'Not enough pts' : 'Redeem'}
                  </button>
                </div>
              )
            })}
          </div>
      }
    </div>
  )
}
