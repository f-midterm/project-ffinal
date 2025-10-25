import React from 'react'
import UnitList from '../../components/list/unit_list'
import UnitDetail from '../../components/form/unit_detail'

function BookingPage() {
  return (
    <div>
      <div className='w-full max-w-6xl mx-auto rounded-xl p-6 sm:p-10'>
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className='title mb-4 lg:mb-6 p-2'>
            Booking Apartment
          </h1>
          <p className='text-xl lg:text-2xl mb-4 lg:mb-6'>Choose Available Room and Submit your information</p>
        </div>

        {/* Main Content: 2-column layout */}
        <div className='grid grid-cols-1 lg:grid-cols-3 gap-8'>

          {/* Left Column: Unit List */}
          <div className='lg:col-span-1'>
            {/* NEW: Added wrapper for scrolling on long lists */}
            <div>
              <UnitList />
            </div>

          </div>

          {/* Right Column: Unit Detail */}
          <div className="lg:col-span-2">
            <UnitDetail />
          </div>
        </div>
      </div>
    </div>
  )
}

export default BookingPage