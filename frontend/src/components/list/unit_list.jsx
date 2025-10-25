import React from 'react'
import UnitSelectedCard from '../card/unit_selected_card'

function UnitList({ selectedUnitId, onSelectUnit }) {
  return (
    <div className="space-y-4 lg:max-h-[600px] lg:overflow-y-auto pr-2 -mr-2">
      <UnitSelectedCard
        onSelect={() => onSelectUnit()}
      />
    </div>
  )
}

export default UnitList