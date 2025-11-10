import React, { useState, useEffect } from 'react';
import StatCard from '../../../components/card/stat_card';
import { MdPendingActions } from "react-icons/md";
import { PiHammer } from "react-icons/pi";
import { GrDocumentText } from "react-icons/gr";
import { Link } from 'react-router-dom';
import UnitCard from '../../../components/card/unit_card';
import { getAllUnits } from '../../../api/services/units.service';
import { getAllRentalRequests } from '../../../api/services/rentalRequests.service';
import AdminDashboardSkeleton from '../../../components/skeleton/admin_dashboard_skeleton';

function AdminDashboard() {
  const [unitsByFloor, setUnitsByFloor] = useState({});
  const [rentalRequestsCount, setRentalRequestsCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [units, rentalRequests] = await Promise.all([
          getAllUnits(),
          getAllRentalRequests()
        ]);

        const groupedUnits = units.reduce((acc, unit) => {
          const { floor } = unit;
          if (!acc[floor]) {
            acc[floor] = [];
          }
          acc[floor].push(unit);
          return acc;
        }, {});
        setUnitsByFloor(groupedUnits);
        setRentalRequestsCount(rentalRequests.length);
      } catch (error) {
        console.error("Failed to fetch data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <AdminDashboardSkeleton />;
  }

  return (
    <div className='flex flex-col'>
      {/* Title */}
      <div className='lg:mb-12 mb-8'>
        <h1 className='title mb-4 lg:mb-6'>
          Dashboard
        </h1>
        <p className='text-xl lg:text-2xl mb-4 lg:mb-6'>Welcome, Test</p>
        <p className='text-lg lg:text-xl text-gray-600'>Apartment : Siri Apartment, Floor : 2, Rooms : 24</p>
      </div>
        
      {/* Unit Section */}
        
      {/* Stat Card */}
      <div className='grid gap-6 grid-cols-1 lg:grid-cols-3 justify-center items-center mb-12'>
        {/* Rental Requests */}
        <Link to="/admin/rental-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<MdPendingActions />} title={"Rental Requests"} value={`${rentalRequestsCount} Requests`} color={"green"} />
        </Link>

        {/* Maintenance Requests */}
        <Link to="maintenance-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<PiHammer />} title={"Maintenane Requests"} value={`2 Requests`} color={"yellow"} />
        </Link>
          
        {/* Lease Renewals */}
        <Link to="lease-renewals" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<GrDocumentText />} title={"Lease Renewals"} value={`3 Upcoming`} color={"red"} />
        </Link>
      </div>

      {Object.entries(unitsByFloor).map(([floor, units]) => {
        const vacantCount = units.filter(unit => unit.status === 'AVAILABLE').length;
        const occupiedCount = units.length - vacantCount;

        return (
          <div key={floor} className='bg-white rounded-lg p-8 shadow-md mb-6 lg:mb-8'>
            <div className="lg:text-xl text-lg font-medium mb-2">{floor}{floor == 1 ? 'st' : 'nd'} Floor</div>

            {/* Status */}
            <div className='text-sm mb-4'>
              <span className='text-green-600'>{vacantCount} vacants</span>, <span className='text-red-600'>{occupiedCount} occupied</span>
            </div>

            {/* Units */}
            <div className='grid grid-cols-2 lg:grid-cols-6 sm:grid-cols-3 md:grid-cols-4 gap-4'>
              {units.map(unit => (
                <UnitCard key={unit.id} unit={unit} />
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default AdminDashboard;
