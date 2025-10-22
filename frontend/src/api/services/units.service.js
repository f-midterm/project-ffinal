/**
 * Units Service
 * 
 * Handles all unit-related API calls including fetching, creating,
 * updating, and deleting apartment units.
 * 
 * @module api/services/units.service
 */

import apiClient from '../client/apiClient';

/**
 * Retrieves all apartment units
 * 
 * @async
 * @function getAllUnits
 * @returns {Promise<Array<{id: number, unitNumber: string, floor: number, rent: number, status: string}>>} Array of all units
 * @throws {Error} When fetching units fails
 * 
 * @example
 * const units = await getAllUnits();
 * console.log(`Total units: ${units.length}`);
 */
export const getAllUnits = async () => {
  return await apiClient.get('/units');
};

/**
 * Retrieves a specific unit by ID
 * 
 * @async
 * @function getUnitById
 * @param {number} id - Unit ID
 * @returns {Promise<{id: number, unitNumber: string, floor: number, rent: number, status: string}>} Unit details
 * @throws {Error} When unit not found or request fails
 * 
 * @example
 * const unit = await getUnitById(1);
 * console.log(`Unit ${unit.unitNumber} on floor ${unit.floor}`);
 */
export const getUnitById = async (id) => {
  return await apiClient.get(`/units/${id}`);
};

/**
 * Creates a new apartment unit
 * 
 * @async
 * @function createUnit
 * @param {object} unitData - Unit creation data
 * @param {string} unitData.unitNumber - Unit number (e.g., "101", "A-205")
 * @param {number} unitData.floor - Floor number
 * @param {number} unitData.rent - Monthly rent amount
 * @param {string} [unitData.status='AVAILABLE'] - Unit status (AVAILABLE, OCCUPIED, MAINTENANCE)
 * @returns {Promise<{id: number, unitNumber: string, floor: number, rent: number, status: string}>} Created unit
 * @throws {Error} When creation fails (e.g., duplicate unit number)
 * 
 * @example
 * const newUnit = await createUnit({
 *   unitNumber: '301',
 *   floor: 3,
 *   rent: 1500,
 *   status: 'AVAILABLE'
 * });
 */
export const createUnit = async (unitData) => {
  return await apiClient.post('/units', unitData);
};

/**
 * Updates an existing unit
 * 
 * @async
 * @function updateUnit
 * @param {number} id - Unit ID to update
 * @param {object} unitData - Updated unit data
 * @param {string} [unitData.unitNumber] - Updated unit number
 * @param {number} [unitData.floor] - Updated floor number
 * @param {number} [unitData.rent] - Updated rent amount
 * @param {string} [unitData.status] - Updated status
 * @returns {Promise<{id: number, unitNumber: string, floor: number, rent: number, status: string}>} Updated unit
 * @throws {Error} When update fails or unit not found
 * 
 * @example
 * const updated = await updateUnit(1, {
 *   rent: 1600,
 *   status: 'OCCUPIED'
 * });
 */
export const updateUnit = async (id, unitData) => {
  return await apiClient.put(`/units/${id}`, unitData);
};

/**
 * Deletes a unit
 * 
 * @async
 * @function deleteUnit
 * @param {number} id - Unit ID to delete
 * @returns {Promise<void>}
 * @throws {Error} When deletion fails or unit has active leases
 * 
 * @example
 * await deleteUnit(5);
 * console.log('Unit deleted successfully');
 */
export const deleteUnit = async (id) => {
  return await apiClient.delete(`/units/${id}`);
};

/**
 * Retrieves available (unoccupied) units
 * 
 * @async
 * @function getAvailableUnits
 * @returns {Promise<Array<{id: number, unitNumber: string, floor: number, rent: number}>>} Array of available units
 * @throws {Error} When fetching fails
 * 
 * @example
 * const available = await getAvailableUnits();
 * console.log(`${available.length} units available for rent`);
 */
export const getAvailableUnits = async () => {
  const units = await getAllUnits();
  return units.filter(unit => unit.status === 'AVAILABLE');
};
