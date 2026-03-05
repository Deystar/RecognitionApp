import { useState, useEffect } from 'react'
import { getUsers, getPoints, getTeams, getUserTeams, getConfig, giveShoutOut } from '../api'
import { useUser } from '../context/UserContext'

export default function GiveAward() {
  const { currentUser } = useUser()
  const [tab, setTab] = useState('person')

  const [pts, setPts]                           = useState(null)
  const [shoutOutValue, setShoutOutValue]       = useState(null)
  const [teamShoutOutValue, setTeamShoutOutValue] = useState(null)

  // Person shout-out state
  const [users, setUsers]               = useState([])
  const [recipientId, setRecipientId]   = useState('')
  const [personMsg, setPersonMsg]       = useState('')
  const [personSubmitting, setPersonSubmitting] = useState(false)
  const [personAlert, setPersonAlert]   = useState(null)

  // Team shout-out state
  const [teams, setTeams]               = useState([])
  const [userTeamIds, setUserTeamIds]   = useState(new Set())
  const [selectedTeam, setSelectedTeam] = useState(null)
  const [teamMsg, setTeamMsg]           = useState('')
  const [teamSubmitting, setTeamSubmitting] = useState(false)
  const [teamAlert, setTeamAlert]       = useState(null)

  useEffect(() => {
    getUsers().then(setUsers)
    getTeams().then(setTeams)
    getConfig().then(cfg => {
      setShoutOutValue(cfg?.shoutOutValue ?? 10)
      setTeamShoutOutValue(cfg?.teamShoutOutValue ?? 5)
    })
  }, [])

  useEffect(() => {
    if (!currentUser) return
    getPoints(currentUser.id).then(setPts)
    getUserTeams(currentUser.id).then(ts => setUserTeamIds(new Set(ts.map(t => t.id))))
  }, [currentUser])

  const canAffordIndividual = pts !== null && pts.givingBalance > 0
  const canAffordTeam       = pts !== null && pts.givingBalance > 0

  async function handlePersonSubmit(e) {
    e.preventDefault()
    setPersonAlert(null); setPersonSubmitting(true)
    try {
      await giveShoutOut({ giverId: currentUser.id, recipientUserId: Number(recipientId), message: personMsg || null })
      setPersonAlert({ type: 'success', text: '🎉 Shout-out sent!' })
      setRecipientId(''); setPersonMsg('')
      getPoints(currentUser.id).then(setPts)
    } catch (err) {
      setPersonAlert({ type: 'error', text: err.message })
    } finally {
      setPersonSubmitting(false)
    }
  }

  async function handleTeamSubmit(e) {
    e.preventDefault()
    setTeamAlert(null); setTeamSubmitting(true)
    try {
      await giveShoutOut({ giverId: currentUser.id, teamId: selectedTeam.id, message: teamMsg || null })
      setTeamAlert({ type: 'success', text: `📣 Shout-out sent to ${selectedTeam.name}!` })
      setSelectedTeam(null); setTeamMsg('')
      getPoints(currentUser.id).then(setPts)
    } catch (err) {
      setTeamAlert({ type: 'error', text: err.message })
    } finally {
      setTeamSubmitting(false)
    }
  }

  if (!currentUser) return <p style={{ color: 'var(--muted)' }}>Select a user to give a shout-out.</p>

  const peers = users.filter(u => u.id !== currentUser.id)

  return (
    <div style={{ maxWidth: 680 }}>
      <div className="page-title">
        Give a Shout-Out
        <div className="page-subtitle">
          Recognize a colleague ({shoutOutValue ?? '…'} pts) or a whole team ({teamShoutOutValue ?? '…'} pts per member)
        </div>
      </div>

      <div className="stats-row" style={{ marginBottom: 28 }}>
        <div className="stat-card amber">
          <div className="stat-label">Shout-Outs Remaining</div>
          <div className="stat-value">{pts?.givingBalance ?? '—'}</div>
          <div className="stat-sub">of {pts?.givingAllowance ?? '—'} this period</div>
        </div>
        <div className="stat-card indigo">
          <div className="stat-label">Individual Value</div>
          <div className="stat-value">{shoutOutValue ?? '—'}</div>
          <div className="stat-sub">pts per person shout-out</div>
        </div>
        <div className="stat-card indigo">
          <div className="stat-label">Team Value</div>
          <div className="stat-value">{teamShoutOutValue ?? '—'}</div>
          <div className="stat-sub">pts per member (team shout-out)</div>
        </div>
      </div>

      {pts !== null && !canAffordIndividual && (
        <div className="alert alert-error" style={{ marginBottom: 20 }}>
          You have no shout-outs remaining this period. Your balance resets automatically.
        </div>
      )}

      <div className="tabs" style={{ marginBottom: 24 }}>
        <button className={'tab-btn' + (tab === 'person' ? ' active' : '')} onClick={() => setTab('person')}>
          👤 Shout-Out a Person
        </button>
        <button className={'tab-btn' + (tab === 'team' ? ' active' : '')} onClick={() => setTab('team')}>
          🏢 Shout-Out a Team
        </button>
      </div>

      {/* ── Individual Shout-Out ── */}
      {tab === 'person' && (
        <>
          {personAlert && <div className={`alert alert-${personAlert.type}`} style={{ marginBottom: 16 }}>{personAlert.text}</div>}
          <form className="card" onSubmit={handlePersonSubmit}>
            <div className="form-group">
              <label className="form-label">Recipient</label>
              <select className="form-select" value={recipientId} onChange={e => setRecipientId(e.target.value)} required>
                <option value="">Select a colleague…</option>
                {peers.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">
                Message <span style={{ color: 'var(--muted)', fontWeight: 400 }}>(optional)</span>
              </label>
              <textarea
                className="form-textarea"
                placeholder="Share why you're recognizing them…"
                value={personMsg}
                onChange={e => setPersonMsg(e.target.value)}
              />
            </div>
            <button
              className="btn btn-primary btn-block"
              disabled={!recipientId || !canAffordIndividual || personSubmitting}
              type="submit"
            >
              {personSubmitting ? 'Sending…' : `📣 Send Shout-Out (${shoutOutValue ?? '…'} pts)`}
            </button>
          </form>
        </>
      )}

      {/* ── Team Shout-Out ── */}
      {tab === 'team' && (
        <>
          {teamAlert && <div className={`alert alert-${teamAlert.type}`} style={{ marginBottom: 16 }}>{teamAlert.text}</div>}

          {teams.length === 0
            ? <div className="empty"><div className="empty-icon">🏢</div><div className="empty-text">No teams exist yet.</div></div>
            : (
              <div className="card" style={{ marginBottom: 20 }}>
                <div className="section-title" style={{ marginBottom: 12 }}>Select a Team</div>
                <div className="award-grid">
                  {teams.map(t => (
                    <div
                      key={t.id}
                      className={'award-option' + (selectedTeam?.id === t.id ? ' selected' : '')}
                      onClick={() => setSelectedTeam(t)}
                    >
                      <div className="award-option-name">{t.name}</div>
                      <div className="award-option-pts">{t.memberCount} member{t.memberCount !== 1 ? 's' : ''}</div>
                      {userTeamIds.has(t.id) && (
                        <div className="award-option-desc" style={{ color: 'var(--amber)' }}>
                          You are a member — you won't receive points
                        </div>
                      )}
                      {t.description && <div className="award-option-desc">{t.description}</div>}
                    </div>
                  ))}
                </div>
              </div>
            )
          }

          {selectedTeam && (
            <form className="card" onSubmit={handleTeamSubmit}>
              <div style={{ marginBottom: 16, fontSize: 14, color: 'var(--muted)' }}>
                Each qualifying member of <strong>{selectedTeam.name}</strong> will receive{' '}
                <strong>{teamShoutOutValue} pts</strong>.
                {userTeamIds.has(selectedTeam.id) && ' Since you are a member, you will not receive points.'}
              </div>
              <div className="form-group">
                <label className="form-label">
                  Message <span style={{ color: 'var(--muted)', fontWeight: 400 }}>(optional)</span>
                </label>
                <textarea
                  className="form-textarea"
                  placeholder="Why are you recognizing this team?"
                  value={teamMsg}
                  onChange={e => setTeamMsg(e.target.value)}
                />
              </div>
              <button
                className="btn btn-primary btn-block"
                style={{ background: '#0891B2' }}
                disabled={!canAffordTeam || teamSubmitting}
                type="submit"
              >
                {teamSubmitting ? 'Sending…' : `📣 Shout-Out ${selectedTeam.name} (${teamShoutOutValue ?? '…'} pts/member)`}
              </button>
            </form>
          )}
        </>
      )}
    </div>
  )
}
