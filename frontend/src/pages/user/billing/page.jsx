import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom';
import { FcInspection } from "react-icons/fc";
import BillingContentCard from '../../../components/card/billing_content_card';
import { getInvoicesByTenant } from '../../../api/services/invoices.service';
import { getCurrentUser } from '../../../api/services/auth.service';

function BillingPage() {
    
    const navigate = useNavigate();
    const { id } = useParams();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [invoices, setInvoices] = useState([]);
    const [upcomingInvoices, setUpcomingInvoices] = useState([]);
    const [finishedInvoices, setFinishedInvoices] = useState([]);
    const [sortBy, setSortBy] = useState('newest');

    useEffect(() => {
        fetchInvoices();
    }, [id]);

    useEffect(() => {
        // Sort finished invoices when sortBy changes
        if (finishedInvoices.length > 0) {
            const sorted = [...finishedInvoices].sort((a, b) => {
                if (sortBy === 'newest') {
                    return new Date(b.invoiceDate) - new Date(a.invoiceDate);
                } else {
                    return new Date(a.invoiceDate) - new Date(b.invoiceDate);
                }
            });
            setFinishedInvoices(sorted);
        }
    }, [sortBy]);

    const fetchInvoices = async () => {
        try {
            setLoading(true);
            setError(null);

            // Get current user to fetch their email
            const user = await getCurrentUser();
            
            // Fetch invoices by user email
            const invoicesData = await getInvoicesByTenant(user.email);
            setInvoices(invoicesData);

            // Split invoices by status
            const upcoming = invoicesData.filter(inv => 
                inv.status === 'PENDING' || inv.status === 'OVERDUE' || inv.status === 'PARTIAL'
            );
            const finished = invoicesData.filter(inv => inv.status === 'PAID');

            // Sort by date (newest first by default)
            const sortedUpcoming = upcoming.sort((a, b) => new Date(b.dueDate) - new Date(a.dueDate));
            const sortedFinished = finished.sort((a, b) => new Date(b.invoiceDate) - new Date(a.invoiceDate));

            setUpcomingInvoices(sortedUpcoming);
            setFinishedInvoices(sortedFinished);
        } catch (err) {
            console.error('Error fetching invoices:', err);
            setError('Failed to load invoices. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    const handleInvoiceClick = (invoice) => {
        if (invoice.status === 'PAID') {
            navigate(`/user/${id}/billing/detail/${invoice.id}`);
        } else {
            navigate(`/user/${id}/billing/payment/${invoice.id}`);
        }
    };

    if (loading) {
        return (
            <div className='flex justify-center items-center min-h-[500px]'>
                <div className='text-xl text-gray-600'>Loading invoices...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className='flex justify-center items-center min-h-[500px]'>
                <div className='text-xl text-red-600'>{error}</div>
            </div>
        );
    }

    return (
        <div>
            
            {/* Empty State */}
            {invoices.length === 0 && (
                <div className='flex flex-col items-center justify-center min-h-[500px]'>
                    <div className='bg-gray-200 rounded-full p-10 mb-6'>
                        <FcInspection size={64} />
                    </div>
                    <div className='text-3xl text-gray-600 font-medium mb-4'>
                        You don't have any bills
                    </div>
                </div>
            )}

            {/* History and Upcoming */}
            {invoices.length > 0 && (
                <div className='flex flex-col'>
                    {/* Upcoming Section */}
                    <div className='mb-4 text-xl font-medium'>
                        Upcoming {upcomingInvoices.length > 0 && `(${upcomingInvoices.length})`}
                    </div>

                    <div className='border-b border-gray-400 mb-4'></div>

                    {upcomingInvoices.length === 0 ? (
                        <div className='text-gray-500 mb-6 text-center py-8'>
                            No upcoming bills
                        </div>
                    ) : (
                        <div className='flex flex-col gap-4 mb-6'>
                            {upcomingInvoices.map(invoice => (
                                <BillingContentCard 
                                    key={invoice.id}
                                    invoice={invoice}
                                    onClick={() => handleInvoiceClick(invoice)}
                                />
                            ))}
                        </div>
                    )}
                    
                    {/* Finished Section */}
                    <div className='flex justify-between items-center'>
                        <div className='text-xl font-medium mb-4'>
                            Finished {finishedInvoices.length > 0 && `(${finishedInvoices.length})`}
                        </div>
                        {/* Sort and Filter */}
                        {finishedInvoices.length > 0 && (
                            <div className='flex gap-4 mb-4 items-center'>
                                <select 
                                    value={sortBy}
                                    onChange={(e) => setSortBy(e.target.value)}
                                    className='bg-white border rounded-lg px-4 py-2 mr-2 text-sm text-gray-700 hover:bg-gray-50 shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                                >
                                    <option value="newest">Sort by: Newest</option>
                                    <option value="oldest">Sort by: Oldest</option>
                                </select>
                            </div>
                        )}
                    </div>

                    <div className='border-b border-gray-400 mb-4'></div>

                    {finishedInvoices.length === 0 ? (
                        <div className='text-gray-500 mb-6 text-center py-8'>
                            No payment history yet
                        </div>
                    ) : (
                        <div className='flex flex-col gap-4 mb-6'>
                            {finishedInvoices.map(invoice => (
                                <BillingContentCard 
                                    key={invoice.id}
                                    invoice={invoice}
                                    onClick={() => handleInvoiceClick(invoice)}
                                />
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    )
}

export default BillingPage