import { Link } from 'react-router-dom'

function HomePage() {
  return (
    <section className="welcome-card">
      <p className="eyebrow">YOUR NEXT MOVIE NIGHT</p>
      <h1>Find a show.<br /><span>Make it a night.</span></h1>
      <p className="welcome-copy">
        Browse theatres and shows, check seats, and try the booking flow.
        We’ll build each part together as you learn React.
      </p>
      <Link className="primary-button" to="/browse">Explore shows <span aria-hidden="true">→</span></Link>
      <div className="learning-note"><span className="status-dot" /><span>Step 1 · Frontend foundation is ready</span></div>
    </section>
  )
}

export default HomePage
