import React from 'react'
import StatCard from '../../../components/card/stat_card'
import { MdDone, MdOutlinePending, MdOutlineDoNotDisturb } from "react-icons/md";
import { PiHammer } from "react-icons/pi";
import MaintenanceTable from '../../../components/table/maintenance_table';

function MaintenancePage() {
  return (
    <div className='flex flex-col'>
      {/* Header */}
      <div className='lg:mb-8 mb-6'>
        <h1 className='title'>
          Maintenance
        </h1>
      </div>

      {/* StatCard */}
      <div className='grid lg:grid-cols-4 grid-cols-1 lg:mb-6 mb-4 gap-4'>
        <StatCard icon={<MdDone />} title={"Success"} value={``} color={"green"} />
        <StatCard icon={<MdOutlinePending />} title={"Pending"} value={``} color={"yellow"} />
        <StatCard icon={<MdOutlineDoNotDisturb />} title={"Reject"} value={``} color={"red"} />
        <StatCard icon={<PiHammer />} title={"Total Maintenance"} value={``} color={"blue"}/>
      </div>
      
      {/* Maintenance Table */}
      <div>
        <MaintenanceTable />
      </div>
    </div>
  )
}

export default MaintenancePage