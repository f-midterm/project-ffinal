import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUnitDetails } from '../../../../api/services/units.service';
import { RiBillLine } from "react-icons/ri";
import { HiArrowLeft } from "react-icons/hi2";


function SendBillPage() {
    const { id } = useParams();
    const [unit, setUnit] = useState(null);
    const [tenant, setTenant] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();


    useEffect(() => {
        const fetchData = async () => {
            try {
                const { unit, tenant } = await getUnitDetails(id);
                setUnit(unit);
                setTenant(tenant);  
            } catch (err) {
                console.error(err);
            }
        };

        fetchData();
    }, [id]);

    if (!unit) {
        return <div>Loading...</div>;
    }

    return (
        <div className='flex flex-col'>
            {/* Back Button */}
            <button
                onClick={() => navigate(`/admin/unit/${unit.id}`)}
                className="flex items-center gap-2 text-gray-600 hover:text-blue-500 mb-6"
            >
                <HiArrowLeft className="w-5 h-5" />
                Back to Unit
            </button>

            <div>
                <div className='title lg:mb-6 mb-4'>Si {unit.roomNumber}</div>
            </div>
            
            <div className='bg-white rounded-xl mb-4 p-6 shadow-md text-xl font-medium flex gap-4 items-center'>
                <div className='bg-gray-200 p-4 rounded-full'><RiBillLine /></div>
                Create Invoice
            </div>
            
            <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>

                {/* Left Detail */}
                <div className='grid grid-cols-1 lg:grid-rows-2 gap-6'>
                    <div className='bg-white p-6 rounded-xl shadow-md'>
                        <div className='text-lg mb-4 font-medium'>
                            Invoice Details
                        </div>

                        <div className='text-gray-500 space-y-4'>
                            <div className='flex items-center justify-between'>
                                <div>Invoice ID :</div>
                                <div className='text-black'></div>
                            </div>
                            <div className='flex items-center justify-between'>
                                <div>Invoice Date :</div>
                                <div className='text-black'></div>
                            </div>
                            <div className='flex items-center justify-between'>
                                <div>Due Date :</div>
                                <div className='text-black'></div>
                            </div>
                        </div>
                    </div>
                    
                    <div className='bg-white p-6 rounded-xl shadow-md'>
                        <div className='text-lg mb-4 font-medium'>
                            Amount Details
                        </div>

                        <div className='text-gray-500 space-y-4'>
                            <div className='flex items-center justify-between'>
                                <div>Monthly Rent :</div>
                                <div className='text-black'>{unit?.rentAmount} ฿</div>
                            </div>
                            <div className='flex items-center justify-between'>
                                <div>Electricity Price :</div>
                                <div className='text-black'> ฿</div>
                            </div>
                            <div className='flex items-center justify-between'>
                                <div>Water Price :</div>
                                <div className='text-black'>฿</div>
                            </div>
                        </div>
                    </div>
                </div>
                

                {/* Right Detail */}
                <div className='bg-white p-6 rounded-xl shadow-md'>
                    <div className='text-lg mb-4 font-medium'>
                        Bill to
                    </div>

                    <div className='text-gray-500 space-y-4'>
                        <div className='flex items-center justify-between'>
                            <div>Name :</div>
                            <div className='text-black'>{tenant?.firstName} {tenant?.lastName}</div>
                        </div>
                        <div className='flex items-center justify-between'>
                            <div>Email :</div>
                            <div className='text-blue-500'>{tenant?.email}</div>
                        </div>
                        <div className='flex items-center justify-between'>
                            <div>Phone :</div>
                            <div className='text-black'>{tenant?.phone}</div>
                        </div>
                    </div>
                    
                    <div className='border-b border-gray-200 my-6'></div>

                    <div className='text-gray-500'>
                        <div className='flex items-center justify-between'>
                            <div>Total Amount :</div>
                            <div className='text-black'> ฿</div>
                        </div>
                    </div>

                    <div className='border-b border-gray-200 my-6'></div>
                    {/* Action Button */}
                    <div className='flex lg:mt-12 mt-6 justify-end gap-4 '>
                        <button 
                            className='px-4 py-2 bg-gray-200 text-gray-600 font-medium rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-gray-300'
                        >
                            Cancel
                        </button>
                        <button 
                            className='px-4 py-2 bg-blue-400 text-white font-medium rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-blue-500'
                        >
                            Send to Tenant
                        </button>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default SendBillPage;