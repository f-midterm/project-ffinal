import React, { useState } from "react";
import { createUnit } from "../../api/services/units.service";
import UnitSelectedModal from "./unit_selected_modal";

const AddFloorModal = ({ isOpen, onClose, onFloorAdded, floors }) => {
  const [floorNumber, setFloorNumber] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newFloor = parseInt(floorNumber, 10);

    if (!floorNumber || newFloor <= 0) {
      setError("Floor number must be greater than 0.");
      return;
    }

    if (floors.includes(floorNumber)) {
      setError("This floor already exists.");
      return;
    }

    try {
      // Create a placeholder unit to establish the new floor
      await createUnit({
        roomNumber: `FLOOR_${floorNumber}`,
        floor: newFloor,
        type: "placeholder",
        rentAmount: 0,
      });
      onFloorAdded();
      onClose();
    } catch (err) {
      setError("Failed to add floor. It might already exist.");
      console.error(err);
    }
  };

  return (
    <UnitSelectedModal isOpen={isOpen} onClose={onClose} title="Add New Floor">
      <form onSubmit={handleSubmit} className="p-6">
        <div className="mb-4">
          <label
            htmlFor="floorNumber"
            className="block font-medium text-gray-700"
          >
            Floor Number
          </label>
          <input
            type="number"
            id="floorNumber"
            value={floorNumber}
            onChange={(e) => {
              setFloorNumber(e.target.value);
              setError("");
            }}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm px-4 py-2"
            placeholder="Enter floor number"
          />
        </div>
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <div className="flex justify-end space-x-2">
          <button
            type="button"
            onClick={onClose}
            className="inline-flex justify-center rounded-md border border-transparent bg-gray-200 px-4 py-2 text-sm font-medium text-gray-800 hover:bg-gray-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-gray-500 focus-visible:ring-offset-2"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:ring-offset-2"
          >
            Add Floor
          </button>
        </div>
      </form>
    </UnitSelectedModal>
  );
};

export default AddFloorModal;
