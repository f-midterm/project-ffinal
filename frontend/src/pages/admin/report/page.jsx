import React from 'react'
import StatCard from '../../../components/card/stat_card'
import { GrMoney } from "react-icons/gr";
import { BsGraphUp,BsGraphDown } from "react-icons/bs";

function ReportPage() {
  return (
    <div className='flex flex-col'>
        {/* Title */}
        <div className='title mb-4 lg:mb-6 py-2'>Report</div>
        
        {/* Stat Card */}
        <div className='grid grid-cols-1 lg:grid-cols-3 gap-4'>
            <StatCard icon={<BsGraphUp />} title={"Total Profit"} value={`-`} color={"green"} />
            <StatCard icon={<GrMoney />} title={"Total Revenue"} value={`-`} color={"blue"} />
            <StatCard icon={<BsGraphDown />} title={"Total Expense"} value={`-`} color={"red"} />
        </div>

        {/* Chart Content */}
        
    </div>
  )
}

export default ReportPage