import React from 'react'
import { HiOutlineInbox } from 'react-icons/hi2'
import MaintenanceRequestCard from '../../../components/card/maintenance_request_card'


function MiantenanceRequestsPage() {
  return (
    <div className='flex flex-col'>
      {/* Header */}
      <div className='title mb-4 lg:mb-6'> 
        Maintenance Requests
      </div>

      {/* Sort select */}
      <div className='flex mb-6 gap-8 justify-end'>
        <select
          className='bg-white border rounded-lg px-4 py-2 mr-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
        >
          <option value="newest">Sort by: Newest</option>
          <option value="oldest">Sort by: Oldest</option>
        </select>
      </div>
      
      {/* Empty State */}
      <div class="flex flex-col justify-center items-center min-h-[400px]">
        <div className="p-12 bg-gray-200 rounded-full flex items-center justify-center mb-6">
          <HiOutlineInbox className="h-16 w-16 text-gray-600" />
        </div>

        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          No maintenance requests found.
        </h2>

        <p className="text-gray-500">
          You're all caught up! New requests will appear here.
        </p>
      </div>

      {/* Maintenance Request Cards */}
      <div className='flex flex-col space-y-6'>
        {/* Upcoming */}
        <div className='text-xl font-medium'>
          Upcoming {/* Pending */}
        </div>

        <div className='border-b border-gray-300'></div>
        <div className='space-y-4'>
          <MaintenanceRequestCard />
        </div>

        {/* Approved */}
        <div className='text-xl font-medium'>
          Approved
        </div>

        <div className='border-b border-gray-300'></div>
        <div>
          <MaintenanceRequestCard />
        </div>

        {/* Rejected */}
        <div className='text-xl font-medium'>
          Rejected
        </div>

        <div className='border-b border-gray-300'></div>
        <div>
          <MaintenanceRequestCard />
        </div>
      </div>
    </div>
  )
}

export default MiantenanceRequestsPage