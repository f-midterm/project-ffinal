import React, { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  FaHome,
  FaBolt,
  FaTint,
  FaWrench,
  FaFileInvoice,
  FaArrowLeft,
  FaDownload,
  FaExclamationCircle,
  FaUpload,
  FaCheckCircle,
  FaClock,
} from "react-icons/fa";
import {
  getInvoiceById,
  downloadInvoicePdf,
  uploadPaymentSlip,
} from "../../../../api/services/invoices.service";
import { getBackendResourceUrl } from "../../../../api/client/apiClient";
import { QRCodeCanvas } from "qrcode.react";
import generatePayload from "promptpay-qr";

const PAYMENT_TYPE_CONFIG = {
  RENT: {
    icon: <FaHome className="inline" />,
    label: "Rent",
    color: "text-indigo-600",
  },
  ELECTRICITY: {
    icon: <FaBolt className="inline" />,
    label: "Electricity",
    color: "text-yellow-600",
  },
  WATER: {
    icon: <FaTint className="inline" />,
    label: "Water",
    color: "text-blue-600",
  },
  MAINTENANCE: {
    icon: <FaWrench className="inline" />,
    label: "Maintenance",
    color: "text-gray-600",
  },
  SECURITY_DEPOSIT: {
    icon: <FaFileInvoice className="inline" />,
    label: "Security Deposit",
    color: "text-purple-600",
  },
  OTHER: {
    icon: <FaFileInvoice className="inline" />,
    label: "Other",
    color: "text-gray-600",
  },
};

