import React, { useEffect, useRef, useState, useCallback, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import NavItem from './nav_items'
import { logout, getUsername, isAuthenticated } from '../../api';
import { FiUser } from "react-icons/fi";

function HomeNavbar() {
  const navLinks = useMemo(() => [
    { to: '/application', label: 'Application' },
    { to: '/partner', label: 'Partner' },
    { to: '/service', label: 'Service' },
    { to: '/contact', label: 'Contact' },
  ], []);

  const [hidden, setHidden] = useState(false)
  const lastScroll = useRef(0)
  const ticking = useRef(false)
  const [scrolled, setScrolled] = useState(false)
  const navigate = useNavigate();
  
  const authenticated = isAuthenticated();
  const username = getUsername();

  useEffect(() => {
    const onScroll = () => {
      const current = window.scrollY || window.pageYOffset
      setScrolled(current > 10)
      if (!ticking.current) {
        window.requestAnimationFrame(() => {
          if (current > lastScroll.current && current > 80) {
            setHidden(true)
          } else {
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

  const handleLogout = useCallback(() => {
    logout();
    navigate('/');
  }, [navigate]);
  

  return (
    <div className={`fixed top-0 left-0 right-0 z-50 transition-transform duration-300 ${hidden ? '-translate-y-full' : 'translate-y-0'}`}>
      <div className={`w-full transition-colors duration-300 shadow-md ${ scrolled ? "bg-white" : "bg-white/75" }`}>
        {/* ✅ ใช้ flex-wrap และ responsive padding */}
        <div className='flex flex-wrap justify-between items-center px-6 md:px-16 lg:px-24 py-4'>
          
          {/* Logo Section */}
          <Link to='/' className='text-3xl md:text-4xl font-semibold logo mb-2 md:mb-0'>
            BeLiv
          </Link>

          {/* Navigation Links */}
          <ul className='flex flex-wrap justify-center gap-4 md:gap-6 items-center text-sm md:text-base w-full md:w-auto order-last md:order-none mt-2 md:mt-0'>
            {navLinks.map((link) => (
              <li key={link.to}>
                <NavItem to={link.to}>{link.label}</NavItem>
              </li>
            ))}
          </ul>

          {/* Action Button */}
          <div className='flex gap-4 md:gap-12 items-center w-full md:w-auto justify-center md:justify-end mt-3 md:mt-0'>
            { authenticated ? (
              <>
                <button className='btn rounded-full p-2 md:p-3 border-2 border-[#0076D4] hover:shadow-md hover:translate-y-[-0.5px]'>
                  <FiUser size={22} className='text-[#0076D4]' />
                </button>
                <span className='text-sm md:text-base'>{username}</span>
              </>
            ) : (
              <>
                <Link to='/signup' 
                  className='btn-primary rounded-full text-white text-sm md:text-lg font-medium px-6 md:px-8 py-2 md:py-3 bg-gradient-to-r from-[#0076D4] to-[#303841] shadow-md hover:translate-y-[-2px] transition-transform'
                >
                  Get Started
                </Link>
                <Link to='/login' 
                  className='btn-secondary rounded-full text-sm md:text-lg font-medium px-6 md:px-8 py-2 md:py-3 text-transparent bg-clip-text border-2 hover:translate-y-[-2px] transition-transform border-[#0076D4] bg-gradient-to-r from-[#0076D4] to-[#303841]'
                >
                  Login
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default HomeNavbar
