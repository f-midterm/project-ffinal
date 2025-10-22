import React from 'react'
import { Outlet } from 'react-router-dom'
import HomeNavbar from '../components/navbar/home_navbar'
import Footer from '../components/footer/footer'

function Layout() {
  return (
    <div className='min-h-screen flex flex-col'>
      <header>
        <HomeNavbar />
      </header>

      <main className='flex-1 w-full mx-auto'>
        <Outlet />
      </main>

      <Footer />
    </div>
  )
}

export default Layout