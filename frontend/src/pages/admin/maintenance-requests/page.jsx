import React, { useState, useEffect } from 'react';
import { HiOutlineInbox } from 'react-icons/hi2';
import { FiSearch, FiFilter, FiCheckCircle, FiClock, FiTool, FiX, FiEdit, FiUser, FiTrash2, FiEye } from 'react-icons/fi';
import StatCard from '../../../components/card/stat_card';
import { getAllMaintenanceRequests, updateRequestStatus, assignMaintenanceRequest, updateRequestPriority, deleteMaintenanceRequest, updateMaintenanceRequest } from '../../../api/services/maintenance.service';
import { getBackendResourceUrl } from '../../../api/client/apiClient';

function MaintenanceRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [sortBy, setSortBy] = useState('newest');
  const [filterStatus, setFilterStatus] = useState('all');
  const [filterPriority, setFilterPriority] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showActionModal, setShowActionModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [actionType, setActionType] = useState(''); // 'status', 'priority', 'assign'
  const [actionValue, setActionValue] = useState('');
  const [actionNotes, setActionNotes] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [editFormData, setEditFormData] = useState({
    title: '',
    description: '',
    category: '',
    priority: ''
  });

  useEffect(() => {
    fetchRequests();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [requests, sortBy, filterStatus, filterPriority, searchQuery]);

  const fetchRequests = async () => {
    try {
      setIsLoading(true);
      const data = await getAllMaintenanceRequests();
      setRequests(data);
    } catch (error) {
      console.error('Error fetching maintenance requests:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const applyFiltersAndSort = () => {
    let filtered = [...requests];

    // Search filter
    if (searchQuery) {
      filtered = filtered.filter(req => 
        req.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        req.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        req.roomNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        req.tenantName?.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Status filter
    if (filterStatus !== 'all') {
      filtered = filtered.filter(req => req.status === filterStatus);
    }

    // Priority filter
    if (filterPriority !== 'all') {
      filtered = filtered.filter(req => req.priority === filterPriority);
    }

    // Sort
    filtered.sort((a, b) => {
      if (sortBy === 'newest') {
        return new Date(b.submittedDate) - new Date(a.submittedDate);
      } else if (sortBy === 'oldest') {
        return new Date(a.submittedDate) - new Date(b.submittedDate);
      } else if (sortBy === 'priority') {
        const priorityOrder = { URGENT: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
        return (priorityOrder[b.priority] || 0) - (priorityOrder[a.priority] || 0);
      }
      return 0;
    });

    setFilteredRequests(filtered);
  };

  const handleAction = (request, type) => {
    setSelectedRequest(request);
    setActionType(type);
    setShowActionModal(true);
    
    if (type === 'status') {
      setActionValue(request.status);
    } else if (type === 'priority') {
      setActionValue(request.priority);
    } else if (type === 'assign') {
      setActionValue(request.assignedToUserId || '');
    }
    setActionNotes('');
  };

  const handleSubmitAction = async () => {
    if (!selectedRequest) return;

    try {
      setIsProcessing(true);

      if (actionType === 'status') {
        await updateRequestStatus(selectedRequest.id, { 
          status: actionValue, 
          notes: actionNotes 
        });
      } else if (actionType === 'priority') {
        await updateRequestPriority(selectedRequest.id, { priority: actionValue });
      } else if (actionType === 'assign') {
        await assignMaintenanceRequest(selectedRequest.id, { 
          assignedToUserId: parseInt(actionValue) 
        });
      }

      await fetchRequests();
      setShowActionModal(false);
      setSelectedRequest(null);
    } catch (error) {
      console.error('Error performing action:', error);
      alert('Failed to perform action. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleEdit = (request) => {
    setSelectedRequest(request);
    setEditFormData({
      title: request.title,
      description: request.description,
      category: request.category,
      priority: request.priority
    });
    setShowEditModal(true);
  };

  const handleSubmitEdit = async () => {
    if (!selectedRequest) return;

    try {
      setIsProcessing(true);
      await updateMaintenanceRequest(selectedRequest.id, editFormData);
      await fetchRequests();
      setShowEditModal(false);
      setSelectedRequest(null);
    } catch (error) {
      console.error('Error updating maintenance request:', error);
      alert('Failed to update request. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDelete = (request) => {
    setSelectedRequest(request);
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = async () => {
    if (!selectedRequest) return;

    try {
      setIsProcessing(true);
      await deleteMaintenanceRequest(selectedRequest.id);
      await fetchRequests();
      setShowDeleteModal(false);
      setSelectedRequest(null);
    } catch (error) {
      console.error('Error deleting maintenance request:', error);
      alert('Failed to delete request. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      SUBMITTED: { color: 'bg-blue-100 text-blue-800', icon: FiClock, text: 'Submitted' },
      WAITING_FOR_REPAIR: { color: 'bg-yellow-100 text-yellow-800', icon: FiClock, text: 'Waiting' },
      APPROVED: { color: 'bg-green-100 text-green-800', icon: FiCheckCircle, text: 'Approved' },
      IN_PROGRESS: { color: 'bg-purple-100 text-purple-800', icon: FiTool, text: 'In Progress' },
      COMPLETED: { color: 'bg-gray-100 text-gray-800', icon: FiCheckCircle, text: 'Completed' },
      CANCELLED: { color: 'bg-red-100 text-red-800', icon: FiX, text: 'Cancelled' }
    };
    
    const config = statusConfig[status] || statusConfig.SUBMITTED;
    const Icon = config.icon;
    
    return (
      <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${config.color}`}>
        <Icon size={14} />
        {config.text}
      </span>
    );
  };

  const getPriorityBadge = (priority) => {
    const colors = {
      LOW: 'bg-gray-100 text-gray-700',
      MEDIUM: 'bg-blue-100 text-blue-700',
      HIGH: 'bg-orange-100 text-orange-700',
      URGENT: 'bg-red-100 text-red-700'
    };
    return (
      <span className={`px-2 py-1 rounded text-xs font-medium ${colors[priority] || colors.MEDIUM}`}>
        {priority}
      </span>
    );
  };

  const groupByStatus = () => {
    const groups = {
      pending: filteredRequests.filter(r => r.status === 'SUBMITTED' || r.status === 'WAITING_FOR_REPAIR'),
      approved: filteredRequests.filter(r => r.status === 'APPROVED' || r.status === 'IN_PROGRESS'),
      completed: filteredRequests.filter(r => r.status === 'COMPLETED'),
      cancelled: filteredRequests.filter(r => r.status === 'CANCELLED')
    };
    return groups;
  };

  const groups = groupByStatus();

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('th-TH', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const handleViewDetails = (request) => {
    setSelectedRequest(request);
    setShowDetailModal(true);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className='flex flex-col'>
      {/* Header */}
      <div className='flex w-full items-center lg:mb-8 mb-6 justify-between'>
        <div>
          <h1 className='title'>Maintenance Requests Management</h1>
          <p className='text-gray-600 mt-1'>Manage and track all maintenance requests</p>
        </div>
      </div>

      {/* Stats */}
      <div className='grid lg:grid-cols-4 grid-cols-2 lg:mb-6 mb-4 gap-4'>
        <StatCard icon={<FiClock />} title={"Pending"} value={groups.pending.length} color={"blue"} />
        <StatCard icon={<FiTool />} title={"In Progress"} value={groups.approved.length} color={"yellow"} />
        <StatCard icon={<FiCheckCircle />} title={"Completed"} value={groups.completed.length} color={"green"} />
        <StatCard icon={<FiX />} title={"Cancelled"} value={groups.cancelled.length} color={"red"} />
      </div>

      {/* Filters and Search */}
      <div className='bg-white p-4 rounded-lg shadow mb-6'>
        <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
          {/* Search */}
          <div className='relative'>
            <FiSearch className='absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400' />
            <input
              type="text"
              placeholder="Search requests..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
            />
          </div>

          {/* Status Filter */}
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className='border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
          >
            <option value="all">All Status</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="WAITING_FOR_REPAIR">Waiting for Repair</option>
            <option value="APPROVED">Approved</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          {/* Priority Filter */}
          <select
            value={filterPriority}
            onChange={(e) => setFilterPriority(e.target.value)}
            className='border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
          >
            <option value="all">All Priority</option>
            <option value="URGENT">Urgent</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          {/* Sort */}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className='border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
          >
            <option value="newest">Sort by: Newest</option>
            <option value="oldest">Sort by: Oldest</option>
            <option value="priority">Sort by: Priority</option>
          </select>
        </div>
      </div>

      {/* Requests Table */}
      <div className='bg-white rounded-lg shadow overflow-hidden'>
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-800">
            All Requests ({filteredRequests.length})
          </h2>
        </div>

        {filteredRequests.length === 0 ? (
          <div className='text-center py-12'>
            <HiOutlineInbox className='mx-auto text-6xl text-gray-300 mb-4' />
            <p className='text-gray-500 text-lg'>No maintenance requests found</p>
            <p className='text-gray-400 text-sm mt-2'>
              {searchQuery || filterStatus !== 'all' || filterPriority !== 'all' 
                ? 'Try adjusting your filters' 
                : "You're all caught up! New requests will appear here."}
            </p>
          </div>
        ) : (
          <div className='overflow-x-auto'>
            <table className="min-w-full divide-y divide-gray-200">
              <thead className='bg-gray-50'>
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Title</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Room</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tenant</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Category</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Submitted</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className='bg-white divide-y divide-gray-200'>
                {filteredRequests.map((request) => (
                  <tr key={request.id} className='hover:bg-gray-50'>
                    <td className="px-6 py-4">
                      <span className='font-medium text-gray-900'>{request.title}</span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {request.roomNumber || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {request.tenantName || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className='text-sm text-gray-600'>{request.category}</span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getPriorityBadge(request.priority)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(request.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(request.submittedDate)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={() => handleViewDetails(request)}
                        className='flex items-center gap-2 text-blue-600 hover:text-blue-800 font-medium'
                      >
                        <FiEye /> View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Detail/Action Modal */}
      {showDetailModal && selectedRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4'>
          <div className='bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto'>
            {/* Modal Header */}
            <div className='sticky top-0 bg-white border-b px-6 py-4 flex justify-between items-center'>
              <div>
                <h3 className='text-xl font-bold text-gray-800'>Request Details</h3>
                <p className='text-sm text-gray-600'>#{selectedRequest.id}</p>
              </div>
              <button
                onClick={() => setShowDetailModal(false)}
                className='text-gray-400 hover:text-gray-600 text-2xl font-bold'
              >
                ×
              </button>
            </div>

            {/* Modal Body */}
            <div className='p-6'>
              {/* Request Details */}
              <div className='grid grid-cols-2 gap-4 mb-6 bg-gray-50 p-4 rounded-lg'>
                <div>
                  <p className='text-sm text-gray-600'>Title</p>
                  <p className='font-semibold'>{selectedRequest.title}</p>
                </div>
                <div>
                  <p className='text-sm text-gray-600'>Status</p>
                  <div className='mt-1'>{getStatusBadge(selectedRequest.status)}</div>
                </div>
                <div>
                  <p className='text-sm text-gray-600'>Room</p>
                  <p className='font-semibold'>{selectedRequest.roomNumber || 'N/A'}</p>
                </div>
                <div>
                  <p className='text-sm text-gray-600'>Tenant</p>
                  <p className='font-semibold'>{selectedRequest.tenantName || 'N/A'}</p>
                </div>
                <div>
                  <p className='text-sm text-gray-600'>Category</p>
                  <p className='font-semibold'>{selectedRequest.category}</p>
                </div>
                <div>
                  <p className='text-sm text-gray-600'>Priority</p>
                  <div className='mt-1'>{getPriorityBadge(selectedRequest.priority)}</div>
                </div>
                <div className='col-span-2'>
                  <p className='text-sm text-gray-600 mb-2'>Description</p>
                  <p className='text-gray-700 bg-white p-3 rounded'>{selectedRequest.description}</p>
                </div>
                <div>
                  <p className='text-sm text-gray-600'>Submitted Date</p>
                  <p className='font-semibold'>{formatDate(selectedRequest.submittedDate)}</p>
                </div>
              </div>

              {/* Attachments */}
              {selectedRequest.attachmentUrls && selectedRequest.attachmentUrls.length > 0 && (
                <div className='mb-6'>
                  <h4 className='font-semibold text-gray-800 mb-3'>Attachments:</h4>
                  <div className='grid grid-cols-2 gap-4'>
                    {selectedRequest.attachmentUrls.map((url, index) => (
                      <div key={index} className='border rounded-lg overflow-hidden'>
                        <img 
                          src={getBackendResourceUrl(url)}
                          alt={`Attachment ${index + 1}`}
                          className='w-full h-48 object-cover bg-gray-50'
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className='flex gap-3 flex-wrap justify-end border-t pt-4'>
                <button
                  onClick={() => {
                    setShowDetailModal(false);
                    handleEdit(selectedRequest);
                  }}
                  className='flex items-center gap-2 px-4 py-2 bg-gray-500 hover:bg-gray-600 text-white rounded-lg'
                >
                  <FiEdit /> Edit
                </button>
                <button
                  onClick={() => {
                    setShowDetailModal(false);
                    handleAction(selectedRequest, 'priority');
                  }}
                  className='flex items-center gap-2 px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white rounded-lg'
                >
                  <FiEdit /> Change Priority
                </button>
                <button
                  onClick={() => {
                    setShowDetailModal(false);
                    handleAction(selectedRequest, 'assign');
                  }}
                  className='flex items-center gap-2 px-4 py-2 bg-purple-500 hover:bg-purple-600 text-white rounded-lg'
                >
                  <FiUser /> Assign Tech
                </button>
                <button
                  onClick={() => {
                    setShowDetailModal(false);
                    handleAction(selectedRequest, 'status');
                  }}
                  className='flex items-center gap-2 px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg'
                >
                  <FiCheckCircle /> Update Status
                </button>
                <button
                  onClick={() => {
                    setShowDetailModal(false);
                    handleDelete(selectedRequest);
                  }}
                  className='flex items-center gap-2 px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg'
                >
                  <FiTrash2 /> Delete
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Action Modal */}
      {showActionModal && selectedRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
          <div className='bg-white rounded-lg p-6 w-full max-w-md'>
            <h3 className='text-xl font-bold mb-4'>
              {actionType === 'status' && 'Update Status'}
              {actionType === 'priority' && 'Change Priority'}
              {actionType === 'assign' && 'Assign Technician'}
            </h3>
            
            <div className='mb-4'>
              <p className='text-sm text-gray-600 mb-2'>Request: <span className='font-medium'>{selectedRequest.title}</span></p>
              <p className='text-sm text-gray-600'>Room: <span className='font-medium'>{selectedRequest.roomNumber}</span></p>
            </div>

            {actionType === 'status' && (
              <div className='mb-4'>
                <label className='block text-sm font-medium mb-2'>New Status</label>
                <select
                  value={actionValue}
                  onChange={(e) => setActionValue(e.target.value)}
                  className='w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value="SUBMITTED">Submitted</option>
                  <option value="WAITING_FOR_REPAIR">Waiting for Repair</option>
                  <option value="APPROVED">Approved</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
                <label className='block text-sm font-medium mb-2 mt-4'>Notes (Optional)</label>
                <textarea
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  placeholder='Add notes about this status change...'
                  className='w-full border border-gray-300 rounded-lg px-4 py-2 min-h-[100px] focus:outline-none focus:ring-2 focus:ring-blue-500'
                />
              </div>
            )}

            {actionType === 'priority' && (
              <div className='mb-4'>
                <label className='block text-sm font-medium mb-2'>New Priority</label>
                <select
                  value={actionValue}
                  onChange={(e) => setActionValue(e.target.value)}
                  className='w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
              </div>
            )}

            {actionType === 'assign' && (
              <div className='mb-4'>
                <label className='block text-sm font-medium mb-2'>Technician ID</label>
                <input
                  type="number"
                  value={actionValue}
                  onChange={(e) => setActionValue(e.target.value)}
                  placeholder='Enter technician user ID'
                  className='w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
                />
              </div>
            )}

            <div className='flex gap-3 justify-end'>
              <button
                onClick={() => {
                  setShowActionModal(false);
                  setSelectedRequest(null);
                }}
                disabled={isProcessing}
                className='px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg font-medium transition-colors disabled:opacity-50'
              >
                Cancel
              </button>
              <button
                onClick={handleSubmitAction}
                disabled={isProcessing}
                className='px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition-colors disabled:opacity-50 flex items-center gap-2'
              >
                {isProcessing ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    Processing...
                  </>
                ) : (
                  'Confirm'
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {showEditModal && selectedRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
          <div className='bg-white rounded-lg p-6 w-full max-w-2xl'>
            <h3 className='text-xl font-bold mb-4 flex items-center gap-2'>
              <FiEdit />
              Edit Maintenance Request
            </h3>
            
            <div className='mb-4'>
              <label className='block text-sm font-medium mb-2'>Title *</label>
              <input
                type='text'
                value={editFormData.title}
                onChange={(e) => setEditFormData({...editFormData, title: e.target.value})}
                className='w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
                placeholder='Brief description of the issue'
              />
            </div>

            <div className='mb-4'>
              <label className='block text-sm font-medium mb-2'>Description *</label>
              <textarea
                value={editFormData.description}
                onChange={(e) => setEditFormData({...editFormData, description: e.target.value})}
                className='w-full border border-gray-300 rounded-lg px-4 py-2 min-h-[120px] focus:outline-none focus:ring-2 focus:ring-blue-500'
                placeholder='Detailed description of the problem'
              />
            </div>

            <div className='grid grid-cols-2 gap-4 mb-4'>
              <div>
                <label className='block text-sm font-medium mb-2'>Category *</label>
                <select
                  value={editFormData.category}
                  onChange={(e) => setEditFormData({...editFormData, category: e.target.value})}
                  className='w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value='PLUMBING'>Plumbing</option>
                  <option value='ELECTRICAL'>Electrical</option>
                  <option value='HVAC'>HVAC</option>
                  <option value='APPLIANCE'>Appliance</option>
                  <option value='STRUCTURAL'>Structural</option>
                  <option value='CLEANING'>Cleaning</option>
                  <option value='OTHER'>Other</option>
                </select>
              </div>

              <div>
                <label className='block text-sm font-medium mb-2'>Priority *</label>
                <select
                  value={editFormData.priority}
                  onChange={(e) => setEditFormData({...editFormData, priority: e.target.value})}
                  className='w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value='LOW'>🟢 Low - Can wait</option>
                  <option value='MEDIUM'>🟡 Medium - Should be fixed soon</option>
                  <option value='HIGH'>🟠 High - Needs attention</option>
                  <option value='URGENT'>🔴 Urgent - Fix immediately</option>
                </select>
              </div>
            </div>

            <div className='flex gap-3 justify-end'>
              <button
                onClick={() => {
                  setShowEditModal(false);
                  setSelectedRequest(null);
                }}
                disabled={isProcessing}
                className='px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg font-medium transition-colors disabled:opacity-50'
              >
                Cancel
              </button>
              <button
                onClick={handleSubmitEdit}
                disabled={isProcessing || !editFormData.title || !editFormData.description}
                className='px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition-colors disabled:opacity-50 flex items-center gap-2'
              >
                {isProcessing ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    Updating...
                  </>
                ) : (
                  <>
                    <FiCheckCircle />
                    Update Request
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && selectedRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
          <div className='bg-white rounded-lg p-6 w-full max-w-md'>
            <h3 className='text-xl font-bold mb-4 flex items-center gap-2 text-red-600'>
              <FiTrash2 />
              Delete Maintenance Request
            </h3>
            
            <div className='mb-6'>
              <p className='text-gray-700 mb-4'>
                Are you sure you want to delete this maintenance request?
              </p>
              <div className='bg-gray-50 p-4 rounded-lg'>
                <p className='text-sm font-medium mb-1'>{selectedRequest.title}</p>
                <p className='text-sm text-gray-600'>Room: {selectedRequest.roomNumber}</p>
                <p className='text-sm text-gray-600'>Tenant: {selectedRequest.tenantName}</p>
              </div>
              <p className='text-sm text-red-600 mt-4'>
                ⚠️ This action cannot be undone.
              </p>
            </div>

            <div className='flex gap-3 justify-end'>
              <button
                onClick={() => {
                  setShowDeleteModal(false);
                  setSelectedRequest(null);
                }}
                disabled={isProcessing}
                className='px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg font-medium transition-colors disabled:opacity-50'
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                disabled={isProcessing}
                className='px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg font-medium transition-colors disabled:opacity-50 flex items-center gap-2'
              >
                {isProcessing ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    Deleting...
                  </>
                ) : (
                  <>
                    <FiTrash2 />
                    Delete Request
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MaintenanceRequestsPage;