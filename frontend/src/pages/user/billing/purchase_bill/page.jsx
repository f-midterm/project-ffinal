import React, { useState, useEffect } from 'react'
import { BiPurchaseTagAlt } from "react-icons/bi";

function PurchaseBillPage() {
    return (
        <div>

            <div className='bg-white bg-white rounded-xl shadow-md mb-4 p-6 text-xl font-medium flex gap-4 items-center'>
                <BiPurchaseTagAlt className='w-5 h-5' />
                Purchase Bill
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                
                {/* Bill PDF */}
                <div className='bg-white rounded-xl shadow-md p-6'>
                    <div className='border w-full h-full'>{/* PDF Here */}</div>
                </div>

                <div className='bg-white rounded-xl shadow-md p-6'>
                    <div className='font-medium text-xl mb-4'>Purchase Method</div>
                    <div>{/* QR Code Here */}</div>
                    
                </div>
            </div>
        </div>
    )
}

export default PurchaseBillPage