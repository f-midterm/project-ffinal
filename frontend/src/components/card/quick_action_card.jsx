import React from 'react'

function QuickActionCard() {
  return (
    <div className='bg-white rounded-2xl shadow-md'>
        <div className='p-6'>
            {/* Title */}
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Quick Action</h2>

            {/* Container */}
            <div className="flex flex-col gap-3">
                <button className="w-full bg-white border border-gray-300 text-gray-800 font-semibold py-3 px-4 rounded-lg shadow-sm hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
                    View Lease Agreement
                </button>
                <button className="w-full bg-white border border-gray-300 text-gray-800 font-semibold py-3 px-4 rounded-lg shadow-sm hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
                    Contact Tenant
                </button>
                <button className="w-full bg-white border border-gray-300 text-gray-800 font-semibold py-3 px-4 rounded-lg shadow-sm hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
                    View Payment History
                </button>
            </div>
        </div>
    </div>
  )
}

export default QuickActionCard