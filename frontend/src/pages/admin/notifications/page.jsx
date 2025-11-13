import React, { useState, useEffect } from 'react'
import { MdOutlineNotificationsPaused } from "react-icons/md";

function AdminNotificationsPage() {

    return (
        <div className=''>
            
            {/* Empty State */}
            <div className='flex flex-col justify-center items-center min-h-[600px]'>
                <div className='bg-gray-200 p-8 rounded-full mb-6'>
                    <MdOutlineNotificationsPaused size={100} className='text-gray-400' />
                </div>

                <div className='text-4xl font-medium mb-4 text-gray-700'>
                    You're all caught up!
                </div>

                <div className='text-lg text-gray-400'>
                    Come back later for more notifications.
                </div>
            </div>

        </div>
    )
}

export default AdminNotificationsPage