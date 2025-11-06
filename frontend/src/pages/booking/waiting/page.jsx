import React from 'react'
import { useNavigate } from 'react-router-dom';
import { FiCheck } from "react-icons/fi";

function WaitingPage() {
    const navigate = useNavigate();

    const handleNavigateToProfile = () => {
        navigate('/user/profile');
        onClose();
    };

    const handleNavigateToHome = () => {
        navigate('/');
        onClose();
    };

    return (
        <div className='flex flex-col items-center justify-center space-y-8 min-h-screen'>
            <div className='bg-green-300 rounded-full p-12'>
                <FiCheck size={64} className="text-white" />
            </div>
            
            {/* Content */}
            <div className='flex flex-col space-y-4 items-center text-xl font-medium'>
                <h2>Your submission has been send</h2>
                <h2>Please wait for approvement from owner</h2>
            </div>

            {/* Action Button */}
            <div className='flex gap-6'>
                <div onClick={handleNavigateToHome} className='border px-6 py-4 rounded-full border-blue-400 text-blue-400 cursor-pointer hover:translate-y-[-1px] hover:shadow-md hover:text-blue-600 hover:border-blue-600'>
                    Back to Home
                </div>
                <div onClick={handleNavigateToProfile} className='border px-6 py-4 rounded-full border-gray-500  text-gray-500 cursor-pointer hover:translate-y-[-1px] hover:shadow-md hover:text-black hover:border-black'>
                    Back to Profile
                </div>
            </div>
        </div>
    )
}

export default WaitingPage