import React from 'react'
import { IoFilter } from "react-icons/io5";
import { BiSort } from "react-icons/bi";

function MaintenanceTable() {
  return (
    <div className='bg-white rounded-lg shadow overflow-hidden'>
      {/* Table Header */}
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        {/* Search Box*/}
          <div className='lg:w-1/4'>
            <input 
              type="text"
              id='search'
              name='search'
              className='lg:px-3 lg:py-2 px-2 w-full border-2 rounded-xl focus:ring-2 focus:ring-blue-500 focus:outline-none'
              placeholder='Search'
            />
          </div>

        {/* Action Button */}
          <div className='flex lg:gap-4 gap-1'>
            <div className='flex lg:gap-2 gap-1 items-center btn border-2 lg:py-2 lg:px-4 p-1 rounded-lg hover:translate-y-[-1px] hover:shadow-md cursor-pointer text-xs lg:text-base'>
              <IoFilter />Filter
            </div>
            <div className='flex lg:gap-2 gap-1 items-center btn border-2 lg:py-2 lg:px-4 p-1 rounded-lg hover:translate-y-[-1px] hover:shadow-md cursor-pointer text-xs lg:text-base'>
              <BiSort />Sort
            </div>
            <div className='flex items-center btn bg-blue-500 lg:py-2 lg:px-4 p-1 rounded-lg text-white hover:translate-y-[-1px] hover:shadow-md cursor-pointer text-xs lg:text-base'>
              + Maintenance
            </div>
          </div>
      </div>

      {/* Table Section */}
      <div className='overflow-x-auto'>
        <table className="min-w-full divide-y divide-gray-200">
          <thead className='bg-gray-50'>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Unit</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
          </thead>

          <tbody className='bg-white divide-y divide-gray-200'>
            <tr className='hover:bg-gray-50'>
              <td className="px-6 py-4 whitespace-nowrap"></td>
              <td className="px-6 py-4 whitespace-nowrap"></td>
              <td className="px-6 py-4 whitespace-nowrap"></td>
              <td className="px-6 py-4 whitespace-nowrap"></td>
              <td className="px-6 py-4 whitespace-nowrap"></td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className='flex gap-4 items-center'>
                  <div className='text text-indigo-700 hover:underline cursor-pointer'>More info</div>
                  <div className='text text-blue-500 hover:underline cursor-pointer'>Edit</div>
                  <div className='text text-red-500 hover:underline cursor-pointer'>Delete</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default MaintenanceTable