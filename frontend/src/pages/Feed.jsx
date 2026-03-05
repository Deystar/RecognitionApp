import { useState, useEffect } from 'react'
import { getShoutOutFeed } from '../api'

function initials(name) {
  return name ? name.split(' ').map(p => p[0]).join('').slice(0, 2).toUpperCase() : '?'
}

function timeAgo(ts) {
  if (!ts) return ''
  const diff = Date.now() - new Date(ts).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1)  return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs  < 24) return `${hrs}h ago`
  return `${Math.floor(hrs / 24)}d ago`
}

export default function Feed() {
  const [feed, setFeed]       = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  useEffect(() => {
    getShoutOutFeed(50)
      .then(setFeed)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p style={{ color: 'var(--muted)' }}>Loading feed…</p>
  if (error)   return <div className="alert alert-error">{error}</div>

  return (
    <div>
      <div className="page-title">
        Recognition Feed
        <div className="page-subtitle">{feed.length} recent shout-outs</div>
      </div>

      {feed.length === 0
        ? <div className="empty">
            <div className="empty-icon">📣</div>
            <div className="empty-text">No shout-outs yet — be the first to give one!</div>
          </div>
        : <div className="feed">
            {feed.map((item, i) => (
              <div className="feed-card" key={item.id}>
                <div className="feed-header">
                  <div className="avatar">{initials(item.recipientName)}</div>
                  <div>
                    <div className="feed-names">
                      <span className="to">{item.recipientName}</span>
                      <span className="from"> · from {item.giverName}</span>
                    </div>
                  </div>
                  <span className={`badge badge-type badge-${i % 5}`}>
                    {item.team ? '🏢 Team' : '👤 Individual'}
                  </span>
                  <span className="badge badge-pts">+{item.points} pts</span>
                </div>
                {item.message && <div className="feed-message">"{item.message}"</div>}
                <div className="feed-time">{timeAgo(item.givenAt)}</div>
              </div>
            ))}
          </div>
      }
    </div>
  )
}
