import React, { useState, useEffect } from "react";
import { createUnit } from "../../api/services/units.service";
import UnitSelectedModal from "./unit_selected_modal";

const AddRoomModal = ({ isOpen, onClose, onRoomAdded, floors }) => {
  const [roomNumber, setRoomNumber] = useState("");
  const [floor, setFloor] = useState("");
  const [type, setType] = useState("standard");
  const [rentAmount, setRentAmount] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (floors.length > 0) {
      setFloor(floors[0]);
    }
  }, [floors]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!roomNumber || !floor || !type || !rentAmount) {
      setError("All fields are required.");
      return;
    }

    try {
      await createUnit({
        roomNumber,
        floor: parseInt(floor, 10),
        type,
        rentAmount: parseFloat(rentAmount),
      });
      onRoomAdded();
      onClose();
    } catch (err) {
      setError("Failed to add room. It might already exist.");
      console.error(err);
    }
  };

  return (
    <UnitSelectedModal isOpen={isOpen} onClose={onClose} title="Add New Room">
      <form onSubmit={handleSubmit} className="p-6">
        <div className="mb-4">
          <label
            htmlFor="roomNumber"
            className="block font-medium text-gray-700"
          >
            Room Number
          </label>
          <input
            type="text"
            id="roomNumber"
            value={roomNumber}
            onChange={(e) => setRoomNumber(e.target.value)}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm px-4 py-2"
            placeholder="Enter room number"
          />
        </div>
        <div className="mb-4">
          <label
            htmlFor="floor"
            className="block font-medium text-gray-700"
          >
            Floor
          </label>
          <select
            id="floor"
            value={floor}
            onChange={(e) => setFloor(e.target.value)}
            className="mt-1 block w-full rounded-md border-gray-400 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm px-4 py-2"
          >
            {floors.map((f) => (
              <option key={f} value={f}>
                {f}
              </option>
            ))}
          </select>
        </div>
        <div className="mb-4">
          <label
            htmlFor="type"
            className="block font-medium text-gray-700"
          >
            Type
          </label>
          <select
            id="type"
            value={type}
            onChange={(e) => setType(e.target.value)}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm px-4 py-2"
          >
            <option value="standard">Standard</option>
            <option value="deluxe">Deluxe</option>
            <option value="premium">Premium</option>
          </select>
        </div>
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <div className="mt-4 flex justify-end space-x-2">
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
            Add Room
          </button>
        </div>
      </form>
    </UnitSelectedModal>
  );
};

export default AddRoomModal;
