import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'

const statusLabels = {
  NOT_BOOKED: 'Available',
  PENDING: 'On hold',
  BOOKED: 'Booked',
}

function formatSeatType(value) {
  return value ? value.toLowerCase().replaceAll('_', ' ') : 'Standard'
}

function formatMoney(amount) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount || 0)
}

function SeatAvailability({ show, theatre, onBack }) {
  const [seats, setSeats] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [retryCount, setRetryCount] = useState(0)
  const [selectedCounts, setSelectedCounts] = useState({})
  const [booking, setBooking] = useState(null)
  const [paymentResult, setPaymentResult] = useState(null)
  const [bookingError, setBookingError] = useState('')
  const [paymentError, setPaymentError] = useState('')
  const [isBooking, setIsBooking] = useState(false)
  const [isPaying, setIsPaying] = useState(false)
  const { isAuthenticated } = useAuth()

  useEffect(() => {
    const controller = new AbortController()
    setSeats([])
    setError('')
    setIsLoading(true)

    apiRequest(`/show/seat-status/${encodeURIComponent(show.sysId)}`, { signal: controller.signal })
      .then((data) => setSeats(Array.isArray(data) ? data : []))
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message || 'Could not load seat availability. Please try again.')
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsLoading(false)
      })

    return () => controller.abort()
  }, [retryCount, show.sysId])

  const rows = useMemo(() => {
    const rowMap = new Map()
    for (const seat of seats) {
      const [rowName = '?', seatIndex = '0'] = (seat.seatNumber || '').split('-')
      if (!rowMap.has(rowName)) rowMap.set(rowName, [])
      rowMap.get(rowName).push({ ...seat, rowName, seatIndex: Number(seatIndex) })
    }
    return [...rowMap.entries()]
      .sort(([rowA], [rowB]) => rowA.localeCompare(rowB))
      .map(([rowName, rowSeats]) => ({
        name: rowName,
        seatType: rowSeats[0]?.seatType,
        seats: rowSeats.sort((seatA, seatB) => seatA.seatIndex - seatB.seatIndex),
      }))
  }, [seats])

  const counts = useMemo(() => seats.reduce((result, seat) => {
    const status = seat.seatStatus || 'NOT_BOOKED'
    result[status] = (result[status] || 0) + 1
    return result
  }, {}), [seats])

  const availableByType = useMemo(() => seats.reduce((result, seat) => {
    if ((seat.seatStatus || 'NOT_BOOKED') === 'NOT_BOOKED') {
      const type = seat.seatType || 'REGULAR'
      result[type] = (result[type] || 0) + 1
    }
    return result
  }, {}), [seats])

  const selectedSeatCount = Object.values(selectedCounts).reduce((total, count) => total + count, 0)
  const selectedSeatTypes = Object.entries(selectedCounts).filter(([, count]) => count > 0)
  const theatreCity = theatre.location?.city

  function changeSeatCount(seatType, amount) {
    setBookingError('')
    setSelectedCounts((currentCounts) => {
      const nextCount = Math.max(0, Math.min(availableByType[seatType] || 0, (currentCounts[seatType] || 0) + amount))
      return { ...currentCounts, [seatType]: nextCount }
    })
  }

  async function createBooking() {
    setBookingError('')
    setIsBooking(true)
    try {
      const result = await apiRequest('/bookings', {
        method: 'POST',
        body: JSON.stringify({
          showSysId: show.sysId,
          seatCounts: selectedSeatTypes.map(([seatType, count]) => ({ seatType, count })),
        }),
      })
      setBooking(result)
      setPaymentResult(null)
      setSelectedCounts({})
      const reservedSeatIds = new Set((result.seats || []).map((seat) => seat.seatId))
      setSeats((currentSeats) => currentSeats.map((seat) => reservedSeatIds.has(seat.seatNumber)
        ? { ...seat, seatStatus: 'PENDING' }
        : seat))
    } catch (bookingRequestError) {
      setBookingError(bookingRequestError.message || 'Could not reserve these seats. Please try again.')
      setRetryCount((count) => count + 1)
    } finally {
      setIsBooking(false)
    }
  }

  async function payForBooking() {
    if (!booking) return
    setPaymentError('')
    setIsPaying(true)
    try {
      const result = await apiRequest(`/payments/booking/${booking.bookingSysId}/pay`, {
        method: 'POST',
        body: JSON.stringify({ amount: booking.amount }),
      })
      setPaymentResult(result)
      if (result.paymentStatus === 'SUCCESS') {
        setBooking((currentBooking) => ({ ...currentBooking, status: 'CONFIRMED' }))
        const confirmedSeatIds = new Set((booking.seats || []).map((seat) => seat.seatId))
        setSeats((currentSeats) => currentSeats.map((seat) => confirmedSeatIds.has(seat.seatNumber)
          ? { ...seat, seatStatus: 'BOOKED' }
          : seat))
      } else {
        setPaymentError('The demo payment was not confirmed. You can try again.')
      }
    } catch (paymentRequestError) {
      setPaymentError(paymentRequestError.message || 'Could not complete the demo payment. Please try again.')
    } finally {
      setIsPaying(false)
    }
  }

  const address = [theatre.location?.addressLine1, theatre.location?.addressLine2, theatre.location?.addressLine3, theatreCity]
    .filter(Boolean)
    .join(', ')

  return (
    <div className="seat-page">
      <button className="back-button" onClick={onBack} type="button"><span aria-hidden="true">←</span> Back to shows</button>
      <div className="seat-heading">
        <div>
          <p className="eyebrow">{theatre.theaterName}{theatreCity ? ` · ${theatreCity}` : ''}</p>
          <h1>{show.showName}</h1>
          <p className="muted-copy">{show.hall?.hallName || 'Screen'}{address ? ` · ${address}` : ''}</p>
        </div>
        <div className="show-count-badge">{show.showStartTime ? new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' }).format(new Date(show.showStartTime)) : 'Show time TBA'}</div>
      </div>

      {!isLoading && !error && seats.length > 0 && (
        <div className="seat-summary" aria-label="Seat availability summary">
          <div><strong>{counts.NOT_BOOKED || 0}</strong><span>Available</span></div>
          <div><strong>{counts.PENDING || 0}</strong><span>On hold</span></div>
          <div><strong>{counts.BOOKED || 0}</strong><span>Booked</span></div>
        </div>
      )}

      {isLoading ? (
        <div className="no-results" role="status"><span className="loading-spinner" /><strong>Loading seat availability…</strong></div>
      ) : error ? (
        <div className="no-results error-state" role="alert">
          <strong>Couldn’t load seat availability</strong><span>{error}</span>
          <button className="text-button" onClick={() => { setError(''); setRetryCount((count) => count + 1) }} type="button">Try again</button>
        </div>
      ) : seats.length > 0 ? (
        <>
          <div className="seat-map-card">
            <div className="screen-label"><span /> SCREEN <span /></div>
            <div className="seat-rows">
              {rows.map((row) => (
                <div className="seat-row" key={row.name}>
                  <span className="row-label">{row.name}</span>
                  <div className="seat-row-items">
                    {row.seats.map((seat) => {
                      const status = seat.seatStatus || 'NOT_BOOKED'
                      const statusName = statusLabels[status] || status
                      return (
                        <span
                          role="img"
                          className={`seat-tile seat-${status.toLowerCase()}`}
                          key={seat.seatNumber}
                          title={`${seat.seatNumber} · ${formatSeatType(seat.seatType)} · ${statusName}`}
                          aria-label={`${seat.seatNumber}, ${formatSeatType(seat.seatType)}, ${statusName}`}
                        >
                          {seat.seatIndex}
                        </span>
                      )
                    })}
                  </div>
                  <span className="row-label">{row.name}</span>
                  <span className="row-type">{formatSeatType(row.seatType)}</span>
                </div>
              ))}
            </div>
            <div className="seat-legend" aria-label="Seat status legend">
              <span><i className="seat-legend-dot available" /> Available</span>
              <span><i className="seat-legend-dot pending" /> On hold</span>
              <span><i className="seat-legend-dot booked" /> Booked</span>
            </div>
          </div>

          {!booking && (
            <section className="booking-panel">
              <div className="booking-panel-heading">
                <div><p className="eyebrow">RESERVE YOUR SEATS</p><h2>Choose how many</h2></div>
                {selectedSeatCount > 0 && <span>{selectedSeatCount} {selectedSeatCount === 1 ? 'seat' : 'seats'}</span>}
              </div>
              <p className="booking-help">Choose a seat count for each type. The backend allocates the next available seats in that category and returns the exact seat numbers.</p>
              <div className="seat-type-options">
                {Object.entries(availableByType).map(([seatType, availableCount]) => (
                  <div className="seat-type-option" key={seatType}>
                    <div><strong>{formatSeatType(seatType)}</strong><span>{availableCount} available</span></div>
                    <div className="quantity-control" aria-label={`${formatSeatType(seatType)} seat count`}>
                      <button onClick={() => changeSeatCount(seatType, -1)} disabled={isBooking || !selectedCounts[seatType]} type="button" aria-label={`Remove one ${formatSeatType(seatType)} seat`}>−</button>
                      <span>{selectedCounts[seatType] || 0}</span>
                      <button onClick={() => changeSeatCount(seatType, 1)} disabled={isBooking || (selectedCounts[seatType] || 0) >= availableCount} type="button" aria-label={`Add one ${formatSeatType(seatType)} seat`}>+</button>
                    </div>
                  </div>
                ))}
              </div>
              {bookingError && <div className="form-error" role="alert">{bookingError}</div>}
              {isAuthenticated ? (
                <button className="primary-button booking-action" disabled={selectedSeatCount === 0 || isBooking || isLoading} onClick={createBooking} type="button">
                  {isBooking ? 'Reserving…' : 'Reserve seats'} <span aria-hidden="true">→</span>
                </button>
              ) : (
                <p className="signin-required">Please <Link to="/login">sign in</Link> to reserve seats.</p>
              )}
            </section>
          )}

          {booking && (
            <section className="booking-panel booking-summary-panel" aria-live="polite">
              {paymentResult?.paymentStatus === 'SUCCESS' ? (
                <>
                  <div className="booking-success-icon" aria-hidden="true">✓</div>
                  <p className="eyebrow">BOOKING CONFIRMED</p>
                  <h2>You’re all set</h2>
                  <p className="booking-help">Your demo payment succeeded. No money was transferred.</p>
                </>
              ) : (
                <>
                  <p className="eyebrow">BOOKING {booking.status || 'PENDING'}</p>
                  <h2>Review your booking</h2>
                  <p className="booking-help">Your seats are held while this demo payment is pending.</p>
                </>
              )}
              <div className="allocated-seats">
                {(booking.seats || []).map((seat) => (
                  <span key={seat.seatId}>{seat.seatId}<small>{formatSeatType(seat.seatType)}</small></span>
                ))}
              </div>
              <div className="booking-total"><span>Total</span><strong>{formatMoney(booking.amount)}</strong></div>
              {paymentResult?.transactionId && <p className="transaction-reference">Demo reference · {paymentResult.transactionId}</p>}
              {paymentError && <div className="form-error" role="alert">{paymentError}</div>}
              {paymentResult?.paymentStatus !== 'SUCCESS' && (
                <button className="primary-button booking-action" disabled={isPaying} onClick={payForBooking} type="button">
                  {isPaying ? 'Processing demo payment…' : `Pay ${formatMoney(booking.amount)} (demo)`}
                  <span aria-hidden="true">→</span>
                </button>
              )}
              {paymentResult?.paymentStatus === 'SUCCESS' && (
                <button className="secondary-action" onClick={onBack} type="button">Back to shows</button>
              )}
            </section>
          )}
        </>
      ) : (
        <div className="no-results">
          <span className="empty-icon" aria-hidden="true">⌕</span>
          <strong>No seat layout available</strong>
          <span>This show’s hall does not have a seat layout configured yet.</span>
        </div>
      )}
    </div>
  )
}

export default SeatAvailability
