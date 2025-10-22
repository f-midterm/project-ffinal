/**
 * Rental Requests Service
 * 
 * Handles all rental request-related API calls including fetching,
 * creating, updating, and managing rental applications.
 * 
 * @module api/services/rentalRequests.service
 */

import apiClient from '../client/apiClient';

/**
 * Retrieves all rental requests
 * 
 * @async
 * @function getAllRentalRequests
 * @returns {Promise<Array<{id: number, unit: object, applicantName: string, email: string, phoneNumber: string, requestDate: string, status: string}>>} Array of all rental requests
 * @throws {Error} When fetching rental requests fails
 * 
 * @example
 * const requests = await getAllRentalRequests();
 * console.log(`Total rental requests: ${requests.length}`);
 */
export const getAllRentalRequests = async () => {
  return await apiClient.get('/rental-requests');
};

/**
 * Retrieves a specific rental request by ID
 * 
 * @async
 * @function getRentalRequestById
 * @param {number} id - Rental request ID
 * @returns {Promise<{id: number, unit: object, applicantName: string, email: string, phoneNumber: string, requestDate: string, status: string}>} Rental request details
 * @throws {Error} When request not found or fetch fails
 * 
 * @example
 * const request = await getRentalRequestById(1);
 * console.log(`Applicant: ${request.applicantName}`);
 */
export const getRentalRequestById = async (id) => {
  return await apiClient.get(`/rental-requests/${id}`);
};

/**
 * Creates a new rental request
 * 
 * @async
 * @function createRentalRequest
 * @param {object} requestData - Rental request creation data
 * @param {number} requestData.unitId - ID of the unit being applied for
 * @param {string} requestData.applicantName - Full name of the applicant
 * @param {string} requestData.email - Applicant's email address
 * @param {string} requestData.phoneNumber - Applicant's phone number
 * @param {string} [requestData.message] - Additional message from applicant
 * @param {string} [requestData.status='PENDING'] - Request status (PENDING, APPROVED, REJECTED)
 * @returns {Promise<{id: number, unit: object, applicantName: string, email: string, phoneNumber: string, requestDate: string, status: string}>} Created rental request
 * @throws {Error} When creation fails
 * 
 * @example
 * const newRequest = await createRentalRequest({
 *   unitId: 5,
 *   applicantName: 'Jane Smith',
 *   email: 'jane@example.com',
 *   phoneNumber: '9876543210',
 *   message: 'Looking for a long-term lease',
 *   status: 'PENDING'
 * });
 */
export const createRentalRequest = async (requestData) => {
  return await apiClient.post('/rental-requests', requestData);
};

/**
 * Updates an existing rental request
 * 
 * @async
 * @function updateRentalRequest
 * @param {number} id - Rental request ID to update
 * @param {object} requestData - Updated request data
 * @param {string} [requestData.status] - Updated status
 * @param {string} [requestData.email] - Updated email
 * @param {string} [requestData.phoneNumber] - Updated phone number
 * @param {string} [requestData.message] - Updated message
 * @returns {Promise<{id: number, unit: object, applicantName: string, email: string, phoneNumber: string, requestDate: string, status: string}>} Updated rental request
 * @throws {Error} When update fails or request not found
 * 
 * @example
 * const updated = await updateRentalRequest(1, {
 *   status: 'APPROVED'
 * });
 */
export const updateRentalRequest = async (id, requestData) => {
  return await apiClient.put(`/rental-requests/${id}`, requestData);
};

/**
 * Deletes a rental request
 * 
 * @async
 * @function deleteRentalRequest
 * @param {number} id - Rental request ID to delete
 * @returns {Promise<void>}
 * @throws {Error} When deletion fails
 * 
 * @example
 * await deleteRentalRequest(5);
 * console.log('Rental request deleted successfully');
 */
export const deleteRentalRequest = async (id) => {
  return await apiClient.delete(`/rental-requests/${id}`);
};

/**
 * Retrieves pending rental requests only
 * 
 * @async
 * @function getPendingRentalRequests
 * @returns {Promise<Array<{id: number, unit: object, applicantName: string, email: string, requestDate: string}>>} Array of pending requests
 * @throws {Error} When fetching fails
 * 
 * @example
 * const pending = await getPendingRentalRequests();
 * console.log(`${pending.length} pending rental requests`);
 */
export const getPendingRentalRequests = async () => {
  const requests = await getAllRentalRequests();
  return requests.filter(request => request.status === 'PENDING');
};

/**
 * Approves a rental request
 * 
 * @async
 * @function approveRentalRequest
 * @param {number} id - Rental request ID to approve
 * @returns {Promise<{id: number, status: string}>} Updated rental request
 * @throws {Error} When approval fails
 * 
 * @example
 * await approveRentalRequest(1);
 * console.log('Rental request approved');
 */
export const approveRentalRequest = async (id) => {
  return await updateRentalRequest(id, { status: 'APPROVED' });
};

/**
 * Rejects a rental request
 * 
 * @async
 * @function rejectRentalRequest
 * @param {number} id - Rental request ID to reject
 * @returns {Promise<{id: number, status: string}>} Updated rental request
 * @throws {Error} When rejection fails
 * 
 * @example
 * await rejectRentalRequest(1);
 * console.log('Rental request rejected');
 */
export const rejectRentalRequest = async (id) => {
  return await updateRentalRequest(id, { status: 'REJECTED' });
};
