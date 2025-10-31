import React, { useState, useEffect } from 'react'
import StatCard from '../../../components/card/stat_card'
import { MdPendingActions } from "react-icons/md";
import { PiHammer } from "react-icons/pi";
import { GrDocumentText } from "react-icons/gr";
import { Link, useNavigate } from 'react-router-dom'
import UnitCard from '../../../components/card/unit_card';
import { getAdminDashboard } from '../../../api/services/adminDashboard.service';
import { getAllUnits } from '../../../api/services/units.service';
import { getUsername } from '../../../api';

function AdminDashboard() {
  const [dashboardData, setDashboardData] = useState(null);
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboardData();
    fetchUnits();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const data = await getAdminDashboard();
      setDashboardData(data);
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUnits = async () => {
    try {
      const unitsData = await getAllUnits();
      setUnits(unitsData);
    } catch (error) {
      console.error('Failed to fetch units:', error);
    }
  };

  // Group units by floor
  const unitsByFloor = units.reduce((acc, unit) => {
    if (!acc[unit.floor]) {
      acc[unit.floor] = [];
    }
    acc[unit.floor].push(unit);
    return acc;
  }, {});

  // Get admin name from token
  const adminName = getUsername() || 'Admin';

  // Handle unit click
  const handleUnitClick = (unitId) => {
    navigate(`/admin/units/${unitId}`);
  };

  return (
    <div className='flex flex-col'>
      {/* Title */}
        <div className='lg:mb-12 mb-8'>
          <h1 className='title mb-4 lg:mb-6'>
            Dashboard
          </h1>
          <p className='text-xl lg:text-2xl mb-4 lg:mb-6'>Welcome, {adminName}</p>
          <p className='text-lg lg:text-xl text-gray-600'>
            Apartment : Siri Apartment, Floor : 2, Rooms : {dashboardData?.totalUnits || 0}
          </p>
        </div>
        
      {/* Unit Section */}
        
        {/* Stat Card */}
        <div className='grid gap-6 grid-cols-1 lg:grid-cols-3 justify-center items-center mb-12'>
          {/* Rental Requests */}
          <Link to="/admin/rental-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
            <StatCard 
              icon={<MdPendingActions />} 
              title={"Rental Requests"} 
              value={loading ? 'Loading...' : `${dashboardData?.pendingRentalRequests || 0} Pending`} 
              color={"green"} 
            />
          </Link>

          {/* Maintenance Requests */}
          <Link to="maintenance-requests" className='hover:translate-y-[-1px] hover:shadow-lg'>
            <StatCard 
              icon={<PiHammer />} 
              title={"Maintenance Requests"} 
              value={loading ? 'Loading...' : `${dashboardData?.maintenanceRequests || 0} Requests`} 
              color={"yellow"} 
            />
          </Link>
          
          {/* Lease Renewals */}
          <Link to="lease-renewals" className='hover:translate-y-[-1px] hover:shadow-lg'>
            <StatCard 
              icon={<GrDocumentText />} 
              title={"Lease Renewals"} 
              value={loading ? 'Loading...' : `${dashboardData?.leasesExpiringSoon || 0} Upcoming`} 
              color={"red"} 
            />
          </Link>
        </div>
        
        {/* Render floors dynamically */}
        {Object.keys(unitsByFloor)
          .sort((a, b) => Number(a) - Number(b))
          .map((floor) => {
            const floorUnits = unitsByFloor[floor];
            const vacantCount = floorUnits.filter(u => u.status === 'AVAILABLE').length;
            const occupiedCount = floorUnits.filter(u => u.status === 'OCCUPIED').length;
            
            return (
              <div key={floor} className='bg-white rounded-lg p-8 shadow-md mb-6 lg:mb-8'>
                <div className="lg:text-xl text-lg font-medium mb-2">
                  {floor === '1' ? '1st' : floor === '2' ? '2nd' : floor === '3' ? '3rd' : `${floor}th`} Floor
                </div>

                {/* Status */}
                <div className='text-sm mb-4'>
                  <span className='text-green-600'>{vacantCount} vacants</span>, <span className='text-red-600'>{occupiedCount} occupied</span>
                </div>

                {/* Units */}
                <div className='grid grid-cols-2 lg:grid-cols-6 sm:grid-cols-3 md:grid-cols-4 gap-4'>
                  {floorUnits.map((unit) => (
                    <div key={unit.id} onClick={() => handleUnitClick(unit.id)} className="cursor-pointer">
                      <UnitCard unit={unit} />
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
    </div>
  )
}

export default AdminDashboard