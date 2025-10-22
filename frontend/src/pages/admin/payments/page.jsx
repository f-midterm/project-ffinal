import React from 'react'
import PaymentsTable from '../../../components/table/payments_table'
import StatCard from '../../../components/card/stat_card'
import { MdAttachMoney, MdOutlinePending, MdOutlineTrendingUp } from "react-icons/md";
import { IoBanOutline } from "react-icons/io5";
import { Link } from 'react-router-dom'

function PaymentsPage() {
  return (
    <div className='flex flex-col'>
        {/* Header */}
        <div className='flex w-full items-center lg:mb-8 mb-6 justify-between'>
            {/* Title */}
            <div>
                <h1 className='title'>
                    Payments
                </h1>
            </div>

            {/* Generate Bill for pend to users */}
            <Link to="/admin/billing" className='btn bg-blue-500 text-white lg:px-6 px-4 lg:py-3 py-2 lg:text-lg text-sm rounded-lg text-lg hover:translate-y-[-1px] hover:shadow-md cursor-pointer'>
                Generate Bill
            </Link>
        </div>

        {/* Stat Card */}
        <div className='grid lg:grid-cols-4 grid-cols-1 lg:mb-6 mb-4 gap-4'>
            <StatCard icon={<MdAttachMoney />} title={"Paid this month"} value={``} color={"green"} />
            <StatCard icon={<MdOutlinePending />} title={"Pending"} value={``} color={"yellow"} />
            <StatCard icon={<IoBanOutline />} title={"Overdue"} value={``} color={"red"} />
            <StatCard icon={<MdOutlineTrendingUp />} title={"Total Revenue"} value={``} color={"blue"}/>
        </div>

        {/* Payment Table */}
        <div className=''>
            <PaymentsTable />
        </div>
    </div>
  )
}

export default PaymentsPage