import React, { useState, useEffect } from 'react';
import StatCard from '../../../components/card/stat_card'
import { MdDone, MdOutlinePending, MdOutlineDoNotDisturb } from "react-icons/md";
import { PiHammer } from "react-icons/pi";
import MaintenanceTable from '../../../components/table/maintenance_table';
import MaintenancePageSkeleton from '../../../components/skeleton/maintenance_page_skeleton';

function MaintenancePage() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate data fetching
    const timer = setTimeout(() => {
      setLoading(false);
    }, 1500); // Adjust time as needed

    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return <MaintenancePageSkeleton />;
  }

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