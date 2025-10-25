import React, { useState, useEffect } from 'react';
import { getUnitById } from '../../api/services/units.service';

function UnitDetail({ selectedUnitId }) {
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

  return (
    <div className="p-8 border rounded-lg shadow-lg">
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
      </div>
    </div>
  );
}

export default UnitDetail;