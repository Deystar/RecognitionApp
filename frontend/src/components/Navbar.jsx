import { NavLink } from 'react-router-dom'
import { useUser } from '../context/UserContext'

export default function Navbar() {
  const { users, currentUser, setCurrentUser } = useUser()

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">🏆 Recog<span>App</span></div>

      <nav className="sidebar-nav">
        <NavLink to="/"        className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
          <span className="nav-icon">📣</span> Recognition Feed
        </NavLink>
        <NavLink to="/dashboard" className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
          <span className="nav-icon">📊</span> My Dashboard
        </NavLink>
        <NavLink to="/give"    className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
          <span className="nav-icon">🎁</span> Give Award
        </NavLink>
        <NavLink to="/store"   className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
          <span className="nav-icon">🛍️</span> Company Store
        </NavLink>
        <NavLink to="/admin"   className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
          <span className="nav-icon">⚙️</span> Admin
        </NavLink>
      </nav>

      <div className="sidebar-user">
        <label>Viewing as</label>
        <select
          value={currentUser?.id ?? ''}
          onChange={e => setCurrentUser(users.find(u => u.id === Number(e.target.value)))}
        >
          {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
        </select>
      </div>
    </aside>
  )
}
