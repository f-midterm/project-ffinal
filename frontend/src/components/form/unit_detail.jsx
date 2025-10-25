import React, { useState, useEffect } from 'react';
import { getUnitById } from '../../api/services/units.service';

function UnitDetail({ selectedUnitId, onClose }) {
  const [unit, setUnit] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (selectedUnitId) {
      const fetchUnitDetails = async () => {
        setLoading(true);
        try {
          const unitData = await getUnitById(selectedUnitId);
          setUnit(unitData);
          setError(null);
        } catch (err) {
          setError('Failed to fetch unit details.');
          console.error(err);
        } finally {
          setLoading(false);
        }
      };

      fetchUnitDetails();
    }
  }, [selectedUnitId]);

  if (!selectedUnitId) {
    return (
      <div className="flex items-center justify-center h-full p-8 border-2 border-dashed rounded-lg">
        <p className="text-lg text-gray-500">Select a unit to see the details</p>
      </div>
    );
  }

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div className="text-red-500">{error}</div>;
  }

  if (!unit) {
    return null;
  }

  const getImageForUnitType = (type) => {
    switch (type.toLowerCase()) {
      case 'standard':
        return '/standard_room.jpg';
      case 'deluxe':
        return '/delux_room.jpg';
      default:
        return '/premium_room.jpg';
    }
  };

  const leaseDurationOptions = [
    { value: 1, label: '1 months', discount: 0 },
    { value: 3, label: '3 months', discount: 0 },
    { value: 6, label: '6 Months', discount: 0 },
    { value: 12, label: '1 Year', discount: 5 },
    { value: 24, label: '2 Years', discount: 10 }
  ];

  return (
    <div className="border rounded-lg shadow-lg">
      {/* Header with Image */}
      <div>
        <img 
          src={getImageForUnitType(unit.type)}
          alt={`${unit.type} room`}
          className="w-full h-48 object-cover rounded-t-lg"
        />
      </div>

      {/* Content */}
      <div className='px-8 py-6'>
        <h2 className="text-2xl font-bold mb-4">Unit {unit.roomNumber}</h2>
        <div className="space-y-2">
          <p><span className="font-semibold">Type:</span> {unit.type}</p>
          <p><span className="font-semibold">Rent:</span> ${unit.rentAmount}/month</p>
          <p><span className="font-semibold">Floor:</span> {unit.floor}</p>
          <p><span className="font-semibold">Size:</span> {unit.sizeSqm} sqm</p>
          <p><span className="font-semibold">Status:</span> {unit.status}</p>
          {unit.description && (
            <p><span className="font-semibold">Description:</span> {unit.description}</p>
          )}
          <form>
            <label htmlFor="leaseDuration" className='flex items-center gap-2'><p className='font-semibold'>Lease Duration :</p>
              <select 
                id="leaseDuration"
                name='leaseDuration'
                className='px-3 py-1 sm:py-1 border border-gray-300 rounded-lg focus:outline-none  focus:ring-2 focus:ring-blue-500 text-sm sm:text-base'
                required
              >
                {leaseDurationOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label} {option.discount > 0 && `(${option.discount}% off)`}
                  </option>
                ))}
              </select>
            </label>
          </form>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="px-8 py-6 flex justify-end space-x-4">
        <button onClick={onClose} className="px-4 py-2 bg-gray-300 text-gray-800 rounded-lg hover:bg-gray-400">
          Cancel
        </button>
        <button type='submit' className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600">
          Submit
        </button>
      </div>
    </div>
  );
}

export default UnitDetail;