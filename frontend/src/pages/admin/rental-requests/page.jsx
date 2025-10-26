import React from 'react'
import RentalRequestsCard from '../../../components/card/rental_requests_card'
import { HiOutlineInbox } from 'react-icons/hi2';

function RentalRequestsPage() {


    return (
        <div>
            {/* Header */}
            <div className='mb-8'>
                <div className='title'>Rental Requests</div>
            </div>
            
            {/* Empty State */}
            <div className="p-16 flex flex-col items-center justify-center text-center">
                <div className="w-28 h-28 bg-gray-200 rounded-full flex items-center justify-center mb-6">
                    <HiOutlineInbox className="h-16 w-16 text-gray-600" />
                </div>
                <h2 className="text-xl font-semibold text-gray-900 mb-2">No Pending Request</h2>
                <p className="text-gray-500">
                    You're all caught up! New Requests will appear here.
                </p>
            </div>

            {/* User Requests */}
            <div className=''>
                {/* Sort and Filter */}
                <div className='flex mb-6 gap-8'>
                    <button className='flex items-center bg-white border rounded-lg px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md'>Sort By:</button>
                    <button className='flex items-center bg-white border rounded-lg px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md'>Filter By:</button>
                </div>
                <RentalRequestsCard />
            </div>
        </div>
    )
}

export default RentalRequestsPage