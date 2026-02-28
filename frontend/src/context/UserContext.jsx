import { createContext, useContext, useState, useEffect } from 'react'
import { getUsers } from '../api'

const UserContext = createContext(null)

export function UserProvider({ children }) {
  const [users, setUsers]           = useState([])
  const [currentUser, setCurrentUser] = useState(null)

  useEffect(() => {
    getUsers().then(data => {
      setUsers(data)
      if (data.length > 0) setCurrentUser(data[0])
    }).catch(() => {})
  }, [])

  const refresh = () => getUsers().then(data => {
    setUsers(data)
    if (!currentUser && data.length > 0) setCurrentUser(data[0])
  })

  return (
    <UserContext.Provider value={{ users, currentUser, setCurrentUser, refresh }}>
      {children}
    </UserContext.Provider>
  )
}

export const useUser = () => useContext(UserContext)
