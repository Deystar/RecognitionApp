import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { UserProvider } from './context/UserContext'
import Navbar from './components/Navbar'
import Feed from './pages/Feed'
import Dashboard from './pages/Dashboard'
import GiveAward from './pages/GiveAward'
import Store from './pages/Store'
import Admin from './pages/Admin'

export default function App() {
  return (
    <UserProvider>
      <BrowserRouter>
        <div className="layout">
          <Navbar />
          <main className="main">
            <Routes>
              <Route path="/"         element={<Feed />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/give"     element={<GiveAward />} />
              <Route path="/store"    element={<Store />} />
              <Route path="/admin"    element={<Admin />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </UserProvider>
  )
}
