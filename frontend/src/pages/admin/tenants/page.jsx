import React, { useState, useEffect } from 'react';
import TenantsTable from '../../../components/table/tenants_table'
import TenantsPageSkeleton from '../../../components/skeleton/tenants_page_skeleton';

function TenantsPage() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate data fetching
    const timer = setTimeout(() => {
      setLoading(false);
    }, 1500); // Adjust time as needed

    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return <TenantsPageSkeleton />;
  }

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