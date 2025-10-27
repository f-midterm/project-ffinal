import React from 'react'
import BillChart from './bill_chart'

function ElectricBillChart() {
    // Mock up Data
    const electricityData = [
        { name: 'May', value: 350 },
        { name: 'Jun', value: 400 },
        { name: 'Jul', value: 420 },
        { name: 'Aug', value: 410 },
        { name: 'Sep', value: 390 },
        { name: 'Oct', value: 320 },
    ];

    return (
        <div>
            <BillChart 
                title={"Electricity Bill"}
                data={electricityData}
                strokeColor={"#F08787"}
            />
        </div>
    )
}

export default ElectricBillChart