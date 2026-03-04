import { useState, useEffect } from 'react'
import { getUsers, getAwardTypes, getPoints, giveAward, getTeams, getUserTeams, awardTeam, getConfig } from '../api'
import { useUser } from '../context/UserContext'

export default function GiveAward() {
  const { currentUser } = useUser()
  const [tab, setTab] = useState('person')

  // Shared
  const [pts, setPts] = useState(null)

  // Person award state
  const [users, setUsers]           = useState([])
  const [awardTypes, setAwardTypes] = useState([])
  const [recipientId,  setRecipientId]  = useState('')
  const [awardTypeId,  setAwardTypeId]  = useState(null)
  const [personMsg,    setPersonMsg]    = useState('')
  const [submitting,   setSubmitting]   = useState(false)
  const [personAlert,  setPersonAlert]  = useState(null)

  // Team award state
  const [teams, setTeams]                 = useState([])
  const [userTeamIds, setUserTeamIds]     = useState(new Set())
  const [allowSelfTeam, setAllowSelfTeam] = useState(false)
  const [selectedTeam,  setSelectedTeam]  = useState(null)
  const [teamPoints,    setTeamPoints]    = useState('')
  const [teamMsg,       setTeamMsg]       = useState('')
  const [teamSubmitting, setTeamSubmitting] = useState(false)
  const [teamAlert,     setTeamAlert]     = useState(null)

  useEffect(() => {
    getUsers().then(setUsers)
    getAwardTypes().then(setAwardTypes)
    getConfig().then(cfg => setAllowSelfTeam(cfg?.allowSelfTeamAward ?? false))
    getTeams().then(setTeams)
  }, [])

  useEffect(() => {
    if (!currentUser) return
    getPoints(currentUser.id).then(setPts)
    getUserTeams(currentUser.id).then(ts => setUserTeamIds(new Set(ts.map(t => t.id))))
  }, [currentUser])

  const selectedType = awardTypes.find(a => a.id === awardTypeId)
  const canSubmitPerson = recipientId && awardTypeId && !submitting &&
                          pts && (selectedType?.pointsCost ?? 0) <= pts.givingBalance

  const availableTeams = allowSelfTeam
    ? teams
    : teams.filter(t => !userTeamIds.has(t.id))

  const teamPointsNum  = Number(teamPoints)
  const canSubmitTeam  = selectedTeam && teamPointsNum > 0 && !teamSubmitting &&
                         pts && teamPointsNum <= pts.teamGivingBalance

  async function handlePersonSubmit(e) {
    e.preventDefault()
    setPersonAlert(null); setSubmitting(true)
    try {
      await giveAward({ giverId: currentUser.id, recipientId: Number(recipientId), awardTypeId, message: personMsg })
      setPersonAlert({ type: 'success', text: '🎉 Award given successfully!' })
      setRecipientId(''); setAwardTypeId(null); setPersonMsg('')
      getPoints(currentUser.id).then(setPts)
    } catch (err) {
      setPersonAlert({ type: 'error', text: err.message })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleTeamSubmit(e) {
    e.preventDefault()
    setTeamAlert(null); setTeamSubmitting(true)
    try {
      await awardTeam({ giverId: currentUser.id, teamId: selectedTeam.id, points: teamPointsNum, message: teamMsg })
      setTeamAlert({ type: 'success', text: `🏆 ${teamPointsNum} pts awarded to ${selectedTeam.name}!` })
      setSelectedTeam(null); setTeamPoints(''); setTeamMsg('')
      getPoints(currentUser.id).then(setPts)
    } catch (err) {
      setTeamAlert({ type: 'error', text: err.message })
    } finally {
      setTeamSubmitting(false)
    }
  }

  if (!currentUser) return <p style={{ color: 'var(--muted)' }}>Select a user to give an award.</p>

  const peers = users.filter(u => u.id !== currentUser.id)

  return (
    <div style={{ maxWidth: 680 }}>
      <div className="page-title">
        Give an Award
        <div className="page-subtitle">Recognize a colleague or a team from your quarterly giving banks</div>
      </div>

      <div className="tabs" style={{ marginBottom: 24 }}>
        <button className={'tab-btn' + (tab === 'person' ? ' active' : '')} onClick={() => setTab('person')}>👤 Award a Person</button>
        <button className={'tab-btn' + (tab === 'team'   ? ' active' : '')} onClick={() => setTab('team')}>🏢 Award a Team</button>
      </div>

      {/* ── Person Award ── */}
      {tab === 'person' && (
        <>
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

          {personAlert && <div className={`alert alert-${personAlert.type}`}>{personAlert.text}</div>}

          <form className="card" onSubmit={handlePersonSubmit}>
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
              <textarea className="form-textarea" placeholder="Share why you're recognizing them…" value={personMsg} onChange={e => setPersonMsg(e.target.value)} />
            </div>

            <button className="btn btn-primary btn-block" disabled={!canSubmitPerson} type="submit">
              {submitting ? 'Sending…' : '🎁 Give Award'}
            </button>
            {selectedType && pts && selectedType.pointsCost > pts.givingBalance && (
              <p style={{ color: 'var(--red)', fontSize: 13, marginTop: 8, textAlign: 'center' }}>
                Insufficient giving balance for this award type.
              </p>
            )}
          </form>
        </>
      )}

      {/* ── Team Award ── */}
      {tab === 'team' && (
        <>
          <div className="stats-row" style={{ marginBottom: 28 }}>
            <div className="stat-card" style={{ borderTop: '3px solid #0891B2' }}>
              <div className="stat-label" style={{ color: '#0891B2' }}>Team Giving Balance</div>
              <div className="stat-value">{pts?.teamGivingBalance ?? '—'}</div>
              <div className="stat-sub">of {pts?.teamGivingAllowance ?? 20} pts this quarter</div>
            </div>
            {selectedTeam && (
              <div className="stat-card indigo">
                <div className="stat-label">Awarding</div>
                <div className="stat-value" style={{ fontSize: 18 }}>{selectedTeam.name}</div>
                <div className="stat-sub">{selectedTeam.totalPoints} pts total · {selectedTeam.memberCount} members</div>
              </div>
            )}
          </div>

          {teamAlert && <div className={`alert alert-${teamAlert.type}`}>{teamAlert.text}</div>}

          {availableTeams.length === 0
            ? <div className="empty"><div className="empty-icon">🏢</div><div className="empty-text">No teams available to award{!allowSelfTeam ? ' (you cannot award your own team)' : ''}.</div></div>
            : (
              <div className="card" style={{ marginBottom: 20 }}>
                <div className="section-title" style={{ marginBottom: 12 }}>Select a Team</div>
                <div className="award-grid">
                  {availableTeams.map(t => (
                    <div
                      key={t.id}
                      className={'award-option' + (selectedTeam?.id === t.id ? ' selected' : '')}
                      onClick={() => setSelectedTeam(t)}
                    >
                      <div className="award-option-name">{t.name}</div>
                      <div className="award-option-pts">{t.totalPoints} pts earned</div>
                      {t.description && <div className="award-option-desc">{t.description}</div>}
                      <div className="award-option-desc">{t.memberCount} member{t.memberCount !== 1 ? 's' : ''}</div>
                    </div>
                  ))}
                </div>
              </div>
            )
          }

          {selectedTeam && (
            <form className="card" onSubmit={handleTeamSubmit}>
              <div className="form-group">
                <label className="form-label">
                  Points to Award <span style={{ color: 'var(--muted)', fontWeight: 400 }}>(1 – {pts?.teamGivingBalance ?? 0})</span>
                </label>
                <input
                  className="form-input"
                  type="number"
                  min="1"
                  max={pts?.teamGivingBalance ?? 0}
                  value={teamPoints}
                  onChange={e => setTeamPoints(e.target.value)}
                  required
                  placeholder="10"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Message <span style={{ color: 'var(--muted)', fontWeight: 400 }}>(optional)</span></label>
                <textarea className="form-textarea" placeholder="Why are you recognizing this team?" value={teamMsg} onChange={e => setTeamMsg(e.target.value)} />
              </div>

              <button className="btn btn-primary btn-block" style={{ background: '#0891B2' }} disabled={!canSubmitTeam} type="submit">
                {teamSubmitting ? 'Sending…' : `🏆 Award ${selectedTeam.name}`}
              </button>
              {teamPointsNum > (pts?.teamGivingBalance ?? 0) && (
                <p style={{ color: 'var(--red)', fontSize: 13, marginTop: 8, textAlign: 'center' }}>
                  Exceeds your team giving balance.
                </p>
              )}
            </form>
          )}
        </>
      )}
    </div>
  )
}
