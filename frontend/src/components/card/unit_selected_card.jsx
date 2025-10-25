import React from 'react'

function UnitSelectedCard({ isSelected, onSelect }) {

    // Base classes for the card
    const baseClasses = 'p-4 rounded-lg border-2 cursor-pointer transition-all duration-200';

    // Classes for the selected state
    const selectedClasses = isSelected
        ? 'border-blue-500 ring-2 ring-blue-600' // Highlighted if selected
        : 'border-gray-200 hover:border-gray-300 hover:shadow-md'; // Standard border

    return (
        <div 
            onClick={onSelect}
            className={`${baseClasses} ${selectedClasses}`}
        >
            <h3 className="text-lg font-medium text-gray-900">Unit Number</h3>
            <p className="text-sm text-gray-500">
                Unit Price, Unit Type
            </p>
        </div>
    )
}

export default UnitSelectedCard