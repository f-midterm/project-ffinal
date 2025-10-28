import React from 'react'
import { Outlet } from 'react-router-dom';

function UserLayout() {

    return (
        <div className='min-h-screen flex flex-col'>
            {/* Header Section */}
            <header>

            </header>

            {/* Main Section */}
            <main className='flex-1 w-full mx-auto lg:py-36 lg:px-32 py-36 px-12 sm:px-24 bg-[#F3F3F3]'>
                <Outlet />
            </main>
        </div>
    )
}

export default UserLayout