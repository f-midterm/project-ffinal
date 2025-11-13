import React, { useState, useEffect } from 'react'
import { MdOutlineNotificationsPaused } from "react-icons/md";
import NotificationsCard from '../../../components/card/notifications_card';

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

            {/* Notification List */}
            <div className='flex flex-col'>
                <div className='mb-4 text-xl font-medium'>
                    Upcoming
                </div>

                <div className='border-b border-gray-400 mb-4'></div>

                <div className='flex gap-6 mb-6 items-center '>
                    <div className='w-full'>
                        <NotificationsCard />
                    </div>
                </div>
                
                <div className='flex justify-between items-center'>
                    <div className='text-xl font-medium mb-4'>
                        Have Read
                    </div>
                    <div className='flex gap-4 mb-4 items-center'>
                        <div>Sort by :</div>
                        <div>Filter by :</div>
                    </div>
                </div>

                <div className='border-b border-gray-400 mb-4'></div>

                <div className='flex gap-6 mb-6 items-center '>
                    <div className='w-full'><NotificationsCard /></div>
                </div>
            </div>
        </div>
    )
}

export default AdminNotificationsPage