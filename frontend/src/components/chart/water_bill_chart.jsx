import React from 'react'
import BillChart from './bill_chart'

function WaterBillChart() {
    // Mock up Data
    const waterData = [
        { name: 'May', value: 120 },
        { name: 'Jun', value: 130 },
        { name: 'Jul', value: 125 },
        { name: 'Aug', value: 140 },
        { name: 'Sep', value: 135 },
        { name: 'Oct', value: 115 },
    ];
    return (
        <div>
            <BillChart 
                title={"Water Bill"}
                data={waterData}
                strokeColor={"#6D94C5"}
            />
        </div>
    )
}

export default WaterBillChart