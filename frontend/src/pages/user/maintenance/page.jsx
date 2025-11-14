import React, { useState, useEffect } from 'react';
import { FiUpload } from "react-icons/fi";
import { SiFormspree } from "react-icons/si";
import UserMaintenanceSkelleton from '../../../components/skeleton/user_maintenance_skelleton';

function UserMaintenancePage() {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 2000); 

    return () => clearTimeout(timer);
  }, []);

  if (isLoading) {
    return <UserMaintenanceSkelleton />;
  }

  return (
    <div className='flex flex-col'>
        <form>
            <div className='grid lg:grid-cols-2 grid-cols-1 gap-6'>
                {/* Upload Picture Here */}
                <div className='bg-white p-6 shadow-md rounded-lg min-h-[600px]'>
                    <div className='flex flex-col border rounded-lg overflow-hidden items-center justify-center w-full h-full space-y-4 p-8'>
                        <div className='bg-gray-200 p-12 rounded-full'><FiUpload size={32} /></div>
                        <div className='text-xl font-medium text-center'>Upload Picture Here</div>
                        <button className='bg-blue-400 hover:bg-blue-500 text-white font-medium py-2 px-6 rounded-full hover:translate-y-[-1px] shadow-md hover:shadow-lg transition-all duration-300'>
                            Upload
                        </button>
                    </div>
                </div>
                
                {/* Maintenance Form */}
                <div className='bg-white p-6 shadow-md rounded-lg min-h-[600px]'>
                    <div className='text-xl font-medium flex gap-4 items-center mb-4'>
                        <SiFormspree size={24} />
                        Maintenance Form
                    </div>

                    <div className='space-y-6 mb-4'>
                        <label>
                            Maintenance Topic
                            <input 
                                type="text"
                                className='border border-gray-400 rounded-lg px-4 py-2 w-full mt-2'
                            />
                        </label>

                        <label>
                            Maintenance Description
                            <textarea 
                                type="text"
                                className='w-full border border-gray-400 rounded-lg px-4 py-2 mt-2 min-h-[200px]'
                            />
                        </label>
                    </div>
                    
                    <div className='w-full border-b border-gray-300 mb-6'></div>

                    <div className='flex justify-end gap-4'>
                        <button className='bg-gray-200 hover:bg-gray-300 rounded-lg px-6 py-2 text-gray-600 font-medium shadow-md hover:translate-y-[-1px] shadow-md hover:shadow-lg transition-all duration-300'>
                            Cancel
                        </button>
                        <button className='bg-blue-400 hover:bg-blue-500 rounded-lg px-6 py-2 text-white font-medium shadow-md hover:translate-y-[-1px] shadow-md hover:shadow-lg transition-all duration-300'>
                            Submit
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
  )
}

export default UserMaintenancePage;