import React from 'react'
import { MdPendingActions } from "react-icons/md";
import { PiHammer } from "react-icons/pi";
import { GrDocumentText } from "react-icons/gr";
import { Link } from 'react-router-dom'

import StatCard from '../../../components/card/stat-card';
import UnitCard from '../../../components/card/unit-card';

function AdminDashboard() {
  return (
    <div className='flex flex-col'>
      {/* Title */}
        <div className='lg:mb-12 mb-8'>
          <h1 className='title mb-4 lg:mb-6'>
            Dashboard
          </h1>
          <p className='text-xl lg:text-2xl mb-4 lg:mb-6'>Welcome, Siri Bunthavorn</p>
          <p className='text-lg lg:text-xl text-gray-600'>Apartment : Siri Apartment, Floor : 2, Rooms : 24</p>
        </div>
        
      {/* Unit Section */}
        
        {/* Stat Card */}
        <div className='grid gap-6 grid-cols-1 lg:grid-cols-3 justify-center items-center mb-12'>
          {/* Rental Requests */}
          <Link to="/admin/rental-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
            <StatCard icon={<MdPendingActions />} title={"Rental Requests"} value={`2 Requests`} color={"green"} />
          </Link>

          {/* Maintenance Requests */}
          <Link to="maintenance-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
            <StatCard icon={<PiHammer />} title={"Maintenane Requests"} value={`2 Requests`} color={"yellow"} />
          </Link>
          
          {/* Lease Renewals */}
          <Link to="lease-renewals" className='hover:translate-y-[-1px] hover:shadow-lg'>
            <StatCard icon={<GrDocumentText />} title={"Lease Renewals"} value={`3 Upcoming`} color={"red"} />
          </Link>
        </div>

        {/* 1st Floor */}
        <div className='bg-white rounded-lg p-8 shadow-md mb-6 lg:mb-8'>
          <div className="lg:text-xl text-lg font-medium mb-2">1st Floor</div>

          {/* Status */}
          <div className='text-sm mb-4'>
            <span className='text-green-600'>7 vacants</span>, <span className='text-red-600'>5 occupied</span>
          </div>

          {/* Units */}
          <div className='grid grid-cols-2 lg:grid-cols-6 sm:grid-cols-3 md:grid-cols-4 gap-4'>
            <UnitCard />
          </div>
        </div>

        {/* 2nd Floor */}
        <div className='bg-white rounded-lg p-8 shadow-md'>
          <div className="lg:text-xl text-lg font-medium mb-2">2nd Floor</div>

          {/* Status */}
          <div className='text-sm mb-4'>
            <span className='text-green-600'>7 vacants</span>, <span className='text-red-600'>5 occupied</span>
          </div>

          {/* Units */}
          <div className='grid grid-cols-2 lg:grid-cols-6 sm:grid-cols-3 md:grid-cols-4 gap-4'>
            <UnitCard />
          </div>
        </div>
    </div>
  )
}

export default AdminDashboard