import React, { useState, useEffect } from 'react';
import StatCard from '../../../components/card/stat_card'
import { MdAttachMoney, MdOutlinePending, MdOutlineTrendingUp } from "react-icons/md";
import { IoBanOutline } from "react-icons/io5";
import { Link } from 'react-router-dom'
import PaymentsPageSkeleton from '../../../components/skeleton/payments_page_skeleton';
import { getWaitingVerificationInvoices, verifyPayment } from '../../../api/services/invoices.service';
import { getBackendResourceUrl } from '../../../api/client/apiClient';
import { FaCheck, FaTimes, FaEye, FaFileInvoice } from 'react-icons/fa';

function PaymentsPage() {
  const [loading, setLoading] = useState(true);
  const [invoices, setInvoices] = useState([]);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [verificationNotes, setVerificationNotes] = useState('');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    fetchPendingInvoices();
  }, []);

  const fetchPendingInvoices = async () => {
    try {
      setLoading(true);
      const data = await getWaitingVerificationInvoices();
      setInvoices(data);
    } catch (error) {
      console.error('Failed to fetch pending invoices:', error);
      alert('Failed to load pending payments');
    } finally {
      setLoading(false);
    }
  };

  const handleViewSlip = (invoice) => {
    setSelectedInvoice(invoice);
    setShowModal(true);
    setVerificationNotes('');
  };

  const handleVerify = async (approved) => {
    if (!selectedInvoice) return;
    
    try {
      setProcessing(true);
      await verifyPayment(selectedInvoice.id, approved, verificationNotes);
      alert(`Payment ${approved ? 'approved' : 'rejected'} successfully!`);
      setShowModal(false);
      setSelectedInvoice(null);
      fetchPendingInvoices(); // Refresh list
    } catch (error) {
      console.error('Verification failed:', error);
      alert('Failed to verify payment. Please try again.');
    } finally {
      setProcessing(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('th-TH', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('th-TH', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  };

  if (loading) {
    return <PaymentsPageSkeleton />;
  }

  return (
    <div className='flex flex-col'>
        {/* Header */}
        <div className='flex w-full items-center lg:mb-8 mb-6 justify-between'>
            <div>
                <h1 className='title'>Payment Verification</h1>
                <p className='text-gray-600 mt-1'>Review and verify payment slips</p>
            </div>
            <Link to="/admin/billing" className='btn bg-blue-500 text-white lg:px-6 px-4 lg:py-3 py-2 rounded-lg hover:translate-y-[-1px] hover:shadow-md'>
                Generate Bill
            </Link>
        </div>

        {/* Stats */}
        <div className='grid lg:grid-cols-4 grid-cols-1 lg:mb-6 mb-4 gap-4'>
            <StatCard icon={<MdOutlinePending />} title={"Waiting Verification"} value={invoices.length} color={"yellow"} />
            <StatCard icon={<MdAttachMoney />} title={"Paid this month"} value={`-`} color={"green"} />
            <StatCard icon={<IoBanOutline />} title={"Rejected"} value={`-`} color={"red"} />
            <StatCard icon={<MdOutlineTrendingUp />} title={"Total Revenue"} value={`-`} color={"blue"}/>
        </div>

        {/* Invoices Table */}
        <div className='bg-white rounded-lg shadow overflow-hidden'>
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800">Pending Verifications ({invoices.length})</h2>
          </div>

          {invoices.length === 0 ? (
            <div className='text-center py-12'>
              <FaFileInvoice className='mx-auto text-6xl text-gray-300 mb-4' />
              <p className='text-gray-500 text-lg'>No payments waiting for verification</p>
            </div>
          ) : (
            <div className='overflow-x-auto'>
              <table className="min-w-full divide-y divide-gray-200">
                <thead className='bg-gray-50'>
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Invoice #</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Unit</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tenant</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Uploaded</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody className='bg-white divide-y divide-gray-200'>
                  {invoices.map((invoice) => (
                    <tr key={invoice.id} className='hover:bg-gray-50'>
                      <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                        {invoice.invoiceNumber}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {invoice.lease?.unit?.roomNumber || 'N/A'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {invoice.lease?.tenant ? 
                          `${invoice.lease.tenant.firstName} ${invoice.lease.tenant.lastName}` : 'N/A'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap font-semibold text-blue-600">
                        ฿{formatCurrency(invoice.totalAmount)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(invoice.slipUploadedAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <button
                          onClick={() => handleViewSlip(invoice)}
                          className='flex items-center gap-2 text-blue-600 hover:text-blue-800 font-medium'
                        >
                          <FaEye /> View Slip
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Verification Modal */}
        {showModal && selectedInvoice && (
          <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4'>
            <div className='bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto'>
              {/* Modal Header */}
              <div className='sticky top-0 bg-white border-b px-6 py-4 flex justify-between items-center'>
                <div>
                  <h3 className='text-xl font-bold text-gray-800'>Verify Payment</h3>
                  <p className='text-sm text-gray-600'>Invoice: {selectedInvoice.invoiceNumber}</p>
                </div>
                <button
                  onClick={() => setShowModal(false)}
                  className='text-gray-400 hover:text-gray-600 text-2xl font-bold'
                >
                  ×
                </button>
              </div>

              {/* Modal Body */}
              <div className='p-6'>
                {/* Invoice Details */}
                <div className='grid grid-cols-2 gap-4 mb-6 bg-gray-50 p-4 rounded-lg'>
                  <div>
                    <p className='text-sm text-gray-600'>Unit</p>
                    <p className='font-semibold'>{selectedInvoice.lease?.unit?.roomNumber || 'N/A'}</p>
                  </div>
                  <div>
                    <p className='text-sm text-gray-600'>Tenant</p>
                    <p className='font-semibold'>
                      {selectedInvoice.lease?.tenant ? 
                        `${selectedInvoice.lease.tenant.firstName} ${selectedInvoice.lease.tenant.lastName}` : 'N/A'}
                    </p>
                  </div>
                  <div>
                    <p className='text-sm text-gray-600'>Amount</p>
                    <p className='font-semibold text-blue-600 text-lg'>฿{formatCurrency(selectedInvoice.totalAmount)}</p>
                  </div>
                  <div>
                    <p className='text-sm text-gray-600'>Uploaded At</p>
                    <p className='font-semibold'>{formatDate(selectedInvoice.slipUploadedAt)}</p>
                  </div>
                </div>

                {/* Payment Slip Image */}
                <div className='mb-6'>
                  <h4 className='font-semibold text-gray-800 mb-3'>Payment Slip:</h4>
                  {selectedInvoice.slipUrl ? (
                    <div className='border rounded-lg overflow-hidden'>
                      <img 
                        src={getBackendResourceUrl(selectedInvoice.slipUrl)}
                        alt="Payment Slip"
                        className='w-full h-auto max-h-[500px] object-contain bg-gray-50'
                      />
                    </div>
                  ) : (
                    <p className='text-gray-500'>No slip image available</p>
                  )}
                </div>

                {/* Verification Notes */}
                <div className='mb-6'>
                  <label className='block text-sm font-medium text-gray-700 mb-2'>
                    Verification Notes (Optional)
                  </label>
                  <textarea
                    value={verificationNotes}
                    onChange={(e) => setVerificationNotes(e.target.value)}
                    rows={3}
                    className='w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none'
                    placeholder='Add notes about this verification...'
                  />
                </div>

                {/* Action Buttons */}
                <div className='flex gap-4 justify-end'>
                  <button
                    onClick={() => setShowModal(false)}
                    disabled={processing}
                    className='px-6 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50'
                  >
                    Cancel
                  </button>
                  <button
                    onClick={() => handleVerify(false)}
                    disabled={processing}
                    className='flex items-center gap-2 px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50'
                  >
                    <FaTimes /> Reject
                  </button>
                  <button
                    onClick={() => handleVerify(true)}
                    disabled={processing}
                    className='flex items-center gap-2 px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50'
                  >
                    <FaCheck /> Approve
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
    </div>
  )
}

export default PaymentsPage