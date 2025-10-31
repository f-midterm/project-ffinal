import React from 'react'
import { useNavigate } from 'react-router-dom'

function UnitCard({ unit }) {
  const navigate = useNavigate();
  
  if (!unit) {
    return (
      <div className={`flex flex-col justify-center items-center cursor-pointer shadow-md hover:shadow-lg hover:translate-y-[-1px] transition-shadow p-4 rounded-xl`}>
        <p className={`font-medium text-md`}>UnitID</p>
        <p className={`text-sm mt-1`}>UnitStatus</p>
      </div>
    );
  }

  const isOccupied = unit.status === 'OCCUPIED';
  const isMaintenance = unit.status === 'MAINTENANCE';
  const bgColor = isOccupied ? 'bg-red-100' : isMaintenance ? 'bg-yellow-100' : 'bg-green-100';
  const textColor = isOccupied ? 'text-red-800' : isMaintenance ? 'text-yellow-800' : 'text-green-800';

  return (
    <div className={`flex flex-col justify-center items-center cursor-pointer shadow-md hover:shadow-lg hover:translate-y-[-1px] transition-shadow p-4 rounded-xl ${bgColor}`}>
      <p className={`font-medium text-md ${textColor}`}>{unit.roomNumber}</p>
      <p className={`text-sm mt-1 ${textColor}`}>{unit.status}</p>
    </div>
  )
}

export default UnitCard