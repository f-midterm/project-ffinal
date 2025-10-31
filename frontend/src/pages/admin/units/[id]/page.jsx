import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getUnitDetails } from '../../../../api/services/units.service';
import { getUtilityRates, updateUtilityRates } from '../../../../api/services/settings.service';
import { FaArrowLeft, FaEdit, FaPencilAlt } from 'react-icons/fa';

function UnitDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [unitData, setUnitData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [utilityRates, setUtilityRates] = useState({ electricityRate: '4.00', waterRate: '20.00' });
  const [isEditingRates, setIsEditingRates] = useState({ electricity: false, water: false });
  const [tempRates, setTempRates] = useState({ electricity: '4.00', water: '20.00' });

  useEffect(() => {
    fetchUnitDetails();
    fetchUtilityRates();
  }, [id]);

  const fetchUnitDetails = async () => {
    try {
      setLoading(true);
      const data = await getUnitDetails(id);
      setUnitData(data);
    } catch (err) {
      console.error('Failed to fetch unit details:', err);
      setError('Failed to load unit details');
    } finally {
      setLoading(false);
    }
  };

  const fetchUtilityRates = async () => {
    try {
      const response = await getUtilityRates();
      setUtilityRates(response);
      setTempRates({
        electricity: response.electricityRate,
        water: response.waterRate
      });
    } catch (err) {
      console.error('Failed to fetch utility rates:', err);
    }
  };

  const handleEditRate = (type) => {
    setIsEditingRates(prev => ({ ...prev, [type]: true }));
  };

  const handleSaveRate = async (type) => {
    try {
      const key = type === 'electricity' ? 'electricityRate' : 'waterRate';
      await updateUtilityRates({
        [key]: tempRates[type]
      });
      await fetchUtilityRates();
      setIsEditingRates(prev => ({ ...prev, [type]: false }));
      alert('Rate updated successfully! This applies to all units.');
    } catch (err) {
      console.error('Failed to update rate:', err);
      alert('Failed to update rate');
    }
  };

  const handleCancelEdit = (type) => {
    setTempRates(prev => ({
      ...prev,
      [type]: type === 'electricity' ? utilityRates.electricityRate : utilityRates.waterRate
    }));
    setIsEditingRates(prev => ({ ...prev, [type]: false }));
  };

  const handleTerminateLease = async () => {
    if (!lease) return;
    
    if (!window.confirm('Are you sure you want to terminate this lease? This action cannot be undone.')) {
      return;
    }
    
    try {
      // TODO: Implement terminate lease API call when backend endpoint is ready
      // await apiClient.put(`/leases/${lease.id}/terminate`);
      alert('Terminate lease feature coming soon');
      // await fetchUnitDetails();
    } catch (err) {
      console.error('Failed to terminate lease:', err);
      alert('Failed to terminate lease');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xl text-gray-600">Loading unit details...</div>
      </div>
    );
  }

  if (error || !unitData) {
    return (
      <div className="flex flex-col items-center justify-center h-64">
        <div className="text-xl text-red-600 mb-4">{error || 'Unit not found'}</div>
        <button
          onClick={() => navigate('/admin')}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Back to Dashboard
        </button>
      </div>
    );
  }

  const { unit, lease, tenant } = unitData;
  const isOccupied = unit.status === 'OCCUPIED';

  return (
    <div className="flex flex-col p-6 max-w-7xl mx-auto">
      {/* Header Section */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Si {unit.roomNumber}</h1>
            <p className="text-gray-600 mt-1">{unit.type}, {unit.floor === 1 ? '1st' : unit.floor === 2 ? '2nd' : unit.floor + 'th'} Floor</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={() => navigate(`/admin/units/${id}/edit`)}
              className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Edit Unit
            </button>
            <button
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
            >
              + Add Maintenance
            </button>
          </div>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Left Column - Tenant & Lease Info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Tenant Information Card */}
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-800">Tenant Information</h2>
            </div>
            <div className="p-6">
              {isOccupied && tenant ? (
                <div className="space-y-4">
                  <div className="flex items-center">
                    <div className="w-12 h-12 bg-gray-300 rounded-full mr-4"></div>
                    <div>
                      <p className="text-sm text-gray-600">Current tenant</p>
                      <p className="font-medium text-gray-900">{tenant.firstName} {tenant.lastName}</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4 pt-4">
                    <div>
                      <p className="text-sm text-gray-600">Lease Dates</p>
                      <p className="font-medium text-gray-900">
                        {lease ? `${new Date(lease.startDate).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })} - ${new Date(lease.endDate).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}` : 'N/A'}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Monthly Rent</p>
                      <p className="font-medium text-gray-900">{lease?.rentAmount?.toLocaleString()} Baht</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Electricity Bill</p>
                      <div className="flex items-center gap-2">
                        {isEditingRates.electricity ? (
                          <>
                            <input
                              type="number"
                              step="0.01"
                              value={tempRates.electricity}
                              onChange={(e) => setTempRates(prev => ({ ...prev, electricity: e.target.value }))}
                              className="w-20 px-2 py-1 border border-gray-300 rounded"
                            />
                            <button
                              onClick={() => handleSaveRate('electricity')}
                              className="text-green-600 hover:text-green-700 text-sm"
                            >
                              ✓
                            </button>
                            <button
                              onClick={() => handleCancelEdit('electricity')}
                              className="text-red-600 hover:text-red-700 text-sm"
                            >
                              ✕
                            </button>
                          </>
                        ) : (
                          <>
                            <p className="font-medium text-gray-900">{utilityRates.electricityRate} Baht/Unit</p>
                            <button
                              onClick={() => handleEditRate('electricity')}
                              className="text-gray-400 hover:text-gray-600"
                            >
                              <FaPencilAlt className="w-3 h-3" />
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Water Bill</p>
                      <div className="flex items-center gap-2">
                        {isEditingRates.water ? (
                          <>
                            <input
                              type="number"
                              step="0.01"
                              value={tempRates.water}
                              onChange={(e) => setTempRates(prev => ({ ...prev, water: e.target.value }))}
                              className="w-20 px-2 py-1 border border-gray-300 rounded"
                            />
                            <button
                              onClick={() => handleSaveRate('water')}
                              className="text-green-600 hover:text-green-700 text-sm"
                            >
                              ✓
                            </button>
                            <button
                              onClick={() => handleCancelEdit('water')}
                              className="text-red-600 hover:text-red-700 text-sm"
                            >
                              ✕
                            </button>
                          </>
                        ) : (
                          <>
                            <p className="font-medium text-gray-900">{utilityRates.waterRate} Baht/Unit</p>
                            <button
                              onClick={() => handleEditRate('water')}
                              className="text-gray-400 hover:text-gray-600"
                            >
                              <FaPencilAlt className="w-3 h-3" />
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <p>No current tenant</p>
                  <p className="text-sm">This unit is available</p>
                </div>
              )}
            </div>
          </div>

          {/* Maintenance Log Card */}
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-800">Maintenance Log</h2>
            </div>
            <div className="p-6">
              <table className="w-full">
                <thead>
                  <tr className="text-left text-sm text-gray-600">
                    <th className="pb-3">Date</th>
                    <th className="pb-3">Issue</th>
                    <th className="pb-3">Status</th>
                    <th className="pb-3">Cost</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td colSpan="4" className="text-center py-8 text-gray-500">
                      No maintenance records
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Column - Quick Actions & Unit Files */}
        <div className="space-y-6">
          {/* Quick Actions Card */}
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-800">Quick Action</h2>
            </div>
            <div className="p-6 space-y-3">
              {isOccupied && lease ? (
                <>
                  <button
                    onClick={() => navigate(`/admin/leases/${lease.id}`)}
                    className="w-full px-4 py-2.5 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors"
                  >
                    View Lease Agreement
                  </button>
                  <button
                    onClick={() => navigate(`/admin/tenants/${tenant?.id}`)}
                    className="w-full px-4 py-2.5 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors"
                  >
                    Contact Tenant
                  </button>
                  <button
                    onClick={() => navigate(`/admin/payments?unitId=${unit.id}`)}
                    className="w-full px-4 py-2.5 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors"
                  >
                    View Payment History
                  </button>
                  <button
                    onClick={handleTerminateLease}
                    className="w-full px-4 py-2.5 text-left text-red-600 hover:bg-red-50 rounded-lg transition-colors font-medium"
                  >
                    Terminate Lease
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={() => navigate(`/admin/rental-requests?unitId=${unit.id}`)}
                    className="w-full px-4 py-2.5 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors"
                  >
                    View Rental Requests
                  </button>
                  <button
                    onClick={() => navigate(`/admin/booking/${unit.id}`)}
                    className="w-full px-4 py-2.5 text-left text-gray-700 hover:bg-gray-50 rounded-lg transition-colors"
                  >
                    Create New Booking
                  </button>
                </>
              )}
            </div>
          </div>

          {/* Unit Files Card */}
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-800">Unit Files</h2>
            </div>
            <div className="p-6">
              <p className="text-center text-gray-500 py-8">No files uploaded</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UnitDetailPage;
