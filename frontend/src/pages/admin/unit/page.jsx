import React from 'react'
import TenantsUnitTable from '../../../components/table/tenants_unit_table'

function UnitPage() {
  return (
    <div>
        {/* Room Number */}
        <div className='flex flex-row justify-between items-center sm:items-center gap-4'>
          <div>
            <div className='title lg:mb-6 mb-4'>Unit Number</div>
            <div className='lg:text-xl text-lg text-gray-500'>Siri apartment, Floor <span>1</span></div>
          </div>
          {/* Action Button */}
          <div>
            <button className="bg-white text-gray-800 font-medium py-2 px-6 rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-gray-50">
              Edit Unit
            </button>
          </div>
        </div>

        <div className='"grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6'>

          {/* Left Column */}
          <div className='lg:col-span-2 flex flex-col gap-6'>
            {/* Tenant infomation */}
            <TenantsUnitTable />

            {/* Electricity and Water bill graph */}
            <div>
              
            </div>
          </div>

          {/* Right Column */}
          <div className='lg:col-span-1 flex flex-col gap-6'>

          </div>
        </div>

    </div>
  )
}

export default UnitPage