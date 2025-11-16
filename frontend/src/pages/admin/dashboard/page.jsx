import React, { useState, useEffect } from 'react';
import StatCard from '../../../components/card/stat_card';
import { MdPendingActions } from "react-icons/md";
import { PiHammer } from "react-icons/pi";
import { GrDocumentText } from "react-icons/gr";
import { HiOutlineBuildingOffice2 } from "react-icons/hi2";
import { Link } from 'react-router-dom';
import UnitCard from '../../../components/card/unit_card';
import { getAllUnits } from '../../../api/services/units.service';
import { getAllRentalRequests } from '../../../api/services/rentalRequests.service';
import { getAllMaintenanceRequests } from '../../../api/services/maintenance.service';
import { getAllPayments } from '../../../api/services/payments.service';
import AdminDashboardSkeleton from '../../../components/skeleton/admin_dashboard_skeleton';

function AdminDashboard() {
  const [unitsByFloor, setUnitsByFloor] = useState({});
  const [rentalRequestsCount, setRentalRequestsCount] = useState(0);
  const [maintenanceRequestsCount, setMaintenanceRequestsCount] = useState(0);
  const [paymentRequestsCount, setPaymentRequestsCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [units, rentalRequests, maintenanceRequests, payments] = await Promise.all([
          getAllUnits(),
          getAllRentalRequests(),
          getAllMaintenanceRequests(),
          getAllPayments()
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
        setMaintenanceRequestsCount(maintenanceRequests.length);
        setPaymentRequestsCount(payments.length);
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
      <div className='grid gap-6 grid-cols-1 lg:grid-cols-4 justify-center items-center mb-8'>
        {/* Rental Requests */}
        <Link to="/admin/rental-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<MdPendingActions />} title={"Rental Requests"} value={`${rentalRequestsCount} Requests`} color={"green"} />
        </Link>

        {/* Maintenance Requests */}
        <Link to="maintenance-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<PiHammer />} title={"Maintenane Requests"} value={`${maintenanceRequestsCount} Requests`} color={"yellow"} />
        </Link>
          
        {/*  */}
        <Link to="/admin/payment-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<GrDocumentText />} title={"Payment Requests"} value={`${paymentRequestsCount} Upcoming`} color={"red"} />
        </Link>

        <Link to="/admin/billing/bulk-import" className='hover:translate-y-[-1px] hover:shadow-lg'>
          <StatCard icon={<GrDocumentText />} title={"Quick Import from CSV"} value={`25 - 30 Every month`} color={"blue"} />
        </Link>

       </div>


      <div className="border-b border-gray-400 mb-6"></div>
      
      <div className='flex justify-between mb-6'>
        <div className='text-xl font-medium flex gap-2 items-center'>
          <HiOutlineBuildingOffice2 />
          Unit Section
        </div>

        <div className='flex gap-4'>
          <button className='bg-white rounded-lg px-6 py-2 shadow-md hover:bg-gray-50 hover:translate-y-[-1px] hover:shadow-lg transition dulation-300'>
            Add Floor
          </button>
          <button className='bg-white rounded-lg px-6 py-2 shadow-md hover:bg-gray-50 hover:translate-y-[-1px] hover:shadow-lg transition dulation-300'>
            Add Room
          </button>
        </div>
      </div>


      {/* Floor Section */}
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
