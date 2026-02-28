import { useState, useEffect } from 'react'
import { getUsers, getAwardTypes, getPoints, giveAward } from '../api'
import { useUser } from '../context/UserContext'

export default function GiveAward() {
  const { currentUser } = useUser()
  const [users, setUsers]           = useState([])
  const [awardTypes, setAwardTypes] = useState([])
  const [pts, setPts]               = useState(null)

  const [recipientId,  setRecipientId]  = useState('')
  const [awardTypeId,  setAwardTypeId]  = useState(null)
  const [message,      setMessage]      = useState('')
  const [submitting,   setSubmitting]   = useState(false)
  const [alert,        setAlert]        = useState(null)

  useEffect(() => {
    getUsers().then(setUsers)
    getAwardTypes().then(setAwardTypes)
  }, [])

  useEffect(() => {
    if (currentUser) getPoints(currentUser.id).then(setPts)
  }, [currentUser])

  const selectedType = awardTypes.find(a => a.id === awardTypeId)
  const canSubmit = recipientId && awardTypeId && !submitting &&
                    pts && (selectedType?.pointsCost ?? 0) <= pts.givingBalance

  async function handleSubmit(e) {
    e.preventDefault()
    setAlert(null)
    setSubmitting(true)
    try {
      await giveAward({ giverId: currentUser.id, recipientId: Number(recipientId), awardTypeId, message })
      setAlert({ type: 'success', text: '🎉 Award given successfully!' })
      setRecipientId('')
      setAwardTypeId(null)
      setMessage('')
      getPoints(currentUser.id).then(setPts)
    } catch (err) {
      setAlert({ type: 'error', text: err.message })
    } finally {
      setSubmitting(false)
    }
  }

  if (!currentUser) return <p style={{ color: 'var(--muted)' }}>Select a user to give an award.</p>

  const peers = users.filter(u => u.id !== currentUser.id)

  return (
    <div style={{ maxWidth: 640 }}>
      <div className="page-title">
        Give an Award
        <div className="page-subtitle">Recognize a colleague from your quarterly giving bank</div>
      </div>

      <div className="stats-row" style={{ marginBottom: 28 }}>
        <div className="stat-card amber">
          <div className="stat-label">Your Giving Balance</div>
          <div className="stat-value">{pts?.givingBalance ?? '—'}</div>
          <div className="stat-sub">of {pts?.givingAllowance ?? 20} pts this quarter</div>
        </div>
        {selectedType && (
          <div className="stat-card indigo">
            <div className="stat-label">This Award Costs</div>
            <div className="stat-value">{selectedType.pointsCost}</div>
            <div className="stat-sub">pts from your balance</div>
          </div>
        )}
      </div>

      {alert && <div className={`alert alert-${alert.type}`}>{alert.text}</div>}

      <form className="card" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Recipient</label>
          <select className="form-select" value={recipientId} onChange={e => setRecipientId(e.target.value)} required>
            <option value="">Select a colleague…</option>
            {peers.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Award Type</label>
          <div className="award-grid">
            {awardTypes.map(a => (
              <div
                key={a.id}
                className={'award-option' + (awardTypeId === a.id ? ' selected' : '') + (a.pointsCost > (pts?.givingBalance ?? 0) ? ' disabled' : '')}
                style={a.pointsCost > (pts?.givingBalance ?? 0) ? { opacity: .45, cursor: 'not-allowed' } : {}}
                onClick={() => a.pointsCost <= (pts?.givingBalance ?? 0) && setAwardTypeId(a.id)}
              >
                <div className="award-option-name">{a.name}</div>
                <div className="award-option-pts">{a.pointsCost} pts</div>
                {a.description && <div className="award-option-desc">{a.description}</div>}
              </div>
            ))}
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Personal Message <span style={{ color: 'var(--muted)', fontWeight: 400 }}>(optional)</span></label>
          <textarea
            className="form-textarea"
            placeholder="Share why you're recognizing them…"
            value={message}
            onChange={e => setMessage(e.target.value)}
          />
        </div>

        <button className="btn btn-primary btn-block" disabled={!canSubmit} type="submit">
          {submitting ? 'Sending…' : '🎁 Give Award'}
        </button>
        {selectedType && pts && selectedType.pointsCost > pts.givingBalance && (
          <p style={{ color: 'var(--red)', fontSize: 13, marginTop: 8, textAlign: 'center' }}>
            Insufficient giving balance for this award type.
          </p>
        )}
      </form>
    </div>
  )
}
