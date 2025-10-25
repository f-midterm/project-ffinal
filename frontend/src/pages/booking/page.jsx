import React, { useState } from 'react';
import UnitList from '../../components/list/unit_list';
import UnitDetail from '../../components/form/unit_detail';
import useMediaQuery from '../../hooks/useMediaQuery';
import UnitSelectedModal from '../../components/modal/unit_selected_modal';

function BookingPage() {
  const [selectedUnitId, setSelectedUnitId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const isLargeScreen = useMediaQuery('(min-width: 1024px)');

  const handleSelectUnit = (unitId) => {
    setSelectedUnitId(unitId);
    if (!isLargeScreen) {
      setIsModalOpen(true);
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
  };

  return (
    <div>
      <div className='w-full max-w-6xl mx-auto rounded-xl p-6 sm:p-10'>
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className='title mb-4 lg:mb-6 p-2'>
            Booking Apartment
          </h1>
          <p className='text-xl lg:text-2xl mb-4 lg:mb-6'>Choose Available Room and Submit your information</p>
        </div>

        {/* Main Content */}
        <div className='grid grid-cols-1 lg:grid-cols-3 gap-8'>

          {/* Left Column: Unit List */}
          <div className='lg:col-span-1'>
            <div>
              <UnitList 
                selectedUnitId={selectedUnitId} 
                onSelectUnit={handleSelectUnit} 
              />
            </div>
          </div>

          {/* Right Column: Unit Detail (on large screens) */}
          {isLargeScreen && (
            <div className="lg:col-span-2">
              <UnitDetail selectedUnitId={selectedUnitId} />
            </div>
          )}
        </div>

        {/* Modal for Unit Detail (on small screens) */}
        {!isLargeScreen && (
          <UnitSelectedModal isOpen={isModalOpen} onClose={handleCloseModal}>
            <UnitDetail selectedUnitId={selectedUnitId} onClose={handleCloseModal} />
          </UnitSelectedModal>
        )}
      </div>
    </div>
  );
}

export default BookingPage;