import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const data = [
  { name: 'Jan', bookings: 4 },
  { name: 'Feb', bookings: 2 },
  { name: 'Mar', bookings: 0 },
  { name: 'Apr', bookings: 5 },
  { name: 'May', bookings: 4 },
  { name: 'Jun', bookings: 2 },
];

const UserBookingBarChart = () => {
  return (
    <div className="bg-white p-8 rounded-xl shadow-md">
      <div className='flex justify-between items-center'>
        <h2 className="text-xl font-semibold text-gray-800 mb-4">Booking Summary</h2>
        <div className='flex gap-4'>
          <select className='border border-gray-300 px-4 py-1 rounded-lg'>
            <option value="bookings">Bookings</option>
            <option value="revenue">Checkout</option>
          </select>

          <select className='border border-gray-300 px-4 py-1 rounded-lg'>
            <option value="monthly">Monthly</option>
            <option value="yearly">Yearly</option>
          </select>
        </div>
      </div>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart
          data={data}
          margin={{
            top: 20,
            right: 30,
            left: 20,
            bottom: 5,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="bookings" fill="#82ca9d" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default UserBookingBarChart;
