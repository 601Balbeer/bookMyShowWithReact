import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

function AppLayout() {
  const { customer, isAuthenticated, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label="BMS home">
          <span className="brand-mark" aria-hidden="true">B</span>
          <span>book<span className="brand-accent">my</span>show</span>
        </NavLink>
        <nav className="main-nav" aria-label="Main navigation">
          <NavLink to="/" end>Home</NavLink>
          <NavLink to="/browse">Browse shows</NavLink>
        </nav>
        <div className="account-nav">
          {isAuthenticated ? (
            <>
              <span className="customer-greeting">Hi, {customer.fullName?.split(' ')[0]}</span>
              <button className="sign-out-button" onClick={logout} type="button">Sign out</button>
            </>
          ) : (
            <>
              <NavLink className="header-sign-in" to="/login">Sign in</NavLink>
              <NavLink className="header-register" to="/register">Create account</NavLink>
            </>
          )}
        </div>
      </header>
      <main className="page-content"><Outlet /></main>
      <footer className="site-footer">
        <span>BMS · Built while learning React</span><span>Demo project</span>
      </footer>
    </div>
  )
}

export default AppLayout
