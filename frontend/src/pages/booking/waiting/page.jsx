/**
 * Booking Waiting Page
 * 
 * Displays pending booking request status and polls for updates.
 * Automatically redirects when status changes to approved or rejected.
 * 
 * @module pages/booking/waiting
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBookingStatus } from '../../../hooks/useBookingStatus';

/**
 * Waiting page for pending booking requests
 * Polls the backend every 30 seconds to check for status updates
 */
function WaitingPage() {
  const navigate = useNavigate();
  const [showSuccess, setShowSuccess] = useState(false);
  
  // Use booking status hook with 30-second polling
  const { 
    loading, 
    status, 
    startPolling, 
    stopPolling 
  } = useBookingStatus({ 
    autoFetch: true,
    pollingInterval: 30000 // 30 seconds
  });

  /**
   * Handle status changes from polling
   * Redirect based on new status
   */
  useEffect(() => {
    if (!status) return;

    // If approved, show success message then redirect to dashboard
    if (status.isApproved) {
      setShowSuccess(true);
      stopPolling();
      
      // Redirect after 3 seconds to let user see success message
      setTimeout(() => {
        navigate('/user/dashboard');
      }, 3000);
    }

    // If rejected or needs acknowledgement, redirect to booking page
    // This will trigger the rejection modal
    if (status.isRejected || status.requiresAcknowledgement) {
      stopPolling();
      navigate('/booking');
    }

    // If no pending request, redirect to booking page
    if (!status.isPending && !status.isApproved) {
      stopPolling();
      navigate('/booking');
    }
  }, [status, navigate, stopPolling]);

  /**
   * Start polling on mount
   */
  useEffect(() => {
    startPolling();

    // Cleanup: stop polling on unmount
    return () => {
      stopPolling();
    };
  }, [startPolling, stopPolling]);

  /**
   * Prevent browser back button
   */
  useEffect(() => {
    const handleBeforeUnload = (e) => {
      e.preventDefault();
      e.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, []);

  /**
   * Show loading state
   */
  if (loading && !status) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <svg 
            className="animate-spin h-12 w-12 text-blue-600 mx-auto mb-4" 
            xmlns="http://www.w3.org/2000/svg" 
            fill="none" 
            viewBox="0 0 24 24"
          >
            <circle 
              className="opacity-25" 
              cx="12" 
              cy="12" 
              r="10" 
              stroke="currentColor" 
              strokeWidth="4"
            />
            <path 
              className="opacity-75" 
              fill="currentColor" 
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <p className="text-gray-600">Loading your request...</p>
        </div>
      </div>
    );
  }

  /**
   * Show success message (before redirect to dashboard)
   */
  if (showSuccess) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-green-50 to-blue-50">
        <div className="bg-white rounded-lg shadow-2xl p-8 max-w-md mx-4 text-center">
          <div className="flex justify-center mb-6">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center animate-bounce">
              <svg 
                className="w-10 h-10 text-green-600" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                  strokeWidth={2} 
                  d="M5 13l4 4L19 7" 
                />
              </svg>
            </div>
          </div>
          
          <h2 className="text-3xl font-bold text-gray-900 mb-3">
            Congratulations! 🎉
          </h2>
          <p className="text-lg text-gray-600 mb-4">
            Your booking has been approved!
          </p>
          <p className="text-sm text-gray-500">
            Redirecting to your dashboard...
          </p>
        </div>
      </div>
    );
  }

  /**
   * Main waiting view
   */
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 py-12 px-4">
      <div className="max-w-3xl mx-auto">
        {/* Header Card */}
        <div className="bg-white rounded-lg shadow-lg p-8 mb-6">
          <div className="text-center mb-6">
            <div className="flex justify-center mb-4">
              <div className="relative">
                <div className="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center">
                  <svg 
                    className="w-10 h-10 text-blue-600" 
                    fill="none" 
                    stroke="currentColor" 
                    viewBox="0 0 24 24"
                  >
                    <path 
                      strokeLinecap="round" 
                      strokeLinejoin="round" 
                      strokeWidth={2} 
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" 
                    />
                  </svg>
                </div>
                {/* Pulse animation */}
                <div className="absolute inset-0 w-20 h-20 bg-blue-400 rounded-full animate-ping opacity-20"></div>
              </div>
            </div>
            
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              Request Under Review
            </h1>
            <p className="text-lg text-gray-600">
              Your booking request is being processed
            </p>
          </div>

          {/* Status Message */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
            <div className="flex items-start gap-3">
              <svg 
                className="w-6 h-6 text-blue-600 flex-shrink-0 mt-0.5" 
                fill="currentColor" 
                viewBox="0 0 20 20"
              >
                <path 
                  fillRule="evenodd" 
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" 
                  clipRule="evenodd" 
                />
              </svg>
              <div>
                <p className="font-medium text-blue-900 mb-1">
                  {status?.statusMessage || 'Pending approval'}
                </p>
                <p className="text-sm text-blue-700">
                  Our team is reviewing your application. You'll be notified once a decision has been made.
                </p>
              </div>
            </div>
          </div>

          {/* Request Details */}
          {status && (
            <div className="space-y-4">
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600 mb-1">Status</p>
                <p className="text-lg font-semibold text-yellow-600">
                  Pending
                </p>
              </div>

              {status.unitRoomNumber && (
                <div className="bg-gray-50 rounded-lg p-4">
                  <p className="text-sm text-gray-600 mb-1">Requested Unit</p>
                  <p className="text-lg font-semibold text-gray-900">
                    Room {status.unitRoomNumber}
                  </p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* What happens next */}
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            What happens next?
          </h2>
          <div className="space-y-4">
            <div className="flex gap-4">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 font-semibold">1</span>
              </div>
              <div>
                <p className="font-medium text-gray-900">Review Process</p>
                <p className="text-sm text-gray-600">
                  Our team will review your application and verify all submitted information.
                </p>
              </div>
            </div>
            
            <div className="flex gap-4">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 font-semibold">2</span>
              </div>
              <div>
                <p className="font-medium text-gray-900">Decision Notification</p>
                <p className="text-sm text-gray-600">
                  You'll receive an automatic notification when your request is approved or if any issues arise.
                </p>
              </div>
            </div>
            
            <div className="flex gap-4">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 font-semibold">3</span>
              </div>
              <div>
                <p className="font-medium text-gray-900">Next Steps</p>
                <p className="text-sm text-gray-600">
                  If approved, you can access your dashboard to complete your lease setup.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Auto-refresh indicator */}
        {/* <div className="text-center mt-6">
          <div className="inline-flex items-center gap-2 bg-white rounded-full px-4 py-2 shadow-md">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
            <p className="text-sm text-gray-600">
              Auto-refreshing every 30 seconds
            </p>
          </div>
        </div> */}

        {/* Back button (optional - use with caution) */}
        <div className="text-center mt-6">
          <button
            onClick={() => navigate('/')}
            className="text-blue-600 hover:text-blue-800 text-sm underline"
          >
            Return to Home
          </button>
        </div>
      </div>
    </div>
  );
}

export default WaitingPage;
