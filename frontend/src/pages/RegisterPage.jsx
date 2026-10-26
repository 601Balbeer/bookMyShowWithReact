import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

function RegisterPage() {
  const [form, setForm] = useState({ fullName: '', email: '', contactNumber: '', dateOfBirth: '', password: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()
  const { registerCustomer } = useAuth()

  function updateField(event) {
    const { name, value } = event.target
    setForm((currentForm) => ({ ...currentForm, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      await registerCustomer(form)
      navigate('/login', { state: { registeredEmail: form.email } })
    } catch (registrationError) {
      setError(registrationError.message || 'Could not create your account. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="account-card register-card">
      <p className="eyebrow">JOIN THE MOVIE NIGHT</p>
      <h1>Create account</h1>
      <p className="account-copy">Create a customer account to continue to booking.</p>
      {error && <div className="form-error" role="alert">{error}</div>}
      <form className="account-form register-form" onSubmit={handleSubmit}>
        <label>Full name<input autoComplete="name" name="fullName" onChange={updateField} required value={form.fullName} /></label>
        <label>Email address<input autoComplete="email" name="email" onChange={updateField} required type="email" value={form.email} /></label>
        <label>Contact number<input autoComplete="tel" name="contactNumber" onChange={updateField} required type="tel" value={form.contactNumber} /></label>
        <label>Date of birth<input autoComplete="bday" name="dateOfBirth" onChange={updateField} required type="date" value={form.dateOfBirth} /></label>
        <label className="full-width-field">Password<input autoComplete="new-password" minLength="8" name="password" onChange={updateField} required type="password" value={form.password} /></label>
        <button className="primary-button account-submit full-width-field" disabled={isSubmitting} type="submit">
          {isSubmitting ? 'Creating account…' : 'Create account'} <span aria-hidden="true">→</span>
        </button>
      </form>
      <p className="account-switch">Already have an account? <Link to="/login">Sign in</Link></p>
    </section>
  )
}

export default RegisterPage
