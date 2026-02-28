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
