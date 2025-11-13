import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom';
import { FcInspection } from "react-icons/fc";
import BillingContentCard from '../../../components/card/billing_content_card';

function BillingPage() {

    return (
        <div>
            
            {/* Empty State */}
            <div className='flex flex-col items-center justify-center min-h-[500px]'>
                <div className='bg-gray-200 rounded-full p-10 mb-6'>
                    <FcInspection size={64} />
                </div>
                <div className='text-3xl text-gray-600 font-medium mb-4'>
                    You're not have any bills
                </div>
            </div>
            
            {/* Sort and Filter */}
            <div className='flex justify-end font-medium text-gray-700 gap-4 mb-4'>
                <select 
                    className='bg-white border rounded-lg px-4 py-2 mr-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                    <option value="newest">Sort by: Newest</option>
                    <option value="oldest">Sort by: Oldest</option>
                </select>
            </div>

            {/* History and Upcoming */}
            <div className='flex flex-col'>
                <div className='mb-4 text-xl font-medium'>
                    Upcoming
                </div>

                <div className='border-b border-gray-400 mb-4'></div>

                <div className='flex gap-6 mb-6 items-center '>
                    <div className='flex bg-green-200 rounded-full px-4 py-2 text-green-600 flex-col items-center justify-center text-sm'>
                        <div>Date</div>
                        <div>Month</div>
                        <div>Year</div>
                    </div>
                    <div className='w-full'>
                        <BillingContentCard />
                    </div>
                </div>
                
                <div className='text-xl font-medium mb-4'>
                    Finished
                </div>

                <div className='border-b border-gray-400 mb-4'></div>

                <div className='flex gap-6 mb-6 items-center '>
                    <div className='flex bg-blue-200 rounded-full px-4 py-2 text-blue-600 flex-col items-center justify-center text-sm'>
                        <div>Date</div>
                        <div>Month</div>
                        <div>Year</div>
                    </div>
                    <div className='w-full'><BillingContentCard /></div>
                </div>
            </div>
        </div>
    )
}

export default BillingPage