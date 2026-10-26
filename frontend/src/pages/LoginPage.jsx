import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

function LoginPage() {
  const location = useLocation()
  const registeredEmail = location.state?.registeredEmail
  const [email, setEmail] = useState(registeredEmail || '')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()
  const { loginCustomer } = useAuth()

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      await loginCustomer({ email, password })
      navigate('/browse')
    } catch (loginError) {
      setError(loginError.message || 'Could not sign in. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="account-card">
      <p className="eyebrow">WELCOME BACK</p>
      <h1>Sign in</h1>
      <p className="account-copy">Sign in with your customer account to continue.</p>
      {registeredEmail && <div className="success-message" role="status">Account created. You can now sign in as {registeredEmail}.</div>}
      {error && <div className="form-error" role="alert">{error}</div>}
      <form className="account-form" onSubmit={handleSubmit}>
        <label>Email address<input autoComplete="email" onChange={(event) => setEmail(event.target.value)} required type="email" value={email} /></label>
        <label>Password<input autoComplete="current-password" onChange={(event) => setPassword(event.target.value)} required type="password" value={password} /></label>
        <button className="primary-button account-submit" disabled={isSubmitting} type="submit">
          {isSubmitting ? 'Signing in…' : 'Sign in'} <span aria-hidden="true">→</span>
        </button>
      </form>
      <p className="account-switch">New to BMS? <Link to="/register">Create a customer account</Link></p>
    </section>
  )
}

export default LoginPage
