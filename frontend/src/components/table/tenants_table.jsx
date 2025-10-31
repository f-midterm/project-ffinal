import React, { useState, useEffect } from 'react'
import { IoFilter } from "react-icons/io5";
import { BiSort } from "react-icons/bi";
import { getAllTenants, deleteTenant } from '../../api/services/tenants.service';

function TenantsTable() {
  const [tenants, setTenants] = useState([]);
  const [filteredTenants, setFilteredTenants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchTenants();
  }, []);

  useEffect(() => {
    // Filter tenants based on search term
    if (searchTerm) {
      const filtered = tenants.filter(tenant =>
        tenant.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tenant.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tenant.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tenant.phone?.includes(searchTerm)
      );
      setFilteredTenants(filtered);
    } else {
      setFilteredTenants(tenants);
    }
  }, [searchTerm, tenants]);

  const fetchTenants = async () => {
    try {
      setLoading(true);
      const data = await getAllTenants();
      setTenants(data);
      setFilteredTenants(data);
    } catch (error) {
      console.error('Failed to fetch tenants:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id, tenantName) => {
    if (window.confirm(`Are you sure you want to delete tenant: ${tenantName}?`)) {
      try {
        await deleteTenant(id);
        // Refresh the list
        await fetchTenants();
        alert('✅ Tenant deleted successfully!\n\nThe user role has been downgraded from VILLAGER to USER and can now book again.');
      } catch (error) {
        console.error('Failed to delete tenant:', error);
        
        // Show specific error message from backend
        const errorMessage = error.message || error.toString();
        
        if (errorMessage.includes('active lease') || errorMessage.includes('404')) {
          alert('❌ Cannot delete tenant with active leases!\n\n' +
                'Please terminate the lease first:\n' +
                '1. Go to Admin Dashboard\n' +
                '2. Click on the unit\n' +
                '3. Click "Terminate Lease" button\n' +
                '4. Then try deleting the tenant again');
        } else {
          alert('❌ Failed to delete tenant\n\n' + errorMessage);
        }
      }
    }
  };

  const handleEdit = (tenantId) => {
    // Navigate to edit page or open edit modal
    window.location.href = `/admin/tenants/${tenantId}/edit`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  return (
    <div className='bg-white rounded-lg shadow overflow-hidden'>
        {/* Table Header */}
        <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
            {/* Search Box*/}
            <div className='lg:w-1/4'>
                <input 
                    type="text"
                    id='search'
                    name='search'
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className='lg:px-3 lg:py-2 px-2 w-full border-2 rounded-xl focus:ring-2 focus:ring-blue-500 focus:outline-none'
                    placeholder='Search tenants...'
                />
            </div>

            {/* Action Button */}
            <div className='flex lg:gap-4 gap-1'>
                <div className='flex lg:gap-2 gap-1 items-center btn border-2 lg:py-2 lg:px-4 p-1 rounded-lg hover:translate-y-[-1px] hover:shadow-md cursor-pointer text-xs lg:text-base'>
                    <IoFilter />Filter
                </div>
                <div className='flex lg:gap-2 gap-1 items-center btn border-2 lg:py-2 lg:px-4 p-1 rounded-lg hover:translate-y-[-1px] hover:shadow-md cursor-pointer text-xs lg:text-base'>
                    <BiSort />Sort
                </div>
                <div className='flex items-center btn bg-blue-500 lg:py-2 lg:px-4 p-1 rounded-lg text-white hover:translate-y-[-1px] hover:shadow-md cursor-pointer text-xs lg:text-base'>
                    + Tenants
                </div>
            </div>
        </div>
        
        {/* Table Section */}
        <div className='overflow-x-auto'>
            <table className="min-w-full divide-y divide-gray-200">
                <thead className='bg-gray-50'>
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                        <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Email</th>
                        <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Phone</th>
                        <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Occupation</th>
                        <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Status</th>
                        <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Action</th>
                    </tr>
                </thead>

                <tbody className='bg-white divide-y divide-gray-200'>
                    {loading ? (
                        <tr>
                            <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                Loading tenants...
                            </td>
                        </tr>
                    ) : filteredTenants.length === 0 ? (
                        <tr>
                            <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                {searchTerm ? 'No tenants found matching your search.' : 'No tenants found.'}
                            </td>
                        </tr>
                    ) : (
                        filteredTenants.map((tenant) => (
                            <tr key={tenant.id} className='hover:bg-gray-50'>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    {tenant.firstName} {tenant.lastName}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">{tenant.email || 'N/A'}</td>
                                <td className="px-6 py-4 whitespace-nowrap">{tenant.phone || 'N/A'}</td>
                                <td className="px-6 py-4 whitespace-nowrap">{tenant.occupation || 'N/A'}</td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                                        tenant.status === 'ACTIVE' 
                                            ? 'bg-green-100 text-green-800' 
                                            : tenant.status === 'INACTIVE'
                                            ? 'bg-gray-100 text-gray-800'
                                            : 'bg-yellow-100 text-yellow-800'
                                    }`}>
                                        {tenant.status}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className='flex gap-4 items-center'>
                                        <button 
                                            onClick={() => handleEdit(tenant.id)}
                                            className='text text-blue-500 hover:underline cursor-pointer'
                                        >
                                            Edit
                                        </button>
                                        <button 
                                            onClick={() => handleDelete(tenant.id, `${tenant.firstName} ${tenant.lastName}`)}
                                            className='text text-red-500 hover:underline cursor-pointer'
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>
    </div>
  )
}

export default TenantsTable