import React from 'react'
import { useNavigate } from 'react-router-dom'

function UnitCard() {
  // const navigate = useNavigate();
  // const isOccupied = unit.status === 'OCCUPIED';
  // const statusClass = isOccupied ? 'occupied' : 'vacant';
  // const bgColor = isOccupied ? 'bg-red-100' : 'bg-green-100';
  // const textColor = isOccupied ? 'text-red-800' : 'text-green-800';
  // const statusColor = isOccupied ? 'text-red-600' : 'text-green-600';

  // const handleNavigate = () => {
  //   if (unit.status === "AVAILABLE") {
  //     navigate(`/admin/booking/${unit.id}`)
  //   } 
  //   else if (unit.status === "OCCUPIED") {
  //     navigate(`/admin/unit/${unit.id}`)
  //   }
  // };

  return (
    <div className={`flex flex-col justify-center items-center cursor-pointer shadow-md hover:shadow-lg hover:translate-y-[-1px] transition-shadow p-4 rounded-xl`}>
      <p className={`font-medium text-md`}>UnitID</p>
      <p className={`text-sm mt-1`}>UnitStatus</p>
    </div>
  )
}

export default UnitCard