import React, { useState, useEffect, useRef } from 'react';
import { FiUpload, FiX, FiCheckCircle, FiClock, FiTool, FiFile, FiImage, FiSearch, FiFilter } from "react-icons/fi";
import { SiFormspree } from "react-icons/si";
import UserMaintenanceSkelleton from '../../../components/skeleton/user_maintenance_skelleton';
import { createMaintenanceRequest, getMyMaintenanceRequests, getAllMaintenanceRequests } from '../../../api/services/maintenance.service';
import { useAuth } from '../../../hooks/useAuth';
import apiClient from '../../../api/client/apiClient';
import { FcSupport } from "react-icons/fc";

function UserMaintenancePage() {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [myRequests, setMyRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [allRequests, setAllRequests] = useState([]);
  const [bookedTimeSlots, setBookedTimeSlots] = useState({});
  const fileInputRef = useRef(null);
  const [isDragging, setIsDragging] = useState(false);
  
  // Filter and search state
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  
  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'OTHER',
    priority: 'MEDIUM',
    preferredDate: '',
    preferredTimeSlot: '',
    preferredTime: '',
    unitId: null,
    tenantId: null
  });
  
  const [files, setFiles] = useState([]);
  const [errors, setErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    fetchMyRequests();
    fetchAllRequests();
  }, []);

  useEffect(() => {
    filterRequests();
  }, [myRequests, searchTerm, statusFilter, categoryFilter]);

  useEffect(() => {
    // Update booked time slots when date changes
    if (formData.preferredDate) {
      updateBookedTimeSlots(formData.preferredDate);
    }
  }, [formData.preferredDate, allRequests]);

  const fetchMyRequests = async () => {
    try {
      setIsLoading(true);
      const requests = await getMyMaintenanceRequests();
      setMyRequests(requests);
      setFilteredRequests(requests);
    } catch (error) {
      console.error('Error fetching maintenance requests:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchAllRequests = async () => {
    try {
      const requests = await getAllMaintenanceRequests();
      // Filter only non-cancelled and non-completed requests
      const activeRequests = requests.filter(req => 
        req.status !== 'CANCELLED' && 
        req.status !== 'COMPLETED' &&
        req.preferredTime
      );
      setAllRequests(activeRequests);
    } catch (error) {
      console.error('Error fetching all requests:', error);
    }
  };

  const updateBookedTimeSlots = (selectedDate) => {
    const booked = {};
    
    allRequests.forEach(req => {
      if (!req.preferredTime) return;
      
      // Extract date and time from preferredTime
      const dateMatch = req.preferredTime.match(/(\d{4}-\d{2}-\d{2})/);
      const timeMatch = req.preferredTime.match(/(\d{2}:\d{2})/);
      
      if (dateMatch && timeMatch) {
        const reqDate = dateMatch[1];
        const reqTime = timeMatch[1];
        
        if (reqDate === selectedDate) {
          // Map time to time slot
          const hour = parseInt(reqTime.split(':')[0]);
          let slot = '';
          
          if (hour >= 8 && hour < 10) slot = '08:00-10:00';
          else if (hour >= 10 && hour < 13) slot = '10:00-12:00';
          else if (hour >= 13 && hour < 15) slot = '13:00-15:00';
          else if (hour >= 15 && hour < 17) slot = '15:00-17:00';
          else if (hour >= 17 && hour < 19) slot = '17:00-19:00';
          
          if (slot) {
            if (!booked[slot]) booked[slot] = 0;
            booked[slot]++;
          }
        }
      }
    });
    
    setBookedTimeSlots(booked);
  };

  const filterRequests = () => {
    let filtered = [...myRequests];

    // Apply search filter
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(request => 
        request.title?.toLowerCase().includes(term) ||
        request.description?.toLowerCase().includes(term) ||
        request.roomNumber?.toLowerCase().includes(term) ||
        request.category?.toLowerCase().includes(term)
      );
    }

    // Apply status filter
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(request => request.status === statusFilter);
    }

    // Apply category filter
    if (categoryFilter !== 'ALL') {
      filtered = filtered.filter(request => request.category === categoryFilter);
    }

    setFilteredRequests(filtered);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => {
      const updated = { ...prev, [name]: value };
      
      // Combine date and time slot into preferredTime for backend
      if (name === 'preferredDate' || name === 'preferredTimeSlot') {
        if (updated.preferredDate && updated.preferredTimeSlot) {
          updated.preferredTime = `${updated.preferredDate} ${updated.preferredTimeSlot}`;
        } else if (updated.preferredDate) {
          updated.preferredTime = updated.preferredDate;
        } else {
          updated.preferredTime = '';
        }
      }
      
      return updated;
    });
    
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleFileSelect = (selectedFiles) => {
    const fileArray = Array.from(selectedFiles);
    const validFiles = [];
    let totalSize = files.reduce((sum, f) => sum + f.size, 0);
    let errorMsg = '';

    for (const file of fileArray) {
      // Check file type
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'application/pdf'];
      if (!validTypes.includes(file.type)) {
        errorMsg = 'Only images (JPG, PNG, GIF) and PDF files are allowed';
        continue;
      }

      // Check total size
      if (totalSize + file.size > 15 * 1024 * 1024) {
        errorMsg = 'Total file size should not exceed 15MB';
        break;
      }

      totalSize += file.size;
      validFiles.push({
        file,
        id: Date.now() + Math.random(),
        name: file.name,
        size: file.size,
        type: file.type,
        preview: file.type.startsWith('image/') ? URL.createObjectURL(file) : null
      });
    }

    if (errorMsg) {
      setErrors(prev => ({ ...prev, files: errorMsg }));
    } else {
      setErrors(prev => ({ ...prev, files: '' }));
    }

    setFiles(prev => [...prev, ...validFiles]);
  };

  const handleFileInput = (e) => {
    if (e.target.files.length > 0) {
      handleFileSelect(e.target.files);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    
    if (e.dataTransfer.files.length > 0) {
      handleFileSelect(e.dataTransfer.files);
    }
  };

  const removeFile = (fileId) => {
    setFiles(prev => {
      const filtered = prev.filter(f => f.id !== fileId);
      // Revoke URL to prevent memory leak
      const removed = prev.find(f => f.id === fileId);
      if (removed?.preview) {
        URL.revokeObjectURL(removed.preview);
      }
      return filtered;
    });
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const getTotalSize = () => {
    const total = files.reduce((sum, f) => sum + f.size, 0);
    return formatFileSize(total);
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.title.trim()) newErrors.title = 'Title is required';
    if (!formData.description.trim()) newErrors.description = 'Description is required';
    if (!user) {
      newErrors.unitId = 'Please log in to submit a maintenance request';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrors({});
      
      // Map priority to urgency enum values (backend expects: EMERGENCY, HIGH, LOW, MEDIUM)
      const priorityToUrgencyMap = {
        'LOW': 'LOW',
        'MEDIUM': 'MEDIUM',
        'HIGH': 'HIGH',
        'URGENT': 'EMERGENCY' // Map URGENT to EMERGENCY
      };
      
      // Only send fields that backend expects
      const requestData = {
        title: formData.title,
        description: formData.description,
        category: formData.category,
        priority: formData.priority,
        urgency: priorityToUrgencyMap[formData.priority] || 'MEDIUM', // Map priority to valid urgency value
        preferredTime: formData.preferredTime || null,
        unitId: user?.unitId || null, // Can be null if user not assigned to unit yet
        tenantId: user?.tenantId || null, // Use actual tenantId from user profile, or null
        createdByUserId: user?.id // Track who created this request
      };
      
      console.log('📤 Sending to backend:', requestData);

      // Create the maintenance request first
      const createdRequest = await createMaintenanceRequest(requestData);
      
      // If there are files, upload them
      if (files.length > 0 && createdRequest.id) {
        const formDataWithFiles = new FormData();
        files.forEach(fileObj => {
          formDataWithFiles.append('files', fileObj.file);
        });

        // Upload files using apiClient (includes auth token)
        try {
          await apiClient.post(`/maintenance-requests/${createdRequest.id}/upload-attachments`, formDataWithFiles);
        } catch (uploadError) {
          console.error('Error uploading files:', uploadError);
          // Don't fail the whole request if file upload fails
          setErrors({ submit: 'Request created but some files failed to upload' });
        }
      }
      
      setSuccessMessage('Maintenance request submitted successfully!');
      
      // Reset form
      setFormData({
        title: '',
        description: '',
        category: 'OTHER',
        priority: 'MEDIUM',
        preferredDate: '',
        preferredTimeSlot: '',
        preferredTime: '',
        unitId: user?.unitId || null,
        tenantId: user?.tenantId || null
      });
      // Clean up file previews
      files.forEach(f => {
        if (f.preview) URL.revokeObjectURL(f.preview);
      });
      setFiles([]);
      
      // Refresh requests list
      fetchMyRequests();
      
      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (error) {
      console.error('Error submitting maintenance request:', error);
      setErrors({ submit: 'Failed to submit request. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      title: '',
      description: '',
      category: 'OTHER',
      priority: 'MEDIUM',
      preferredDate: '',
      preferredTimeSlot: '',
      preferredTime: '',
      unitId: user?.unitId || null,
      tenantId: user?.tenantId || null
    });
    // Clean up file previews
    files.forEach(f => {
      if (f.preview) URL.revokeObjectURL(f.preview);
    });
    setFiles([]);
    setErrors({});
    setSuccessMessage('');
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

  if (isLoading) {
    return <UserMaintenanceSkelleton />;
  }

  return (
    <div className='flex flex-col'>
      {/* Success Message */}
      {successMessage && (
        <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2 text-green-800">
          <FiCheckCircle size={20} />
          <span>{successMessage}</span>
        </div>
      )}

      {/* Error Message */}
      {errors.submit && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-800">
          <FiX size={20} />
          <span>{errors.submit}</span>
        </div>
      )}

      {/* Unit ID Error - Only show if form was submitted without authentication */}
      {errors.unitId && !user && (
        <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg flex items-center gap-2 text-yellow-800">
          <FiX size={20} />
          <span>{errors.unitId}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className='grid lg:grid-cols-2 grid-cols-1 gap-6 mb-8'>
          {/* Upload Files with Drag & Drop */}
          <div className='bg-white p-6 shadow-md rounded-lg min-h-[600px] flex flex-col'>
            <h3 className='text-lg font-semibold mb-4 flex items-center gap-2'>
              <FiUpload />
              Upload Files
            </h3>
            
            {/* Drop Zone */}
            <div
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              className={`flex-1 flex flex-col border-2 border-dashed rounded-lg transition-colors ${
                isDragging 
                  ? 'border-blue-500 bg-blue-50' 
                  : 'border-gray-300 bg-gray-50'
              }`}
            >
              {files.length === 0 ? (
                <div className='flex-1 flex flex-col items-center justify-center p-8 space-y-4'>
                  <div className='bg-gray-200 p-12 rounded-full'>
                    <FiUpload size={32} className='text-gray-600' />
                  </div>
                  <div className='text-xl font-medium text-center text-gray-700'>
                    Drag & Drop files here
                  </div>
                  <p className='text-sm text-gray-500 text-center'>
                    or click to browse
                  </p>
                  <p className='text-xs text-gray-400 text-center'>
                    Images (JPG, PNG, GIF) and PDF files<br />
                    Maximum 15MB total
                  </p>
                  <input
                    ref={fileInputRef}
                    type="file"
                    multiple
                    accept="image/*,.pdf"
                    onChange={handleFileInput}
                    className='hidden'
                  />
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className='bg-blue-400 hover:bg-blue-500 text-white font-medium py-2 px-6 rounded-full hover:translate-y-[-1px] shadow-md hover:shadow-lg transition-all duration-300'
                  >
                    Choose Files
                  </button>
                </div>
              ) : (
                <div className='flex-1 flex flex-col p-4'>
                  <div className='flex justify-between items-center mb-3'>
                    <span className='text-sm font-medium text-gray-700'>
                      {files.length} file(s) - {getTotalSize()}
                    </span>
                    <button
                      type="button"
                      onClick={() => fileInputRef.current?.click()}
                      className='text-sm text-blue-500 hover:text-blue-600 font-medium'
                    >
                      + Add More
                    </button>
                    <input
                      ref={fileInputRef}
                      type="file"
                      multiple
                      accept="image/*,.pdf"
                      onChange={handleFileInput}
                      className='hidden'
                    />
                  </div>
                  
                  <div className='flex-1 overflow-y-auto space-y-2'>
                    {files.map((file) => (
                      <div 
                        key={file.id}
                        className='flex items-center gap-3 p-3 bg-white border border-gray-200 rounded-lg hover:shadow-md transition-shadow'
                      >
                        {file.preview ? (
                          <img 
                            src={file.preview} 
                            alt={file.name}
                            className='w-16 h-16 object-cover rounded'
                          />
                        ) : (
                          <div className='w-16 h-16 bg-red-100 rounded flex items-center justify-center'>
                            <FiFile size={24} className='text-red-600' />
                          </div>
                        )}
                        <div className='flex-1 min-w-0'>
                          <p className='text-sm font-medium text-gray-700 truncate'>
                            {file.name}
                          </p>
                          <p className='text-xs text-gray-500'>
                            {formatFileSize(file.size)}
                          </p>
                        </div>
                        <button
                          type="button"
                          onClick={() => removeFile(file.id)}
                          className='p-2 hover:bg-red-50 rounded-full transition-colors'
                        >
                          <FiX size={18} className='text-red-500' />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
            
            {errors.files && (
              <p className='text-red-500 text-sm mt-2'>{errors.files}</p>
            )}
          </div>
          
          {/* Maintenance Form */}
          <div className='bg-white p-6 shadow-md rounded-lg min-h-[600px]'>
            <div className='text-xl font-medium flex gap-4 items-center mb-6'>
              <SiFormspree size={24} />
              Maintenance Request Form
            </div>

            <div className='space-y-4 mb-6'>
              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Title <span className='text-red-500'>*</span>
                </label>
                <input 
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleInputChange}
                  placeholder="e.g., Leaking faucet in kitchen"
                  className={`border rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.title ? 'border-red-500' : 'border-gray-400'
                  }`}
                />
                {errors.title && <p className='text-red-500 text-sm mt-1'>{errors.title}</p>}
              </div>

              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Category <span className='text-red-500'>*</span>
                </label>
                <select
                  name="category"
                  value={formData.category}
                  onChange={handleInputChange}
                  className='border border-gray-400 rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value="PLUMBING">Plumbing</option>
                  <option value="ELECTRICAL">Electrical</option>
                  <option value="HVAC">HVAC</option>
                  <option value="APPLIANCE">Appliance</option>
                  <option value="STRUCTURAL">Structural</option>
                  <option value="CLEANING">Cleaning</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Priority <span className='text-red-500'>*</span>
                  <span className='text-xs text-gray-500 ml-2 font-normal'>
                    (How important is this issue?)
                  </span>
                </label>
                <select
                  name="priority"
                  value={formData.priority}
                  onChange={handleInputChange}
                  className='border border-gray-400 rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value="LOW">Low - Can wait, not urgent</option>
                  <option value="MEDIUM">Medium - Should be fixed soon</option>
                  <option value="HIGH">High - Important, needs attention</option>
                  <option value="URGENT">Urgent - Critical, fix immediately</option>
                </select>
              </div>

              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Preferred Date & Time
                </label>
                <div className='grid grid-cols-2 gap-3'>
                  <div>
                    <label className='text-xs text-gray-600 mb-1 block'>Date</label>
                    <input 
                      type="date"
                      name="preferredDate"
                      value={formData.preferredDate || ''}
                      onChange={handleInputChange}
                      min={new Date().toISOString().split('T')[0]}
                      className='border border-gray-400 rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500'
                    />
                  </div>
                  <div>
                    <label className='text-xs text-gray-600 mb-1 block'>Time</label>
                    <select
                      name="preferredTimeSlot"
                      value={formData.preferredTimeSlot || ''}
                      onChange={handleInputChange}
                      disabled={!formData.preferredDate}
                      className='border border-gray-400 rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed'
                    >
                      <option value="">Select time</option>
                      {[
                        { value: '08:00-10:00', label: '08:00 - 10:00 AM' },
                        { value: '10:00-12:00', label: '10:00 - 12:00 PM' },
                        { value: '13:00-15:00', label: '01:00 - 03:00 PM' },
                        { value: '15:00-17:00', label: '03:00 - 05:00 PM' },
                        { value: '17:00-19:00', label: '05:00 - 07:00 PM' }
                      ].map(slot => {
                        const bookedCount = bookedTimeSlots[slot.value] || 0;
                        const isFullyBooked = bookedCount >= 1; // Lock after 1 booking
                        
                        return (
                          <option 
                            key={slot.value} 
                            value={slot.value}
                            disabled={isFullyBooked}
                          >
                            {slot.label} {bookedCount > 0 ? '(Booked - Unavailable)' : '(Available)'}
                          </option>
                        );
                      })}
                    </select>
                  </div>
                </div>
                <p className='text-xs text-gray-500 mt-2'>
                  {!formData.preferredDate 
                    ? 'Select a date first to see available time slots'
                    : '⚠️ Each time slot allows only 1 booking - select an available slot'}
                </p>
              </div>

              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Description <span className='text-red-500'>*</span>
                </label>
                <textarea 
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  placeholder="Please describe the issue in detail..."
                  className={`w-full border rounded-lg px-4 py-2 min-h-[120px] focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.description ? 'border-red-500' : 'border-gray-400'
                  }`}
                />
                {errors.description && <p className='text-red-500 text-sm mt-1'>{errors.description}</p>}
              </div>
            </div>
            
            <div className='w-full border-b border-gray-300 mb-6'></div>

            <div className='flex justify-end gap-4'>
              <button 
                type="button"
                onClick={handleCancel}
                disabled={isSubmitting}
                className='bg-gray-200 hover:bg-gray-300 rounded-lg px-6 py-2 text-gray-600 font-medium shadow-md hover:translate-y-[-1px] hover:shadow-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed'
              >
                Cancel
              </button>
              <button 
                type="submit"
                disabled={isSubmitting}
                className='bg-blue-400 hover:bg-blue-500 rounded-lg px-6 py-2 text-white font-medium shadow-md hover:translate-y-[-1px] hover:shadow-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2'
              >
                {isSubmitting ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    Submitting...
                  </>
                ) : (
                  'Submit Request'
                )}
              </button>
            </div>
          </div>
        </div>
      </form>

      {/* My Maintenance Requests History */}
      <div className='bg-white p-6 shadow-md rounded-lg mt-6'>
        <h2 className='text-2xl font-medium mb-6'>My Maintenance Requests</h2>
        
        {/* Search and Filter Controls */}
        {myRequests.length > 0 && (
          <div className='mb-6 grid grid-cols-1 md:grid-cols-3 gap-4'>
            {/* Search Box */}
            <div className='md:col-span-1'>
              <div className='relative'>
                <FiSearch className='absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400' size={18} />
                <input
                  type="text"
                  placeholder="Search requests..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className='w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                />
              </div>
            </div>

            {/* Status Filter */}
            <div className='md:col-span-1'>
              <div className='relative'>
                <FiFilter className='absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400' size={18} />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className='w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none bg-white'
                >
                  <option value="ALL">All Status</option>
                  <option value="SUBMITTED">Submitted</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>
            </div>

            {/* Category Filter */}
            <div className='md:col-span-1'>
              <select
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
                className='w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none bg-white'
              >
                <option value="ALL">All Categories</option>
                <option value="PLUMBING">Plumbing</option>
                <option value="ELECTRICAL">Electrical</option>
                <option value="HVAC">HVAC</option>
                <option value="APPLIANCE">Appliance</option>
                <option value="STRUCTURAL">Structural</option>
                <option value="CLEANING">Cleaning</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
        )}

        {/* Results Summary */}
        {myRequests.length > 0 && (
          <div className='mb-4 text-sm text-gray-600'>
            Showing {filteredRequests.length} of {myRequests.length} request{myRequests.length !== 1 ? 's' : ''}
          </div>
        )}
        
        {myRequests.length === 0 ? (
          <div className="flex flex-col justify-center items-center min-h-[300px]">
            <div className="p-12 bg-gray-200 rounded-full flex items-center justify-center mb-6">
              <FcSupport className="h-16 w-16" />
            </div>
            <h3 className="text-xl font-medium text-gray-900 mb-2">
              No maintenance requests yet
            </h3>
            <p className="text-gray-500">
              Your submitted requests will appear here
            </p>
          </div>
        ) : filteredRequests.length === 0 ? (
          <div className="flex flex-col justify-center items-center min-h-[300px]">
            <div className="p-12 bg-gray-200 rounded-full flex items-center justify-center mb-6">
              <FiSearch className="h-16 w-16 text-gray-400" />
            </div>
            <h3 className="text-xl font-medium text-gray-900 mb-2">
              No matching requests found
            </h3>
            <p className="text-gray-500">
              Try adjusting your search or filters
            </p>
          </div>
        ) : (
          <div className='space-y-4'>
            {filteredRequests.map((request) => (
              <div 
                key={request.id}
                className='border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow'
              >
                <div className='flex justify-between items-start mb-2'>
                  <div className='flex-1'>
                    <h3 className='font-semibold text-lg'>{request.title}</h3>
                    <p className='text-sm text-gray-600 mt-1'>{request.description}</p>
                  </div>
                  <div className='flex flex-col items-end gap-2'>
                    {getStatusBadge(request.status)}
                    {getPriorityBadge(request.priority)}
                  </div>
                </div>
                <div className='flex gap-4 text-sm text-gray-500 mt-3'>
                  <span>Category: <span className='font-medium'>{request.category}</span></span>
                  <span>•</span>
                  <span>Submitted: {new Date(request.submittedDate).toLocaleDateString()}</span>
                  {request.completedDate && (
                    <>
                      <span>•</span>
                      <span>Completed: {new Date(request.completedDate).toLocaleDateString()}</span>
                    </>
                  )}
                </div>
                {request.attachmentUrls && (
                  <div className='mt-3 flex gap-2 flex-wrap'>
                    {request.attachmentUrls.split(',').map((url, idx) => (
                      <a
                        key={idx}
                        href={url.trim()}
                        target="_blank"
                        rel="noopener noreferrer"
                        className='inline-flex items-center gap-1 px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs hover:bg-blue-100 transition-colors'
                      >
                        {url.trim().toLowerCase().endsWith('.pdf') ? <FiFile size={14} /> : <FiImage size={14} />}
                        Attachment {idx + 1}
                      </a>
                    ))}
                  </div>
                )}
                {request.completionNotes && (
                  <div className='mt-3 p-3 bg-gray-50 rounded text-sm'>
                    <span className='font-medium'>Notes: </span>
                    {request.completionNotes}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default UserMaintenancePage;