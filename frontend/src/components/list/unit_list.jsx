import React, { useState, useEffect } from 'react';
import UnitSelectedCard from '../card/unit_selected_card';
import { getAvailableUnits } from '../../api/services/units.service';

function UnitList({ selectedUnitId, onSelectUnit }) {
  const [units, setUnits] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUnits = async () => {
      try {
        const availableUnits = await getAvailableUnits();
        setUnits(availableUnits);
      } catch (err) {
        setError('Failed to fetch units. Please try again later.');
        console.error(err);
      }
    };

    fetchUnits();
  }, []);

  if (error) {
    return <div className="text-red-500">{error}</div>;
  }

  return (
    <div className="space-y-4 lg:max-h-[600px] lg:overflow-y-auto pr-2 -mr-2">
      {units.map((unit) => (
        <UnitSelectedCard
          key={unit.id}
          unit={unit}
          isSelected={unit.id === selectedUnitId}
          onSelect={onSelectUnit}
        />
      ))}
    </div>
  );
}

export default UnitList;