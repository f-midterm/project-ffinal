import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const data = [
  { name: 'Jan', bookings: 20 },
  { name: 'Feb', bookings: 30 },
  { name: 'Mar', bookings: 15 },
  { name: 'Apr', bookings: 25 },
  { name: 'May', bookings: 10 },
  { name: 'Jun', bookings: 18 },
];

const UserBookingBarChart = () => {
  return (
    <div className="bg-white p-6 rounded-xl shadow-md">
      <h2 className="text-xl font-semibold text-gray-800 mb-4">Booking Summary</h2>
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
