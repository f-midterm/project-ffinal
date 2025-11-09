import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { PiBuilding } from "react-icons/pi";

function ProfilePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAdmin, loading } = useAuth();

  useEffect(() => {
    if (!loading && user) {
      if (!isAdmin && user.id.toString() !== id) {
        navigate(`/user/${user.id}`);
      }
    }
  }, [id, user, isAdmin, loading, navigate]);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className='flex flex-col lg:flex-row gap-8'>
      
      {/* Profile */}
      <div className='lg:w-1/3 w-full mb-8'>
        {/* Profile Picture */}
        <div className="w-80 h-80 bg-gray-200 rounded-xl mx-auto mb-4"></div>

        {/* Profile Detail */}
        <div className='lg:mb-12 mb-6'>
          <span className='text-md text-gray-400'>Profile</span><div className="border-t border-gray-300 pt-4"></div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">{user?.username}</h2>
          <div className="space-y-2">
            <ProfileDetail label="Fullname" value={user?.firstName + " " + user?.lastName || 'N/A'} />
            <ProfileDetail label="Email" value={user?.email || 'N/A'} isEmail={true} />
            <ProfileDetail label="Phone" value={user?.phone || 'N/A'} />
            <ProfileDetail label="Emergency Contact" value={user?.emergencyContact || 'N/A'} />
            <ProfileDetail label="Emergency Phone" value={user?.emergencyPhone || 'N/A'} />
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

function ProfileDetail({ label, value, isEmail = false }) {
  return (
    <div className="flex justify-between text-md mb-2">
      <span className="font-medium text-gray-600">{label}:</span>
      { isEmail ? (
        <a href={`mailto:${value}`} className="text-blue-600 hover:underline">{value}</a>
      ) : (
        <span className="text-gray-900">{value}</span>
      )}
    </div>
  );
}

export default ProfilePage;