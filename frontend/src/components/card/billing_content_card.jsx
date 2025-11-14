import React, { useState, useEffect } from 'react'
import { FiChevronRight } from "react-icons/fi";

function BillingContentCard() {
  return (
    <div className='flex justify-between gap-16 items-center bg-white rounded-xl p-6 shadow-md hover:translate-y-[-1px] hover:shadow-lg cursor-pointer'>
        <div className='flex lg:justify-between flex-col lg:flex-row w-full'>
            <div className='text-gray-400'>Invoice ID</div>
            <div>Due Date</div>
            <div>Tenant name</div>
            <div>Unit</div>
            <div className='text-indigo-500'>Type</div> {/* Type: RENT, MAINTENANCE, OTHER*/}
            <div className='text-blue-600'>Amount</div>
        </div>

        <div><FiChevronRight /></div>
    </div>
  )
}

export default BillingContentCard