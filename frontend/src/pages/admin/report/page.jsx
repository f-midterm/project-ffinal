import React from 'react'
import StatCard from '../../../components/card/stat_card'
import { GrMoney } from "react-icons/gr";
import { BsGraphUp,BsGraphDown } from "react-icons/bs";
import ReportSummaryChart from '../../../components/chart/report_summary_chart';

function ReportPage() {
  return (
    <div className='flex flex-col'>
        {/* Title */}
        <div className='title mb-4 lg:mb-6 py-2'>Report</div>
        
        {/* Stat Card */}
        <div className='grid grid-cols-1 lg:grid-cols-3 gap-4 mb-4 lg:mb-6'>
            <StatCard icon={<BsGraphUp />} title={"Total Profit"} value={`-`} color={"green"} />
            <StatCard icon={<GrMoney />} title={"Total Revenue"} value={`-`} color={"blue"} />
            <StatCard icon={<BsGraphDown />} title={"Total Expense"} value={`-`} color={"red"} />
        </div>

        {/* Chart Content */}
        <div className='flex-col'>
          <ReportSummaryChart title="Report Summary" strokeColor="#1E90FF" />
        </div>
    </div>
  )
}

export default ReportPage