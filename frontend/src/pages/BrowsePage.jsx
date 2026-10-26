import { useEffect, useMemo, useState } from 'react'
import { apiRequest } from '../api/client.js'
import TheatreShows from './TheatreShows.jsx'

const popularCities = ['Mumbai', 'Pune', 'Bengaluru', 'Delhi']

function getAddress(location = {}) {
  return [location.addressLine1, location.addressLine2, location.addressLine3, location.city]
    .filter(Boolean)
    .join(', ')
}

function BrowsePage() {
  const [searchText, setSearchText] = useState('')
  const [searchedCity, setSearchedCity] = useState('')
  const [theatres, setTheatres] = useState([])
  const [selectedTheatre, setSelectedTheatre] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!searchedCity) {
      setTheatres([])
      setError('')
      setIsLoading(false)
      return undefined
    }

    const controller = new AbortController()
    setIsLoading(true)
    setError('')

    apiRequest(`/theatre/get?city=${encodeURIComponent(searchedCity)}`, { signal: controller.signal })
      .then((data) => setTheatres(Array.isArray(data) ? data : []))
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message || 'Could not load theatres. Please try again.')
          setTheatres([])
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsLoading(false)
      })

    return () => controller.abort()
  }, [searchedCity])

  const filteredTheatres = useMemo(() => {
    const query = searchText.trim().toLowerCase()
    if (!query || query.toLowerCase() === searchedCity.toLowerCase()) return theatres

    return theatres.filter((theatre) => {
      const location = theatre.location || {}
      return `${theatre.theaterName || ''} ${location.city || ''} ${getAddress(location)}`
        .toLowerCase()
        .includes(query)
    })
  }, [searchText, searchedCity, theatres])

  if (selectedTheatre) {
    return (
      <section className="browse-page">
        <TheatreShows theatre={selectedTheatre} onBack={() => setSelectedTheatre(null)} />
      </section>
    )
  }

  function searchCity(city = searchText) {
    const normalizedCity = city.trim()
    if (!normalizedCity) return
    setSearchText(normalizedCity)
    setSearchedCity(normalizedCity)
  }

  function clearSearch() {
    setSearchText('')
    setSearchedCity('')
    setTheatres([])
    setError('')
  }

  return (
    <section className="browse-page">
      <div className="browse-heading">
        <div>
          <p className="eyebrow">DISCOVER NEAR YOU</p>
          <h1>Find a theatre</h1>
          <p className="muted-copy">Search by city to find theatres and their locations.</p>
        </div>
        <span className="sample-badge live-badge">Live backend data</span>
      </div>

      <form className="search-field" onSubmit={(event) => { event.preventDefault(); searchCity() }}>
        <span className="search-icon" aria-hidden="true">⌕</span>
        <label className="visually-hidden" htmlFor="city-search">Search theatres by city</label>
        <input
          id="city-search"
          type="search"
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          placeholder="Enter a city, e.g. Mumbai"
        />
        {searchText && <button className="clear-search" onClick={clearSearch} type="button">Clear</button>}
        <button className="search-button" disabled={!searchText.trim() || isLoading} type="submit">
          {isLoading ? 'Searching…' : 'Search'}
        </button>
      </form>

      <div className="city-filters" aria-label="Popular cities">
        <span className="city-filter-label">Popular cities</span>
        {popularCities.map((city) => (
          <button
            className={`city-chip${searchedCity.toLowerCase() === city.toLowerCase() ? ' selected' : ''}`}
            key={city}
            onClick={() => searchCity(city)}
            type="button"
            aria-pressed={searchedCity.toLowerCase() === city.toLowerCase()}
          >
            {city}
          </button>
        ))}
      </div>

      {searchedCity && (
        <div className="results-heading">
          <h2>Theatres in {searchedCity}</h2>
          {!isLoading && !error && <span>{filteredTheatres.length} {filteredTheatres.length === 1 ? 'place' : 'places'}</span>}
        </div>
      )}

      {isLoading ? (
        <div className="no-results" role="status"><span className="loading-spinner" /><strong>Finding theatres in {searchedCity}…</strong></div>
      ) : error ? (
        <div className="no-results error-state" role="alert">
          <strong>Couldn’t load theatres</strong>
          <span>{error}</span>
          <button className="text-button" onClick={() => searchCity(searchedCity)} type="button">Try again</button>
        </div>
      ) : filteredTheatres.length > 0 ? (
        <div className="theatre-grid">
          {filteredTheatres.map((theatre) => {
            const location = theatre.location || {}
            const name = theatre.theaterName || 'Theatre'
            return (
              <article className="theatre-card" key={theatre.sysId || theatre.id}>
                <div className="theatre-art" aria-hidden="true">
                  <span className="theatre-art-mark">{name.charAt(0).toUpperCase()}</span>
                  <span className="theatre-art-caption">NOW SHOWING</span>
                </div>
                <div className="theatre-info">
                  <span className="theatre-city">{location.city || searchedCity}</span>
                  <h3>{name}</h3>
                  <p><span aria-hidden="true">⌖</span> {getAddress(location) || 'Location details unavailable'}</p>
                <button className="theatre-select-button" onClick={() => setSelectedTheatre(theatre)} type="button">
                  View shows <span aria-hidden="true">→</span>
                </button>
                </div>
              </article>
            )
          })}
        </div>
      ) : searchedCity ? (
        <div className="no-results">
          <span className="empty-icon" aria-hidden="true">⌕</span>
          <strong>No theatres found in {searchedCity}</strong>
          <span>Try another city or choose one of the popular cities.</span>
          <button className="text-button" onClick={clearSearch} type="button">Clear search</button>
        </div>
      ) : (
        <div className="no-results browse-prompt">
          <span className="empty-icon" aria-hidden="true">⌕</span>
          <strong>Where do you want to watch?</strong>
          <span>Enter a city or choose a popular city to load theatres.</span>
        </div>
      )}
    </section>
  )
}

export default BrowsePage
