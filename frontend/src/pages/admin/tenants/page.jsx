import React from 'react'
import TenantsTable from '../../../components/table/tenants_table'
import StatCard from '../../../components/card/stat_card'
import { Link } from 'react-router-dom'

function TenantsPage() {
  return (
    <div className='flex flex-col'>
      {/* Title */}
      <div className='lg:mb-8 mb-6'>
        <h1 className='title'>
          Tenants
        </h1>
      </div>

      {/* Tanants Table */}
      <div className=''>
        <TenantsTable />
      </div>
    </div>
  )
}

export default TenantsPage