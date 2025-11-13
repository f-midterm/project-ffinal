/**
 * Invoices Service
 * 
 * Handles all invoice-related API calls including creating invoices with payment line items
 * 
 * @module api/services/invoices.service
 */

import apiClient from '../client/apiClient';

/**
 * Creates an invoice with multiple payment line items (RENT, ELECTRICITY, WATER)
 * 
 * @async
 * @function createInvoice
 * @param {object} invoiceData - Invoice creation data
 * @param {number} invoiceData.leaseId - ID of the associated lease
 * @param {string} invoiceData.invoiceDate - Invoice date (YYYY-MM-DD format)
 * @param {string} invoiceData.dueDate - Due date (YYYY-MM-DD format)
 * @param {number} invoiceData.rentAmount - Rent amount (optional)
 * @param {number} invoiceData.electricityAmount - Electricity amount (optional)
 * @param {number} invoiceData.waterAmount - Water amount (optional)
 * @param {string} [invoiceData.notes] - Optional invoice notes
 * @returns {Promise<{id: number, invoiceNumber: string, lease: object, invoiceDate: string, dueDate: string, totalAmount: number, status: string, payments: Array}>} Created invoice with invoice number and payment line items
 * @throws {Error} When invoice creation fails
 * 
 * @example
 * const invoice = await createInvoice({
 *   leaseId: 1,
 *   invoiceDate: '2025-11-13',
 *   dueDate: '2025-11-28',
 *   rentAmount: 5000,
 *   electricityAmount: 800,
 *   waterAmount: 200,
 *   notes: 'ค่าเช่าประจำเดือน พฤศจิกายน 2025'
 * });
 * console.log(`Invoice created: ${invoice.invoiceNumber}`); // INV-20251113-1
 */
export const createInvoice = async (invoiceData) => {
  return await apiClient.post('/invoices/create', invoiceData);
};

/**
 * Retrieves an invoice by ID
 * 
 * @async
 * @function getInvoiceById
 * @param {number} id - Invoice ID
 * @returns {Promise<{id: number, invoiceNumber: string, lease: object, invoiceDate: string, dueDate: string, totalAmount: number, status: string, payments: Array}>} Invoice details with payment line items
 * @throws {Error} When invoice not found or request fails
 * 
 * @example
 * const invoice = await getInvoiceById(1);
 * console.log(`Invoice: ${invoice.invoiceNumber}`);
 */
export const getInvoiceById = async (id) => {
  return await apiClient.get(`/invoices/${id}`);
};

/**
 * Retrieves an invoice by invoice number
 * 
 * @async
 * @function getInvoiceByNumber
 * @param {string} invoiceNumber - Invoice number (e.g., INV-20251113-1)
 * @returns {Promise<{id: number, invoiceNumber: string, lease: object, invoiceDate: string, dueDate: string, totalAmount: number, status: string, payments: Array}>} Invoice details with payment line items
 * @throws {Error} When invoice not found or request fails
 * 
 * @example
 * const invoice = await getInvoiceByNumber('INV-20251113-1');
 */
export const getInvoiceByNumber = async (invoiceNumber) => {
  return await apiClient.get(`/invoices/number/${invoiceNumber}`);
};

/**
 * Retrieves all invoices for a specific lease
 * 
 * @async
 * @function getInvoicesByLeaseId
 * @param {number} leaseId - Lease ID
 * @returns {Promise<Array<{id: number, invoiceNumber: string, invoiceDate: string, dueDate: string, totalAmount: number, status: string}>>} Array of invoices for the lease
 * @throws {Error} When fetching fails
 * 
 * @example
 * const invoices = await getInvoicesByLeaseId(1);
 * console.log(`${invoices.length} invoices for this lease`);
 */
export const getInvoicesByLeaseId = async (leaseId) => {
  return await apiClient.get(`/invoices/lease/${leaseId}`);
};
