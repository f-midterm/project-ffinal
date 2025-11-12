import React, { useState, useEffect, use } from 'react'
import { useParams, useNavigate } from 'react-router-dom';
import { getUnitDetails } from '../../../../api';
import { HiArrowLeft } from "react-icons/hi2";

function PaymentsHistoryPage() {

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
        Payments History
      </div>   
    </div>
  )
}

export default PaymentsHistoryPage