function PaymentPage() {
  const { id, invoiceId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [invoice, setInvoice] = useState(null);
  const [downloading, setDownloading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [slipFile, setSlipFile] = useState(null);
  const [slipPreview, setSlipPreview] = useState(null);
  const fileInputRef = useRef(null);

  // Calculate late fee
  const [daysLate, setDaysLate] = useState(0);
  const [lateFee, setLateFee] = useState(0);
  const [totalWithLateFee, setTotalWithLateFee] = useState(0);

  // PromptPay configuration - ใส่เบอร์โทรหรือ ID ของร้าน
  const PROMPTPAY_ID = "0624243432"; // เปลี่ยนเป็นเบอร์จริง

  useEffect(() => {
    fetchInvoice();
  }, [invoiceId]);

  const fetchInvoice = async () => {
    try {
      setLoading(true);
      setError(null);
      const invoiceData = await getInvoiceById(invoiceId);
      setInvoice(invoiceData);

      // Calculate late fee if overdue
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const dueDate = new Date(invoiceData.dueDate);
      dueDate.setHours(0, 0, 0, 0);

      if (dueDate < today && invoiceData.status !== "PAID") {
        const days = Math.floor((today - dueDate) / (1000 * 60 * 60 * 24));
        const fee = days * 300; // 300 baht per day
        setDaysLate(days);
        setLateFee(fee);
        setTotalWithLateFee(invoiceData.totalAmount + fee);
      } else {
        setDaysLate(0);
        setLateFee(0);
        setTotalWithLateFee(invoiceData.totalAmount);
      }
    } catch (err) {
      console.error("Error fetching invoice:", err);
      setError("Failed to load invoice details. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    try {
      setDownloading(true);
      await downloadInvoicePdf(invoice.id, invoice.invoiceNumber);
    } catch (err) {
      console.error("Error downloading PDF:", err);
      alert("Failed to download invoice PDF. Please try again.");
    } finally {
      setDownloading(false);
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        // 5MB limit
        alert("File size should not exceed 5MB");
        return;
      }
      if (!file.type.startsWith("image/")) {
        alert("Please upload an image file");
        return;
      }
      setSlipFile(file);

      // Create preview
      const reader = new FileReader();
      reader.onloadend = () => {
        setSlipPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleUploadSlip = async () => {
    if (!slipFile) {
      alert("Please select a payment slip image");
      return;
    }

    try {
      setUploading(true);
      await uploadPaymentSlip(invoice.id, slipFile);
      alert(
        "Payment slip uploaded successfully! Please refresh the page to see the updated status."
      );
      // Don't auto-refresh to avoid 403 - let user refresh manually
      // fetchInvoice();
      setSlipFile(null);
      setSlipPreview(null);
    } catch (err) {
      console.error("Error uploading slip:", err);
      alert("Failed to upload payment slip. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  const handlePayNow = () => {
    // Scroll to QR code section
    const qrSection = document.getElementById("qr-code-section");
    if (qrSection) {
      qrSection.scrollIntoView({ behavior: "smooth" });
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("th-TH", {
      style: "decimal",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  const getStatusBadge = () => {
    if (isOverdue) {
      return (
        <span className="px-4 py-2 rounded-full text-sm font-medium bg-red-100 text-red-800 flex items-center gap-2">
          <FaExclamationCircle /> Overdue
        </span>
      );
    }

    switch (invoice.status) {
      case "PAID":
        return (
          <span className="px-4 py-2 rounded-full text-sm font-medium bg-green-100 text-green-800 flex items-center gap-2">
            <FaCheckCircle /> Paid
          </span>
        );
      case "WAITING_VERIFICATION":
        return (
          <span className="px-4 py-2 rounded-full text-sm font-medium bg-blue-100 text-blue-800 flex items-center gap-2">
            <FaClock /> Waiting Verification
          </span>
        );
      case "PARTIAL":
        return (
          <span className="px-4 py-2 rounded-full text-sm font-medium bg-blue-100 text-blue-800">
            Partial Payment
          </span>
        );
      case "REJECTED":
        return (
          <span className="px-4 py-2 rounded-full text-sm font-medium bg-red-100 text-red-800 flex items-center gap-2">
            <FaExclamationCircle /> Payment Rejected
          </span>
        );
      default:
        return (
          <span className="px-4 py-2 rounded-full text-sm font-medium bg-yellow-100 text-yellow-800">
            Pending
          </span>
        );
    }
  };

  const isOverdue =
    invoice &&
    new Date(invoice.dueDate) < new Date() &&
    invoice.status !== "PAID";

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[500px]">
        <div className="text-xl text-gray-600">Loading invoice details...</div>
      </div>
    );
  }

  if (error || !invoice) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[500px]">
        <div className="text-xl text-red-600 mb-4">
          {error || "Invoice not found"}
        </div>
        <button
          onClick={() => navigate(`/user/${id}/billing`)}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Back to Billing
        </button>
      </div>
    );
  }

  return (
    <div className="mx-auto p-6">
      {/* Back Button */}
      <button
        onClick={() => navigate(`/user/${id}/billing`)}
        className="flex items-center gap-2 text-gray-600 hover:text-blue-600 mb-6"
      >
        <FaArrowLeft /> Back to Billing
      </button>

      {/* Invoice Header */}
      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-800 mb-2">
              Invoice Payment
            </h1>
            <p className="text-gray-600">Invoice: {invoice.invoiceNumber}</p>
          </div>
          <div className="text-right">{getStatusBadge()}</div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t">
          <div>
            <p className="text-sm text-gray-500">Invoice Date</p>
            <p className="text-gray-800 font-medium">
              {formatDate(invoice.invoiceDate)}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Due Date</p>
            <p
              className={`font-medium ${
                isOverdue ? "text-red-600" : "text-gray-800"
              }`}
            >
              {formatDate(invoice.dueDate)}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Unit</p>
            <p className="text-gray-800 font-medium">
              {invoice.lease?.unit?.roomNumber || "N/A"}
            </p>
          </div>
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Unit Information */}
        <div className="bg-white rounded-xl shadow-md p-6 lg:mb-6">
          <h2 className="text-xl font-bold text-gray-800 mb-4">
            Property Information
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-500">Unit Number</p>
              <p className="text-gray-800 font-medium">
                {invoice.lease?.unit?.roomNumber || "N/A"}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Unit Type</p>
              <p className="text-gray-800 font-medium">
                {invoice.lease?.unit?.unitType || "N/A"}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Tenant Name</p>
              <p className="text-gray-800 font-medium">
                {invoice.lease?.tenant
                  ? `${invoice.lease.tenant.firstName} ${invoice.lease.tenant.lastName}`
                  : "N/A"}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Contact</p>
              <p className="text-gray-800 font-medium">
                {invoice.lease?.tenant?.phone || "N/A"}
              </p>
            </div>
          </div>
        </div>

        {/* Payment Breakdown */}
        <div className="bg-white rounded-xl shadow-md p-6 mb-6">
          <h2 className="text-xl font-bold text-gray-800 mb-4">
            Payment Breakdown
          </h2>
          <div className="space-y-3">
            {invoice.payments &&
              invoice.payments.map((payment, index) => {
                const config =
                  PAYMENT_TYPE_CONFIG[payment.paymentType] ||
                  PAYMENT_TYPE_CONFIG.OTHER;
                return (
                  <div
                    key={index}
                    className="flex justify-between items-center py-3 border-b last:border-b-0"
                  >
                    <div className="flex items-center gap-3">
                      <span className={`text-xl ${config.color}`}>
                        {config.icon}
                      </span>
                      <div>
                        <p className="font-medium text-gray-800">
                          {config.label}
                        </p>
                        {payment.notes && (
                          <p className="text-sm text-gray-500">
                            {payment.notes}
                          </p>
                        )}
                      </div>
                    </div>
                    <div className="text-gray-800 font-medium">
                      ฿{formatCurrency(payment.amount)}
                    </div>
                  </div>
                );
              })}
          </div>

          <div>
            {daysLate > 0 && (
              <div className="mb-4">
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-3">
                  <div className="flex items-center gap-2 text-red-700 mb-2">
                    <FaExclamationCircle />
                    <span className="font-medium">Late Payment Fee</span>
                  </div>
                  <p className="text-sm text-red-600 mb-2">
                    This invoice is{" "}
                    <strong>
                      {daysLate} day{daysLate > 1 ? "s" : ""}
                    </strong>{" "}
                    overdue. Late fee: 300 ฿/day
                  </p>
                  <div className="flex justify-between items-center text-red-700 font-medium">
                    <span>Late Fee ({daysLate} days × 300 ฿):</span>
                    <span>+฿{formatCurrency(lateFee)}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl p-6 shadow-md mb-6">
          <div className="flex justify-between items-center mb-2">
            <span className="text-lg font-medium text-gray-700">Subtotal</span>
            <span className="text-lg font-medium text-gray-800">
              ฿{formatCurrency(invoice.totalAmount)}
            </span>
          </div>

          {daysLate > 0 && (
            <div className="flex justify-between items-center mb-2">
              <span className="text-lg font-medium text-red-600">Late Fee</span>
              <span className="text-lg font-medium text-red-600">
                +฿{formatCurrency(lateFee)}
              </span>
            </div>
          )}

          <div className="flex justify-between items-center pt-3 border-t border-gray-200">
            <span className="text-xl font-bold text-gray-800">
              Total Amount
            </span>
            <span
              className={`text-2xl font-bold ${
                daysLate > 0 ? "text-red-600" : "text-blue-600"
              }`}
            >
              ฿{formatCurrency(totalWithLateFee)}
            </span>
          </div>
      </div>

      {/* QR Code Payment Section */}
      {(invoice.status === "PENDING" || invoice.status === "REJECTED") && (
        <div
          id="qr-code-section"
          className="bg-white rounded-xl shadow-md p-6 mb-6"
        >
          {/* Payment method */}
          <div className="text-xl font-medium">Payment Method</div>
          <div className="flex justify-between items-center mb-6 p-4 border-b border-gray-200 gap-2">
            <button className="w-full border-2 border-gray-300 p-4 rounded-lg hover:bg-gray-100">Full payment</button>
            <button className="w-full border-2 border-gray-300 p-4 rounded-lg hover:bg-gray-100">Installment plan</button>
          </div>

          {/* If using Installment Plan gonna show this Caution */}
          <div className="border-l-4 border-amber-500 p-6 bg-amber-100 mb-6">
            
          </div>

          <h2 className="text-xl font-bold text-gray-800 mb-4 text-center">
            Scan to Pay with PromptPay
          </h2>
          <div className="flex flex-col items-center gap-4">
            <div className="bg-white p-6 rounded-lg border-4 border-blue-500">
              <QRCodeCanvas
                value={generatePayload(PROMPTPAY_ID, {
                  amount: parseFloat(totalWithLateFee),
                })}
                size={256}
                level="H"
                includeMargin={true}
              />
            </div>
            <div className="text-center">
              <p className="text-gray-600 mb-2">
                Scan QR Code with your banking app
              </p>
              <p
                className={`text-2xl font-bold ${
                  daysLate > 0 ? "text-red-600" : "text-blue-600"
                }`}
              >
                ฿{formatCurrency(totalWithLateFee)}
              </p>
              {daysLate > 0 && (
                <p className="text-sm text-red-600 mt-1">
                  Includes ฿{formatCurrency(lateFee)} late fee ({daysLate} day
                  {daysLate > 1 ? "s" : ""})
                </p>
              )}
              <p className="text-sm text-gray-500 mt-2">
                Invoice: {invoice.invoiceNumber}
              </p>
            </div>
          </div>

          {/* Upload Slip Section */}
          <div className="mt-8 pt-6 border-t flex flex-col items-center gap-4">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">
              Upload Payment Slip
            </h3>
            <div className="border p-4 lg:min-h-[400px] lg:min-w-[800px] rounded-xl border-gray-300 flex flex-col items-center justify-center">
              {slipPreview ? (
                <div className="space-y-4">
                  <div className="flex justify-center">
                    <img
                      src={slipPreview}
                      alt="Payment Slip Preview"
                      className="max-w-xs rounded-lg shadow-md"
                    />
                  </div>
                  <div className="flex gap-3 justify-center">
                    <button
                      onClick={() => {
                        setSlipFile(null);
                        setSlipPreview(null);
                        if (fileInputRef.current) {
                          fileInputRef.current.value = "";
                        }
                      }}
                      className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handleUploadSlip}
                      disabled={uploading}
                      className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 flex items-center gap-2"
                    >
                      <FaUpload />
                      {uploading ? "Uploading..." : "Submit Payment"}
                    </button>
                  </div>
                </div>
              ) : (
                <div className="flex flex-col items-center gap-4">
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleFileSelect}
                    className="hidden"
                    id="slip-upload"
                  />
                  <label
                    htmlFor="slip-upload"
                    className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 cursor-pointer flex items-center gap-2 transition-colors"
                  >
                    <FaUpload />
                    Choose Payment Slip Image
                  </label>
                  <p className="text-sm text-gray-500">
                    Maximum file size: 5MB
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Waiting Verification Message */}
      {invoice.status === "WAITING_VERIFICATION" && (
        <div className="bg-blue-50 border-l-4 border-blue-500 p-6 rounded-lg mb-6">
          <div className="flex items-start gap-4">
            <FaClock className="text-blue-600 text-2xl mt-1" />
            <div>
              <h3 className="text-lg font-semibold text-blue-900 mb-2">
                Payment Under Verification
              </h3>
              <p className="text-blue-800 mb-2">
                Your payment slip has been submitted successfully and is
                currently being verified by our admin team.
              </p>
              <p className="text-blue-700 text-sm">
                You will be notified once the payment is confirmed. This usually
                takes 1-2 business days.
              </p>
              {invoice.slipUrl && (
                <div className="mt-4">
                  <p className="text-sm text-blue-700 mb-2">
                    Submitted Payment Slip:
                  </p>
                  <img
                    src={getBackendResourceUrl(invoice.slipUrl)}
                    alt="Submitted Payment Slip"
                    className="max-w-xs rounded-lg shadow-md"
                  />
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Payment Confirmed Message */}
      {invoice.status === "PAID" && (
        <div className="bg-green-50 border-l-4 border-green-500 p-6 rounded-lg mb-6">
          <div className="flex items-start gap-4">
            <FaCheckCircle className="text-green-600 text-2xl mt-1" />
            <div>
              <h3 className="text-lg font-semibold text-green-900 mb-2">
                Payment Confirmed
              </h3>
              <p className="text-green-800">
                Your payment has been verified and confirmed. Thank you for your
                payment!
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Notes */}
      {invoice.notes && (
        <div className="bg-blue-50 border-l-4 border-blue-500 ounded-xl p-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-2">Notes</h3>
          <p className="text-gray-700">{invoice.notes}</p>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex gap-4 justify-center flex-wrap">
        <button
          onClick={handleDownloadPdf}
          disabled={downloading}
          className="px-8 py-3 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors shadow-md hover:shadow-lg flex items-center gap-2 disabled:opacity-50"
        >
          <FaDownload /> {downloading ? "Downloading..." : "Download Invoice"}
        </button>
      </div>

      {/* Overdue Warning */}
      {isOverdue && (
        <div className="mt-6 bg-red-50 border-l-4 border-red-500 p-4 rounded-lg">
          <div className="flex items-center gap-3">
            <FaExclamationCircle className="text-red-600 text-xl" />
            <div>
              <p className="font-semibold text-red-800">Payment Overdue</p>
              <p className="text-red-700 text-sm">
                Please pay as soon as possible to avoid late fees.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default PaymentPage;
