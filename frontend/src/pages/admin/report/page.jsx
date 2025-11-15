import React from 'react'
import StatCard from '../../../components/card/stat_card'
import { GrMoney } from "react-icons/gr";
import { BsGraphUp,BsGraphDown } from "react-icons/bs";
import ReportSummaryChart from '../../../components/chart/report_summary_chart';
import BookingUnitTypeDonutChart from '../../../components/chart/booking_unit_type_donut_chart';
import UserBookingBarChart from '../../../components/chart/user_booking_bar_chart';

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

        {/* Report Summary Chart */}
        <div className='mb-6'>
          <ReportSummaryChart title="Report Summary" strokeColor="#1E90FF" />
        </div>
        
        <div className='flex gap-6'>
          {/* Booking Unit Type Donut Chart */}
          <div className='w-1/3'>
            <BookingUnitTypeDonutChart />
          </div>
          
          {/* User Booking Bar chart */}
          <div className='w-2/3'>
            <UserBookingBarChart />
          </div>
        </div>

        <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
          {/* Recent Payments */}
          <div></div>

          {/* New Tenants */}
          <div></div>         
        </div>
    </div>
  )
}

export default ReportPage