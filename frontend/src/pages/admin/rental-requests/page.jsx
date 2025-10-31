import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllRentalRequests } from '../../../api/services/rentalRequests.service'
import { HiOutlineInbox } from 'react-icons/hi2'

function RentalRequestsPage() {
    const [rentalRequests, setRentalRequests] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [sortBy, setSortBy] = useState('newest')
    const [filterBy, setFilterBy] = useState('all')
    const navigate = useNavigate()

    useEffect(() => {
        fetchRentalRequests()
    }, [])

    const fetchRentalRequests = async () => {
        setLoading(true)
        setError(null)
        try {
            const data = await getAllRentalRequests()
            setRentalRequests(data)
        } catch (err) {
            console.error('Failed to fetch rental requests:', err)
            setError('Failed to load rental requests. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    const handleCardClick = (requestId) => {
        navigate(`/admin/rental-requests/${requestId}`)
    }

    const formatDate = (dateString) => {
        const date = new Date(dateString)
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        })
    }

    const getStatusBadgeColor = (status) => {
        switch (status) {
            case 'PENDING':
                return 'bg-yellow-100 text-yellow-800'
            case 'APPROVED':
                return 'bg-green-100 text-green-800'
            case 'REJECTED':
                return 'bg-red-100 text-red-800'
            default:
                return 'bg-gray-100 text-gray-800'
        }
    }

    // Filter requests based on selected filter
    const filteredRequests = rentalRequests.filter(request => {
        if (filterBy === 'all') return true
        return request.status === filterBy
    })

    // Sort requests
    const sortedRequests = [...filteredRequests].sort((a, b) => {
        if (sortBy === 'newest') {
            return new Date(b.requestDate) - new Date(a.requestDate)
        } else if (sortBy === 'oldest') {
            return new Date(a.requestDate) - new Date(b.requestDate)
        }
        return 0
    })

    const pendingCount = rentalRequests.filter(r => r.status === 'PENDING').length

    return (
        <div>
            {/* Header */}
            <div className='mb-8'>
                <div className='title'>Rental Requests</div>
                <div className='flex mb-6 gap-4'>
                    <div className='flex-grow font-medium text-gray-700 mt-5'>
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className='bg-white border rounded-lg px-4 py-2 mr-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                    >
                        <option value="newest">Sort by: Newest</option>
                        <option value="oldest">Sort by: Oldest</option>
                    </select>
                    <select
                        value={filterBy}
                        onChange={(e) => setFilterBy(e.target.value)}
                        className='bg-white border rounded-lg px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                    >
                        <option value="all">Filter by: All</option>
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="REJECTED">Rejected</option>
                    </select>
</div>
                </div>
                {pendingCount > 0 && (
                    <p className='text-sm text-gray-600 mt-2'>
                        {pendingCount} pending request{pendingCount > 1 ? 's' : ''} awaiting review
                    </p>
                )}
            </div>

            {/* Error Message */}
            {error && (
                <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                    {error}
                </div>
            )}

            {/* Loading State */}
            {loading ? (
                <div className="flex justify-center items-center p-16">
                    <div className="text-gray-600">Loading rental requests...</div>
                </div>
            ) : sortedRequests.length === 0 ? (
                /* Empty State */
                <div className="p-16 flex flex-col items-center justify-center text-center">
                    <div className="w-28 h-28 bg-gray-200 rounded-full flex items-center justify-center mb-6">
                        <HiOutlineInbox className="h-16 w-16 text-gray-600" />
                    </div>
                    <h2 className="text-xl font-semibold text-gray-900 mb-2">
                        {filterBy === 'all' ? 'No Rental Requests' : `No ${filterBy} Requests`}
                    </h2>
                    <p className="text-gray-500">
                        {filterBy === 'all'
                            ? "You're all caught up! New requests will appear here."
                            : `No requests with status "${filterBy}".`
                        }
                    </p>
                </div>
            ) : (
                /* Requests List */
                <div>
                    {/* Sort and Filter */}
                    {/* <div className='flex mb-6 gap-4'>
                        <select 
                            value={sortBy} 
                            onChange={(e) => setSortBy(e.target.value)}
                            className='bg-white border rounded-lg px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                        >
                            <option value="newest">Sort by: Newest</option>
                            <option value="oldest">Sort by: Oldest</option>
                        </select>
                        <select 
                            value={filterBy} 
                            onChange={(e) => setFilterBy(e.target.value)}
                            className='bg-white border rounded-lg px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                        >
                            <option value="all">Filter by: All</option>
                            <option value="PENDING">Pending</option>
                            <option value="APPROVED">Approved</option>
                            <option value="REJECTED">Rejected</option>
                        </select>
                    </div> */}

                    {/* Request Cards */}
                    <div className='space-y-4'>
                        {sortedRequests.map((request) => (
                            <div
                                key={request.id}
                                onClick={() => handleCardClick(request.id)}
                                className='bg-white rounded-lg shadow-md p-6 hover:shadow-lg hover:-translate-y-1 transition-all cursor-pointer'
                            >
                                <div className='flex justify-between items-start'>
                                    <div className='flex-1'>
                                        <div className='flex items-center gap-3 mb-2'>
                                            <h3 className='text-lg font-semibold text-gray-900'>
                                                Si {request.unit?.roomNumber || request.unitId}
                                            </h3>
                                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusBadgeColor(request.status)}`}>
                                                {request.status}
                                            </span>
                                        </div>
                                        <p className='text-gray-700 font-medium mb-1'>
                                            {request.firstName} {request.lastName}
                                        </p>
                                        <p className='text-sm text-gray-500'>
                                            {request.email} • {request.phone}
                                        </p>
                                        <p className='text-sm text-gray-500 mt-2'>
                                            Requested: {formatDate(request.requestDate)}
                                        </p>
                                    </div>
                                    <div className='text-right'>
                                        <p className='text-sm text-gray-600'>
                                            Duration: {request.leaseDurationMonths} month{request.leaseDurationMonths > 1 ? 's' : ''}
                                        </p>
                                        {request.unit && (
                                            <p className='text-lg font-semibold text-blue-600 mt-2'>
                                                ${request.unit.rentAmount}/mo
                                            </p>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    )
}

export default RentalRequestsPage