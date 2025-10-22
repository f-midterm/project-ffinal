/**
 * Maintenance Service
 * 
 * Handles all maintenance request-related API calls including fetching,
 * creating, updating, and managing maintenance requests.
 * 
 * @module api/services/maintenance.service
 */

import apiClient from '../client/apiClient';

/**
 * Retrieves all maintenance requests
 * 
 * @async
 * @function getAllMaintenanceRequests
 * @returns {Promise<Array<{id: number, unit: object, description: string, status: string, priority: string, requestDate: string, completionDate: string}>>} Array of all maintenance requests
 * @throws {Error} When fetching maintenance requests fails
 * 
 * @example
 * const requests = await getAllMaintenanceRequests();
 * console.log(`Total maintenance requests: ${requests.length}`);
 */
export const getAllMaintenanceRequests = async () => {
  return await apiClient.get('/maintenance-requests');
};

/**
 * Retrieves a specific maintenance request by ID
 * 
 * @async
 * @function getMaintenanceRequestById
 * @param {number} id - Maintenance request ID
 * @returns {Promise<{id: number, unit: object, description: string, status: string, priority: string, requestDate: string, completionDate: string}>} Maintenance request details
 * @throws {Error} When request not found or fetch fails
 * 
 * @example
 * const request = await getMaintenanceRequestById(1);
 * console.log(`Request: ${request.description}`);
 */
export const getMaintenanceRequestById = async (id) => {
  return await apiClient.get(`/maintenance-requests/${id}`);
};

/**
 * Creates a new maintenance request
 * 
 * @async
 * @function createMaintenanceRequest
 * @param {object} requestData - Maintenance request creation data
 * @param {number} requestData.unitId - ID of the unit requiring maintenance
 * @param {string} requestData.description - Detailed description of the issue
 * @param {string} [requestData.priority='MEDIUM'] - Priority level (LOW, MEDIUM, HIGH, URGENT)
 * @param {string} [requestData.status='PENDING'] - Request status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
 * @param {string} [requestData.requestDate] - Request date (ISO format, defaults to current date)
 * @returns {Promise<{id: number, unit: object, description: string, status: string, priority: string, requestDate: string}>} Created maintenance request
 * @throws {Error} When creation fails
 * 
 * @example
 * const newRequest = await createMaintenanceRequest({
 *   unitId: 5,
 *   description: 'Leaking faucet in kitchen',
 *   priority: 'HIGH',
 *   status: 'PENDING'
 * });
 */
export const createMaintenanceRequest = async (requestData) => {
  return await apiClient.post('/maintenance-requests', requestData);
};

/**
 * Updates an existing maintenance request
 * 
 * @async
 * @function updateMaintenanceRequest
 * @param {number} id - Maintenance request ID to update
 * @param {object} requestData - Updated request data
 * @param {string} [requestData.description] - Updated description
 * @param {string} [requestData.status] - Updated status
 * @param {string} [requestData.priority] - Updated priority
 * @param {string} [requestData.completionDate] - Completion date (ISO format)
 * @returns {Promise<{id: number, unit: object, description: string, status: string, priority: string, requestDate: string, completionDate: string}>} Updated maintenance request
 * @throws {Error} When update fails or request not found
 * 
 * @example
 * const updated = await updateMaintenanceRequest(1, {
 *   status: 'COMPLETED',
 *   completionDate: '2024-01-15'
 * });
 */
export const updateMaintenanceRequest = async (id, requestData) => {
  return await apiClient.put(`/maintenance-requests/${id}`, requestData);
};

/**
 * Deletes a maintenance request
 * 
 * @async
 * @function deleteMaintenanceRequest
 * @param {number} id - Maintenance request ID to delete
 * @returns {Promise<void>}
 * @throws {Error} When deletion fails
 * 
 * @example
 * await deleteMaintenanceRequest(5);
 * console.log('Maintenance request deleted successfully');
 */
export const deleteMaintenanceRequest = async (id) => {
  return await apiClient.delete(`/maintenance-requests/${id}`);
};

/**
 * Retrieves pending maintenance requests only
 * 
 * @async
 * @function getPendingRequests
 * @returns {Promise<Array<{id: number, unit: object, description: string, priority: string, requestDate: string}>>} Array of pending requests
 * @throws {Error} When fetching fails
 * 
 * @example
 * const pending = await getPendingRequests();
 * console.log(`${pending.length} pending requests`);
 */
export const getPendingRequests = async () => {
  const requests = await getAllMaintenanceRequests();
  return requests.filter(request => request.status === 'PENDING');
};

/**
 * Retrieves high priority maintenance requests
 * 
 * @async
 * @function getHighPriorityRequests
 * @returns {Promise<Array<{id: number, unit: object, description: string, status: string, requestDate: string}>>} Array of high priority requests
 * @throws {Error} When fetching fails
 * 
 * @example
 * const urgent = await getHighPriorityRequests();
 * console.log(`${urgent.length} high priority requests`);
 */
export const getHighPriorityRequests = async () => {
  const requests = await getAllMaintenanceRequests();
  return requests.filter(request => 
    request.priority === 'HIGH' || request.priority === 'URGENT'
  );
};
