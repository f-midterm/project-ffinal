import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { VscEye, VscEyeClosed } from "react-icons/vsc";

function SignupForm() {

    const [showPassword, setShowPassword] = useState(false)
    const [showConfirmPassword, setShowConfirmPassword] = useState(false)
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')

    const togglePassword = () => {
        setShowPassword(!showPassword);
    };

    const toggleConfirmPassword = () => {
        setShowConfirmPassword(!showConfirmPassword);
    };

    const passwordsMatch = password === confirmPassword

    return (
        <div className='flex flex-col justify-center items-center'>

            {/* Header */}
            <h1 className='lg:text-5xl sm:text-3xl text-3xl font-medium lg:mb-6 mb-4 sm:mb-4 bg-gradient-to-r from-[#0076D4] to-[#303841] bg-clip-text text-transparent '>
                Get started with us
            </h1>
            <p className='lg:text-xl sm:text-md mb-6 lg:mb-8 sm:mb-6'>Create a new account to get started with <span className='text-[#0076D4]'>BeLiv</span>.</p>

            {/* Input Section */}
            <div>
                
            </div>
            <form className='space-y-4 lg:space-y-6 sm:space-y-4 w-full'>

                {/* Username */}
                <div>
                    <label htmlFor="username" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Username
                    </label>
                    <input 
                        type="text"
                        id='username'
                        name='username'
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder="Enter Username"
                        required
                    />
                </div>
                
                {/* Email */}
                <div>
                    <label htmlFor="email" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Email
                    </label>
                    <input 
                        type="email"
                        id='email'
                        name='email'
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder="who@company.com"
                        required
                    />
                </div>

                {/* Enter Password */}
                <div className='relative'>
                    <label htmlFor="password" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Password
                    </label>
                    <input
                        type={showPassword ? 'text' : 'password'}
                        id='password'
                        name='password'
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className='w-full px-3 py-3 sm:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                        placeholder='Enter your Password'
                        required
                    />
                    <button
                        type="button"
                        onClick={togglePassword}
                        className="absolute lg:inset-y-16  inset-y-14 right-0 flex items-center px-3 text-gray-500 hover:text-gray-700"
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                        {showPassword ? <VscEyeClosed size={20} /> : <VscEye size={20} />}
                    </button>
                </div>

                {/* Confirm Password */}
                <div className='relative'>
                    <label htmlFor="confirm-password" className='block lg:text-xl sm:text-md text-md lg:mb-3 mb-2'>
                        Confirm Password
                    </label>
                    <input
                        type={showConfirmPassword ? 'text' : 'password'}
                        id='confirm-password'
                        name='confirm-password'
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className={`w-full px-3 py-3 sm:py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm sm:text-base ${confirmPassword && !passwordsMatch ? 'border-red-500' : 'border-gray-300'}`}
                        placeholder='Confirm your Password'
                        required
                    />
                    {confirmPassword && !passwordsMatch && (
                        <p className="text-sm text-red-600 mt-1">Passwords do not match</p>
                    )}
                    <button
                        type="button"
                        onClick={toggleConfirmPassword}
                        className="absolute lg:inset-y-16  inset-y-14 right-0 flex items-center px-3 text-gray-500 hover:text-gray-700"
                        aria-label={showConfirmPassword ? 'Hide confirm password' : 'Show confirm password'}
                    >
                        {showConfirmPassword ? <VscEyeClosed size={20} /> : <VscEye size={20} />}
                    </button>
                </div>

                {/* Remember and Forget Password */}
                {/* <div className="flex flex-col sm:flex-row sm:justify-between gap-3 sm:gap-0">
                    <label className="remember-me text-sm sm:text-sm lg:text-lg">
                        <input type="checkbox" className="mr-1" /> Remember me
                    </label>
                    <Link to="/forgot-password" className="text-sm sm:text-sm lg:text-lg font-medium text-blue-500 hover:underline">
                        Forgot Password?
                    </Link>
                </div> */}

                {/* Submit button */}
                <button
                    type="submit"
                    className={`w-full py-2 sm:py-3 lg:py-4 text-white border-none rounded-2xl bg-gradient-to-r from-gray-700 via-blue-500 to-blue-300 text-md sm:text-lg lg:text-xl shadow-md duration-200 cursor-pointer hover:translate-y-[-2px]`}
                >
                    Create account
                </button>
                
                <div className='form-divider'><span>OR</span></div>
                <p className="flex lg:text-lg sm:text-md text-md justify-center">
                    Already Have An Account? <Link to="/login" className="text-[#0076D4] hover:underline px-1">Sign In</Link>
                </p>
            </form>
        </div>
    )
}

export default SignupForm