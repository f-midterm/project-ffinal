import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUnitDetails } from '../../../../api/services/units.service';
import apiClient from '../../../../api/client/apiClient';
import { FaArrowLeft, FaSave } from 'react-icons/fa';

function UnitEditPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  
  const [formData, setFormData] = useState({
    roomNumber: '',
    floor: '',
    type: '',
    rentAmount: '',
    sizeSqm: '',
    description: '',
    status: 'AVAILABLE'
  });

  useEffect(() => {
    fetchUnit();
  }, [id]);

  const fetchUnit = async () => {
    try {
      setLoading(true);
      const data = await getUnitDetails(id);
      setFormData({
        roomNumber: data.unit.roomNumber || '',
        floor: data.unit.floor || '',
        type: data.unit.type || '',
        rentAmount: data.unit.rentAmount || '',
        sizeSqm: data.unit.sizeSqm || '',
        description: data.unit.description || '',
        status: data.unit.status || 'AVAILABLE'
      });
    } catch (err) {
      console.error('Failed to fetch unit:', err);
      setError('Failed to load unit data');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.roomNumber || !formData.floor || !formData.type || !formData.rentAmount) {
      alert('Please fill in all required fields');
      return;
    }

    try {
      setSaving(true);
      await apiClient.put(`/units/${id}`, formData);
      alert('Unit updated successfully!');
      navigate(`/admin/units/${id}`);
    } catch (err) {
      console.error('Failed to update unit:', err);
      alert('Failed to update unit. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xl text-gray-600">Loading unit data...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64">
        <div className="text-xl text-red-600 mb-4">{error}</div>
        <button
          onClick={() => navigate(`/admin/units/${id}`)}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Back to Unit Details
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col p-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <button
          onClick={() => navigate(`/admin/units/${id}`)}
          className="flex items-center text-blue-600 hover:text-blue-800 mb-4"
        >
          <FaArrowLeft className="mr-2" />
          Back to Unit Details
        </button>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Edit Unit</h1>
        <p className="text-gray-600">Update unit information and pricing</p>
      </div>

      {/* Form */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <form onSubmit={handleSubmit}>
          <div className="grid gap-6 md:grid-cols-2">
            {/* Room Number */}
            <div>
              <label htmlFor="roomNumber" className="block text-sm font-medium text-gray-700 mb-2">
                Room Number <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="roomNumber"
                name="roomNumber"
                value={formData.roomNumber}
                onChange={handleChange}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., 101"
              />
            </div>

            {/* Floor */}
            <div>
              <label htmlFor="floor" className="block text-sm font-medium text-gray-700 mb-2">
                Floor <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                id="floor"
                name="floor"
                value={formData.floor}
                onChange={handleChange}
                required
                min="1"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., 1"
              />
            </div>

            {/* Type */}
            <div>
              <label htmlFor="type" className="block text-sm font-medium text-gray-700 mb-2">
                Type <span className="text-red-500">*</span>
              </label>
              <select
                id="type"
                name="type"
                value={formData.type}
                onChange={handleChange}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select type</option>
                <option value="Standard">Standard</option>
                <option value="Deluxe">Deluxe</option>
                <option value="Premium">Premium</option>
              </select>
            </div>

            {/* Rent Amount */}
            <div>
              <label htmlFor="rentAmount" className="block text-sm font-medium text-gray-700 mb-2">
                Monthly Rent (Baht) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                id="rentAmount"
                name="rentAmount"
                value={formData.rentAmount}
                onChange={handleChange}
                required
                min="0"
                step="0.01"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., 8000.00"
              />
            </div>

            {/* Size */}
            <div>
              <label htmlFor="sizeSqm" className="block text-sm font-medium text-gray-700 mb-2">
                Size (㎡)
              </label>
              <input
                type="number"
                id="sizeSqm"
                name="sizeSqm"
                value={formData.sizeSqm}
                onChange={handleChange}
                min="0"
                step="0.01"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., 25.0"
              />
            </div>

            {/* Status */}
            <div>
              <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <select
                id="status"
                name="status"
                value={formData.status}
                onChange={handleChange}
                disabled={formData.status === 'OCCUPIED'}
                className={`w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  formData.status === 'OCCUPIED' ? 'bg-gray-100 cursor-not-allowed' : ''
                }`}
              >
                <option value="AVAILABLE">Available</option>
                <option value="OCCUPIED">Occupied</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="RESERVED">Reserved</option>
              </select>
              {formData.status === 'OCCUPIED' ? (
                <p className="mt-1 text-xs text-red-600 font-medium">
                  ⚠️ Cannot change status while unit is OCCUPIED. Please use "Terminate Lease" button on unit detail page.
                </p>
              ) : (
                <p className="mt-1 text-xs text-gray-500">
                  Note: Status changes may be managed automatically by the system
                </p>
              )}
            </div>

            {/* Description */}
            <div className="md:col-span-2">
              <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
                Description
              </label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows="4"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter unit description, features, or notes..."
              />
            </div>
          </div>

          {/* Action Buttons */}
          <div className="mt-8 flex gap-4 justify-end">
            <button
              type="button"
              onClick={() => navigate(`/admin/units/${id}`)}
              className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-blue-300 flex items-center gap-2"
            >
              <FaSave />
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>

      {/* Info Box */}
      <div className={`mt-6 border rounded-lg p-4 ${
        formData.status === 'OCCUPIED' 
          ? 'bg-amber-50 border-amber-200' 
          : 'bg-blue-50 border-blue-200'
      }`}>
        <h3 className={`font-semibold mb-2 ${
          formData.status === 'OCCUPIED' ? 'text-amber-900' : 'text-blue-900'
        }`}>Important Notes:</h3>
        <ul className={`list-disc list-inside text-sm space-y-1 ${
          formData.status === 'OCCUPIED' ? 'text-amber-800' : 'text-blue-800'
        }`}>
          <li>Changing the rent amount will affect future billing cycles</li>
          <li>Electricity and water rates are managed globally for all units</li>
          <li>Room number changes should be coordinated with physical signage</li>
          {formData.status === 'OCCUPIED' && (
            <li className="font-bold text-red-600">
              Unit is OCCUPIED: Cannot change status. Use "Terminate Lease" to end tenancy.
            </li>
          )}
          {formData.status !== 'OCCUPIED' && (
            <li>Status changes may be overridden by lease management</li>
          )}
        </ul>
      </div>
    </div>
  );
}

export default UnitEditPage;
