import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUnitDetails } from '../../../api/services/units.service';
import TenantsUnitTable from '../../../components/table/tenants_unit_table';
import ElectricBillChart from '../../../components/chart/electric_bill_chart';
import WaterBillChart from '../../../components/chart/water_bill_chart';
import MaintenanceLogTable from '../../../components/table/maintenance_log_table';
import QuickActionCard from '../../../components/card/quick_action_card';
import UnitFilesCard from '../../../components/card/unit_files_card';
import { FiAlertCircle } from "react-icons/fi";

import UnitPageSkeleton from '../../../components/skeleton/unit_page_skeleton';

function UnitPage() {
  const { id } = useParams();
  const [unit, setUnit] = useState(null);
  const [lease, setLease] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const { unit, lease, tenant } = await getUnitDetails(id);
        setUnit(unit);
        setLease(lease);
        setTenant(tenant);

      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  if (loading) {
    return <UnitPageSkeleton />;
  }

  if (error) {
    return <div>Error fetching data: {error.message}</div>;
  }

  if (!unit) {
    return <div>Unit not found</div>;
  }

  if (!lease) {
    return (
      <div>
        <div className='flex flex-col bg-white rounded-2xl shadow-md justify-center items-center min-h-screen'>
          <div className='bg-red-300 rounded-full p-6 text-white mb-4'>
            <FiAlertCircle size={64} />
          </div>

          <div className='text-4xl font-medium mb-4'>
            Vacant
          </div>

          <div className='text-xl mb-4'>
            Don't have any tenant, Please add the tenant first.
          </div>

          <button className='bg-blue-400 px-4 py-2 text-white font-medium rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-blue-500'>
            Add Tenant
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Room Number */}
      <div className='flex flex-row justify-between items-center sm:items-center gap-4'>
        <div>
          <div className='title lg:mb-6 mb-4'>Si {unit.roomNumber}</div>
          <div className='lg:text-xl text-lg text-gray-500'>
            Siri apartment, Floor <span>{unit.floor}</span>
          </div>
        </div>
        {/* Action Button */}
        <div className='flex justify-space gap-4'>
          <button 
            onClick={() => navigate(`/admin/unit/${unit.id}/send-invoice`)}
            className="bg-white text-gray-800 font-medium py-2 px-6 rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-gray-50"
          >
            Send Invoice
          </button>
          <button className="bg-blue-500 text-white font-medium py-2 px-6 rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-blue-600">
            Edit Unit
          </button>
        </div>
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6'>
        {/* Left Column */}
        <div className='lg:col-span-2 flex flex-col gap-6'>
          {/* Tenant infomation */}
          <TenantsUnitTable unit={unit} lease={lease} tenant={tenant} />

          {/* Electricity and Water bill graph */}
          <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
            <ElectricBillChart unitId={unit.id} />
            <WaterBillChart unitId={unit.id} />
          </div>

          {/* Maintenance Log Table */}
          <MaintenanceLogTable />
        </div>

        {/* Right Column */}
        <div className='lg:col-span-1 flex flex-col gap-6'>
          <QuickActionCard />
          <UnitFilesCard />
        </div>
      </div>
    </div>
  );
}

export default UnitPage;