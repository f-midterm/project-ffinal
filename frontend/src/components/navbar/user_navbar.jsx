import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FiMenu, FiBell, FiUser } from "react-icons/fi";

function UserNavbar({ toggleSidebar }) {

  const navigate = useNavigate();

  return (
    <div className='fixed top-0 left-0 right-0 z-10'>
      <div className='w-full transition-colors duration-300 shadow-md'>
        <div className='flex justify-between py-4 lg:px-32 px-6 bg-white'>

          {/* Left Side */}
          <div className='flex lg:gap-12 gap-6 items-center'>
            <button 
              className='btn rounded-full p-3 hover:text-[#0076D4]'
              onClick={toggleSidebar}
            >
              <FiMenu size={24} />
            </button>
            <Link to="/profile" className='logo text-4xl font-semibold'>BeLiv</Link>
          </div>

          {/* Right Side */}
          <div className='flex lg:gap-12 gap-6 items-center'>
            <Link to='/user/:id/notifications' className='btn rounded-full p-3 hover:text-[#0076D4]'>
              <FiBell size={24} />
            </Link>
            <button className='btn rounded-full p-3 border-2 border-[#0076D4] hover:shadow-md hover:translate-y-[-0.5px]'>
              <FiUser size={24} className='text-[#0076D4]' />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default UserNavbar