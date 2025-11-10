import React, { useState, useEffect } from 'react';
import { getUnitDetails } from '../../api/services/units.service';
import { PiBuilding, PiFileText } from "react-icons/pi";

function SelectedUnitDetail({ unitId }) {
  const [details, setDetails] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!unitId) {
      setLoading(false);
      setError("No unit ID provided.");
      return;
    }

    const fetchUnitDetails = async () => {
      setLoading(true);
      try {
        const data = await getUnitDetails(unitId);
        setDetails(data);
        setError(null);
      } catch (err) {
        setError('Failed to fetch unit details.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchUnitDetails();
  }, [unitId]);

  const getImageForUnitType = (type) => {
    if (!type) return '/standard_room.jpg'; // Default image
    switch (type.toLowerCase()) {
      case 'standard':
        return '/standard_room.jpg';
      case 'deluxe':
        return '/delux_room.jpg';
      default:
        return '/premium_room.jpg';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const options = { day: '2-digit', month: 'long', year: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-GB', options);
  };

  if (loading) {
    return <div>Loading unit details...</div>;
  }

  if (error) {
    return <div className="text-red-500">{error}</div>;
  }

  if (!details) {
    return <div>No unit details available.</div>;
  }

  const { unit, lease } = details;

  return (
    <div className="bg-white shadow-lg rounded-2xl w-full h-full overflow-hidden">
      <img 
        src={getImageForUnitType(unit?.type)}
        alt={`${unit?.type} room`}
        className="w-full h-48 object-cover"
      />
      <div className="p-8 lg:mt-2 mt-2">
        <h2 className="text-3xl font-bold text-gray-800 mb-6">Your Apartment</h2>
        
        {/* Unit Details */}
        <div className="mb-8">
          <div className="flex items-center mb-4">
            <PiBuilding size={24} className="text-gray-500 mr-3" />
            <h3 className="text-xl font-semibold text-gray-700">Unit Information</h3>
          </div>
          <div className="grid grid-cols-2 gap-4 text-md">
            <DetailItem label="Room Number" value={`Si ${unit?.roomNumber}`} />
            <DetailItem label="Unit Type" value={unit?.type} />
            <DetailItem label="Floor" value={unit?.floor} />
            <DetailItem label="Size" value={unit?.sizeSqm ? `${unit.sizeSqm} sqm` : 'N/A'} />
          </div>
        </div>

        {/* Lease Details */}
        {lease ? (
          <div>
            <div className="flex items-center mb-4">
              <PiFileText size={24} className="text-gray-500 mr-3" />
              <h3 className="text-xl font-semibold text-gray-700">Lease Agreement</h3>
            </div>
            <div className="grid grid-cols-2 gap-4 text-md">
              <DetailItem label="Lease Status" value={lease?.status} />
              <DetailItem label="Monthly Rent" value={unit?.rentAmount ? `${unit.rentAmount} ฿` : 'N/A'} />
              <DetailItem label="Start Date" value={formatDate(lease?.startDate)} />
              <DetailItem label="End Date" value={formatDate(lease?.endDate)} />
            </div>
          </div>
        ) : (
          <div className="text-center py-8 bg-gray-50 rounded-lg">
            <p className="text-gray-600">No active lease found for this unit.</p>
          </div>
        )}
      </div>

      {/* Check out Button */}
      <div className='flex justify-end p-4'>
        <button 
          className='bg-red-400 px-6 py-2 rounded-xl font-medium hover:bg-red-500 text-white shadow-md hover:translate-y-[-1px]'
          onClick={() => {}}
        >
          Check Out
        </button>
      </div>
    </div>
  );
}

const DetailItem = ({ label, value }) => (
  <div>
    <p className="text-sm text-gray-500">{label}</p>
    <p className="font-semibold text-gray-800">{value || 'N/A'}</p>
  </div>
);

export default SelectedUnitDetail;