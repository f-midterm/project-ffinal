import React from 'react'
import { PiBuilding } from "react-icons/pi";
import ProfileDetail from '../../../components/form/profile_detail';

function ProfilePage() {

  return (
    <div className='flex flex-col lg:flex-row gap-8'>
      
      {/* Profile */}
      <div className='lg:w-1/3 w-full mb-8'>
        {/* Profile Picture */}
        <div className="w-80 h-80 bg-gray-200 rounded-xl mx-auto mb-4"></div>

        {/* Profile Detail */}
        <div className='lg:mb-12 mb-6'>
          <span className='text-md text-gray-400'>Profile</span><div className="border-t border-gray-300 pt-4"></div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Username</h2>
          <div className="space-y-2">
            <ProfileDetail label="Fullname" value={"Fullname"} />
            <ProfileDetail label="Email" value={"who@company.com"} isEmail={true} />
            <ProfileDetail label="Phone" value={"123-123-1234"} />
            <ProfileDetail label="Emergency" value={"123-123-1234"} />
          </div>
        </div>

        <button className='w-full bg-gray-800 text-white py-4 rounded-xl shadow-md hover:translate-y-[-1px]'>
          Edit Profile
        </button>
        
      </div>

      {/* Lease Detail */}
      <div className='flex lg:flex-1 border border-gray-400 rounded-2xl justify-center items-center'>
        <div className='text-center lg:py-0 py-32'>
          <div className="flex justify-center mb-4">
            <div className="w-24 h-24 bg-gray-200 rounded-full flex items-center justify-center"><PiBuilding size={32} className='text-gray-500' /></div>
          </div>
          <h2 className="text-2xl font-semibold text-gray-700">
            You haven't any application
          </h2>
        </div>
      </div>
    </div>
  )
}

export default ProfilePage