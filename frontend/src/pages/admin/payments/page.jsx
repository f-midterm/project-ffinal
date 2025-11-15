import React, { useState, useEffect } from 'react';
import PaymentsTable from '../../../components/table/payments_table'
import StatCard from '../../../components/card/stat_card'
import { MdAttachMoney, MdOutlinePending, MdOutlineTrendingUp } from "react-icons/md";
import { IoBanOutline } from "react-icons/io5";
import { Link } from 'react-router-dom'
import PaymentsPageSkeleton from '../../../components/skeleton/payments_page_skeleton';

function PaymentsPage() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate data fetching
    const timer = setTimeout(() => {
      setLoading(false);
    }, 1500); // Adjust time as needed

    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return <PaymentsPageSkeleton />;
  }

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
        <div className='grid lg:grid-cols-3 grid-cols-1 lg:mb-6 mb-4 gap-4'>
            <StatCard icon={<MdAttachMoney />} title={"Accepted"} value={``} color={"green"} />
            <StatCard icon={<MdOutlinePending />} title={"Pending"} value={``} color={"yellow"} />
            <StatCard icon={<IoBanOutline />} title={"Overdue"} value={``} color={"red"} />
        </div>

        {/* Payment Table */}
        <div className=''>
            <PaymentsTable />
        </div>
    </div>
  )
}

export default PaymentsPage