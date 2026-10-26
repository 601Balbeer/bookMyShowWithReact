import { useEffect, useState } from 'react'
import { apiRequest } from '../api/client.js'
import SeatAvailability from './SeatAvailability.jsx'

function formatShowTime(value) {
  if (!value) return 'Time not available'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('en-IN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
  }).format(date)
}

function TheatreShows({ theatre, onBack }) {
  const [shows, setShows] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [retryCount, setRetryCount] = useState(0)
  const [selectedShow, setSelectedShow] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    setShows([])
    setError('')
    setIsLoading(true)

    apiRequest(`/show/get/${encodeURIComponent(theatre.sysId)}`, { signal: controller.signal })
      .then((data) => setShows(Array.isArray(data) ? data : []))
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message || 'Could not load shows. Please try again.')
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsLoading(false)
      })

    return () => controller.abort()
  }, [retryCount, theatre.sysId])

  function retry() {
    setError('')
    setRetryCount((count) => count + 1)
  }

  const location = theatre.location || {}
  const address = [location.addressLine1, location.addressLine2, location.addressLine3, location.city]
    .filter(Boolean)
    .join(', ')

  if (selectedShow) {
    return <SeatAvailability show={selectedShow} theatre={theatre} onBack={() => setSelectedShow(null)} />
  }

  return (
    <div className="shows-page">
      <button className="back-button" onClick={onBack} type="button"><span aria-hidden="true">←</span> All theatres</button>
      <div className="shows-heading">
        <div>
          <p className="eyebrow">THEATRE · {location.city || 'LOCATION'}</p>
          <h1>{theatre.theaterName || 'Theatre'}</h1>
          {address && <p className="muted-copy">{address}</p>}
        </div>
        <div className="show-count-badge">{shows.length} {shows.length === 1 ? 'show' : 'shows'}</div>
      </div>

      <div className="results-heading"><h2>Scheduled shows</h2></div>

      {isLoading ? (
        <div className="no-results" role="status"><span className="loading-spinner" /><strong>Loading shows…</strong></div>
      ) : error ? (
        <div className="no-results error-state" role="alert">
          <strong>Couldn’t load shows</strong><span>{error}</span>
          <button className="text-button" onClick={retry} type="button">Try again</button>
        </div>
      ) : shows.length > 0 ? (
        <div className="shows-list">
          {shows.map((show) => (
            <article className="show-card" key={show.sysId || show.id}>
              <div className="show-date-mark" aria-hidden="true">▶</div>
              <div className="show-details">
                <h3>{show.showName || 'Untitled show'}</h3>
                {show.shortDescription && <p className="show-description">{show.shortDescription}</p>}
                <div className="show-meta">
                  <span><span aria-hidden="true">◷</span> {formatShowTime(show.showStartTime)}</span>
                  {show.hall?.hallName && <span><span aria-hidden="true">▦</span> {show.hall.hallName}</span>}
                </div>
                {show.artists && <p className="show-artists">Featuring {show.artists}</p>}
                <button className="view-seats-button" onClick={() => setSelectedShow(show)} type="button">
                  View seats <span aria-hidden="true">→</span>
                </button>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <div className="no-results">
          <span className="empty-icon" aria-hidden="true">▶</span>
          <strong>No shows scheduled yet</strong>
          <span>There are currently no shows listed for this theatre.</span>
        </div>
      )}
    </div>
  )
}

export default TheatreShows
