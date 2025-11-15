import React from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const data = [
  { name: 'Standard', value: 400 },
  { name: 'Deluxe', value: 300 },
  { name: 'Premium', value: 300 },
];

const COLORS = ['#0088FE', '#00C49F', '#FFBB28'];

const BookingUnitTypeDonutChart = () => {
  return (
    <div className="bg-white p-6 rounded-xl shadow-md">
      <h2 className="text-xl font-semibold text-gray-800 mb-4">Booking Unit Types</h2>
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={80}
            fill="#8884d8"
            paddingAngle={5}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};

export default BookingUnitTypeDonutChart;
