import { createContext, useContext, useState } from 'react'
import { apiRequest } from '../api/client.js'

const AuthContext = createContext(null)

function readSavedCustomer() {
  try {
    const savedCustomer = localStorage.getItem('bms-customer')
    return savedCustomer ? JSON.parse(savedCustomer) : null
  } catch {
    localStorage.removeItem('bms-customer')
    return null
  }
}

export function AuthProvider({ children }) {
  const [customer, setCustomer] = useState(readSavedCustomer)

  async function registerCustomer(details) {
    return apiRequest('/users/user/register', {
      method: 'POST',
      body: JSON.stringify({ ...details, userType: 'CUSTOMER' }),
    })
  }

  async function loginCustomer(credentials) {
    const result = await apiRequest('/users/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    })
    if (!result.token || !result.customer) {
      throw new Error('These credentials do not belong to a customer account.')
    }

    localStorage.setItem('bms-token', result.token)
    localStorage.setItem('bms-customer', JSON.stringify(result.customer))
    setCustomer(result.customer)
    return result.customer
  }

  function logout() {
    localStorage.removeItem('bms-token')
    localStorage.removeItem('bms-customer')
    setCustomer(null)
  }

  return (
    <AuthContext.Provider value={{ customer, isAuthenticated: Boolean(customer), loginCustomer, logout, registerCustomer }}>
      {children}
    </AuthContext.Provider>
  )
}

// The context hook intentionally lives beside its provider for this small app foundation.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
