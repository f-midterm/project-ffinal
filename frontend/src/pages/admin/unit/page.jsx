import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getUnitById } from '../../../api/services/units.service';
import TenantsUnitTable from '../../../components/table/tenants_unit_table';
import ElectricBillChart from '../../../components/chart/electric_bill_chart';
import WaterBillChart from '../../../components/chart/water_bill_chart';
import MaintenanceLogTable from '../../../components/table/maintenance_log_table';
import QuickActionCard from '../../../components/card/quick_action_card';
import UnitFilesCard from '../../../components/card/unit_files_card';

function UnitPage() {
  const { id } = useParams();
  const [unit, setUnit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const unitData = await getUnitById(id);
        setUnit(unitData);

      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error fetching data: {error.message}</div>;
  }

  if (!unit) {
    return <div>Unit not found</div>;
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
        <div>
          <button className="bg-white text-gray-800 font-medium py-2 px-6 rounded-lg shadow-md hover:translate-y-[-1px] hover:bg-gray-50">
            Edit Unit
          </button>
        </div>
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6'>
        {/* Left Column */}
        <div className='lg:col-span-2 flex flex-col gap-6'>
          {/* Tenant infomation */}
          <TenantsUnitTable />

          {/* Electricity and Water bill graph */}
          <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
            <ElectricBillChart />
            <WaterBillChart />
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