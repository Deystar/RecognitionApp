const BASE = '/api'

async function req(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || res.statusText)
  }
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

// Users
export const getUsers       = ()           => req('/users')
export const createUser     = (name, email) => req('/users', { method: 'POST', body: JSON.stringify({ name, email }) })

// Award Types
export const getAwardTypes  = ()            => req('/award-types')
export const createAwardType = (data)       => req('/award-types', { method: 'POST', body: JSON.stringify(data) })

// Awards / Feed
export const getAwardFeed   = (limit = 50)  => req(`/awards/feed?limit=${limit}`)
export const getAwardsReceived = (userId)   => req(`/awards/received/${userId}`)
export const getAwardsGiven    = (userId)   => req(`/awards/given/${userId}`)
export const giveAward      = (data)        => req('/awards', { method: 'POST', body: JSON.stringify(data) })

// Points
export const getPoints      = (userId)      => req(`/points/${userId}`)

// Store
export const getStoreItems  = ()            => req('/store/items')
export const createStoreItem = (data)       => req('/store/items', { method: 'POST', body: JSON.stringify(data) })
export const purchaseItem   = (userId, storeItemId) => req('/store/purchase', { method: 'POST', body: JSON.stringify({ userId, storeItemId }) })
export const getPurchases   = (userId)      => req(`/store/purchases/${userId}`)

// Config
export const getConfig           = ()           => req('/config')
export const setShoutOutValue     = (value)     => req('/config/shout-out-value', { method: 'PUT', body: JSON.stringify({ value }) })
export const setTeamShoutOutValue  = (value)        => req('/config/team-shout-out-value', { method: 'PUT', body: JSON.stringify({ value }) })
export const setShoutOutAllowance  = (value)        => req('/config/shout-out-allowance', { method: 'PUT', body: JSON.stringify({ value }) })
export const setResetInterval      = (quantity, unit) => req('/config/reset-interval', { method: 'PUT', body: JSON.stringify({ quantity, unit }) })

// Shout-outs
export const giveShoutOut         = (data)      => req('/shout-outs', { method: 'POST', body: JSON.stringify(data) })
export const getShoutOutFeed      = (limit = 50) => req(`/shout-outs/feed?limit=${limit}`)
export const getShoutOutsReceived = (userId)    => req(`/shout-outs/received/${userId}`)
export const getShoutOutsGiven    = (userId)    => req(`/shout-outs/given/${userId}`)

// Teams
export const getTeams            = ()                   => req('/teams')
export const getTeamMemberships  = ()                   => req('/teams/memberships')
export const createTeam          = (data)               => req('/teams', { method: 'POST', body: JSON.stringify(data) })
export const deleteTeam       = (id)                 => req(`/teams/${id}`, { method: 'DELETE' })
export const getTeam          = (id)                 => req(`/teams/${id}`)
export const addTeamMember    = (teamId, userId)     => req(`/teams/${teamId}/members`, { method: 'POST', body: JSON.stringify({ userId }) })
export const removeTeamMember = (teamId, userId)     => req(`/teams/${teamId}/members/${userId}`, { method: 'DELETE' })
export const getUserTeams     = (userId)             => req(`/teams/user/${userId}`)
export const awardTeam        = (data)               => req('/teams/award', { method: 'POST', body: JSON.stringify(data) })
export const getTeamAwards    = (teamId)             => req(`/teams/${teamId}/awards`)
