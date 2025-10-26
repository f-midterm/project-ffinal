import React from 'react'

function TenantsUnitTable() {
    const infoItems = [
        { label: 'Current tenant', value: 'John Doe', isAvatar: true },
        { label: 'Lease Dates', value: 'Aug 1, 2025 - Jul 31, 2026' },
        { label: 'Monthly Rent', value: '2,500.00 Baht/Unit' },
    ];

    return (
        <div className='bg-white shadow-md rounded-lg'>
            <div className='p-6'>
                <h2 className="text-xl font-semibold text-gray-800 mb-4">Tenant Information</h2>
                <div className="divide-y divide-gray-200">
                    {infoItems.map((item, index) => (
                        <div key={index} className='flex justify-between items-center py-4'>
                            <span className="text-gray-600">{item.label}</span>
                            <div className="flex items-center gap-3">
                                <span className="text-gray-900 font-medium">{item.value}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

export default TenantsUnitTable