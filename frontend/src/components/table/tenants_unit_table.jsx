import React from 'react';

function TenantsUnitTable({ unit, lease, tenant }) {
    const isVacant = unit && unit.status === 'AVAILABLE';

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    };

    const infoItems = !isVacant && tenant && lease ? [
        { label: 'Current tenant', value: `${tenant.firstName} ${tenant.lastName}` },
        { label: 'Lease Dates', value: `${formatDate(lease.startDate)} - ${formatDate(lease.endDate)}` },
        { label: 'Monthly Rent', value: `${unit.rentAmount} ฿/month` },
        { label: 'Electricity Price/Unit', value: `${unit.electricityPricePerUnit} ฿/kWh` },
        { label: 'Water Price/Unit', value: `${unit.waterPricePerUnit} ฿/unit` },
    ] : [];

    return (
        <div className='bg-white shadow-md rounded-lg'>
            <div className='p-6'>
                <div>
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
        </div>
    );
}

export default TenantsUnitTable