import React, { useState, useEffect } from 'react';
import TenantsTable from '../../../components/table/tenants_table'
import TenantsPageSkeleton from '../../../components/skeleton/tenants_page_skeleton';
import { getAllTenants, deleteTenant } from '../../../api/services/tenants.service';

function TenantsPage() {
  const [tenants, setTenants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchTenants();
  }, []);

  const fetchTenants = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getAllTenants();
      setTenants(data);
    } catch (err) {
      console.error('Failed to fetch tenants:', err);
      setError('Failed to load tenants. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (tenant) => {
    // TODO: Implement edit functionality (open modal or navigate to edit page)
    console.log('Edit tenant:', tenant);
    alert(`Edit functionality for ${tenant.firstName} ${tenant.lastName} - Coming soon!`);
  };

  const handleDelete = async (tenant) => {
    if (!window.confirm(`Are you sure you want to delete ${tenant.firstName} ${tenant.lastName}?`)) {
      return;
    }

    try {
      await deleteTenant(tenant.id);
      // Refresh the list after successful deletion
      await fetchTenants();
      alert('Tenant deleted successfully');
    } catch (err) {
      console.error('Failed to delete tenant:', err);
      alert('Failed to delete tenant. They may have active leases or payments.');
    }
  };

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
        {error && (
          <div className="mt-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
            {error}
            <button 
              onClick={fetchTenants}
              className="ml-4 text-sm underline hover:no-underline"
            >
              Retry
            </button>
          </div>
        )}
        {!error && !loading && (
          <p className="text-gray-600 mt-2">
            Total tenants: {tenants.length}
          </p>
        )}
      </div>

      {/* Tenants Table */}
      <div className=''>
        <TenantsTable 
          tenants={tenants}
          loading={loading}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>
    </div>
  )
}

export default TenantsPage