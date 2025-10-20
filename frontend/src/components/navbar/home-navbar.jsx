import React, { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import NavItem from './nav-items'
function HomeNavbar() {
  const navLinks = [
    { to: '/application', label: 'Application' },
    { to: '/partner', label: 'Partner' },
    { to: '/service', label: 'Service' },
    { to: '/contact', label: 'Contact' },
  ]

  const [hidden, setHidden] = useState(false)
  const lastScroll = useRef(0)
  const ticking = useRef(false)
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const onScroll = () => {
      const current = window.scrollY || window.pageYOffset
      setScrolled(current > 10)
      if (!ticking.current) {
        window.requestAnimationFrame(() => {
          if (current > lastScroll.current && current > 80) {
            // scrolled down
            setHidden(true)
          } else {
            // scrolled up
            setHidden(false)
          }
          lastScroll.current = current
          ticking.current = false
        })
        ticking.current = true
      }
    }

    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <div className={`fixed top-0 left-0 right-0 z-50 transition-transform duration-300 ${hidden ? '-translate-y-full' : 'translate-y-0'}`}>
      <div className={`w-full transition-colors duration-300 shadow-md ${ scrolled ? "bg-white" : "bg-white/75" }`}>
        <div className='flex px-24 py-4 justify-center'>
          <div className='flex items-center gap-24 gap-32'>
            {/* Logo Section */}
            <Link to='/' className='text-4xl font-semibold logo'>
              BeLiv
            </Link>

            {/* Navigation Links */}
            <ul className='flex gap-6 items-center'>
              {navLinks.map((link) => (
                <li key={link.to}>
                  <NavItem to={link.to}>{link.label}</NavItem>
                </li>
              ))}
            </ul>

            {/* Action Button */}
            <div className='ml-auto flex gap-12'>
              <Link to='/signup' 
                className='btn-primary rounded-full text-white text-lg font-medium px-8 py-3 bg-gradient-to-r from-[#0076D4] to-[#303841] shadow-md hover:translate-y-[-2px] transition-transform'
              >
                Get Started
              </Link>
              <Link to='/login' 
                className={`btn-secondary rounded-full text-lg font-medium px-8 py-3 text-transparent bg-clip-text border-2 hover:translate-y-[-2px] transition-transform border-[#0076D4] bg-gradient-to-r from-[#0076D4] to-[#303841] `}
              >
                Login
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default HomeNavbar