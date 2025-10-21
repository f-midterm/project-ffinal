import React from 'react'
import Hero from '../../components/hero/hero'
import HomeNavbar from '../../components/navbar/home-navbar'   // 
function HomePage() {
  return (
    <div>
      <HomeNavbar />

      <div className="pt-24">
        <Hero />

        <div id='explore' className='py-24'>
          <div className='max-w-4xl mx-auto text-center'>
            <div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default HomePage
