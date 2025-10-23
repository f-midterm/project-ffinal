import React, { useState, useCallback, useMemo } from 'react'
import { useNavigate, Link } from 'react-router-dom';

function CreateProfileForm() {
    
    const [formData, setFormData] = useState({
        firstname: '',
        lastname: '',
        phoneNumber: '',
        emergencyNumber: '',
    });

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));
    }, []);

    const handleSubmit = useCallback(async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await createProfile({
                firstname: formData.firstname,
                lastname: formData.lastname,
                phone: formData.phoneNumber,
                emergency: formData.emergencyNumber
            });
        
            // After successful registration, redirect to login
            navigate('/login', { 
                state: { 
                    message: 'Create Profile successful! Thank you for using our service.' 
                }
            });

        } catch (err) {
            setError(err.message || 'Create your profile failed. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [formData, navigate]);

    return (
        <div className='flex flex-col justify-center items-center w-[560px]'>

            {/* Header */}
            <h1 className='lg:text-5xl sm:text-3xl text-3xl font-medium lg:mb-6 mb-4 sm:mb-4 bg-gradient-to-r from-[#0076D4] to-[#303841] bg-clip-text text-transparent '>
                Create Your Profile
            </h1>
            <p className='lg:text-xl sm:text-md mb-6 lg:mb-8 sm:mb-6'>Please, Enter Your Information below.</p>
            
            {/* Input Section */}
            <form className='space-y-4 lg:space-y-6 sm:space-y-4 w-full' onSubmit={handleSubmit}>
                {/* Firstname */}
                <div>
                    <label htmlFor="firstname" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Firstname
                    </label>
                    <input 
                        type="text"
                        id='firstname'
                        name='firstname'
                        value={formData.firstname}
                        onChange={handleChange}
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder="Your Firstname"
                        required
                    />
                </div>
                
                {/* Lastname */}
                <div>
                    <label htmlFor="lastname" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Lastname
                    </label>
                    <input 
                        type="text"
                        id='lastname'
                        name='lastname'
                        value={formData.lastname}
                        onChange={handleChange}
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder="Your Lastname"
                        required
                    />
                </div>

                {/* Phone number */}
                <div>
                    <label htmlFor="phoneNumber" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Phone Number
                    </label>
                    <input 
                        type="text"
                        id='phoneNumber'
                        name='phoneNumber'
                        value={formData.phoneNumber}
                        onChange={handleChange}
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder="123-123-1234"
                        required
                    />
                </div>

                {/* Phone number */}
                <div>
                    <label htmlFor="emergencyNumber" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Emergency Phone Number
                    </label>
                    <input 
                        type="text"
                        id='emergencyNumber'
                        name='emergencyNumber'
                        value={formData.emergencyNumber}
                        onChange={handleChange}
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder="123-123-1234"
                        required
                    />
                </div>
                
                {/* Submit button */}
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2 sm:py-3 lg:py-4 text-white border-none rounded-2xl bg-gradient-to-r from-gray-700 via-blue-500 to-blue-300 text-md sm:text-lg lg:text-xl cursor-pointer shadow-md hover:translate-y-[-2px] duration-200"
                >
                    {loading ? 'Creating...' : 'Create Profile'}
                </button>
            </form>
        </div>
    )
}

export default CreateProfileForm