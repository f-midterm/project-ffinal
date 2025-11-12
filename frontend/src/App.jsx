import HomePage from "./pages/home/page"
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from "./pages/layout";
import LoginPage from "./pages/login/page";
import SignupPage from "./pages/signup/page";
import AdminLayout from "./pages/admin/layout";
import AdminDashboard from "./pages/admin/dashboard/page";
import TenantsPage from "./pages/admin/tenants/page";
import PaymentsPage from "./pages/admin/payments/page";
import MaintenancePage from "./pages/admin/maintenance/page";
import CreateProfilePage from "./pages/create-profile/page";
import BookingPage from "./pages/booking/page";
import PrivateRoute from "./components/auth/PrivateRoute";
import AdminRoute from "./components/auth/AdminRoute";
import UserRoute from "./components/auth/UserRoute";
import ProfilePage from "./pages/user/profile/page";
import UnitPage from "./pages/admin/unit/page";
import UserLayout from "./pages/user/layout";
import RentalRequestsPage from "./pages/admin/rental-requests/page";
import WaitingPage from "./pages/booking/waiting/page";
import BookedPage from "./pages/booking/booked/page";
import RentalRequestDetailPage from "./pages/admin/rental-requests/[id]/page";
import SendBillPage from "./pages/admin/unit/send_bill/page";


function App() {

  return (
    <>
      <Router>
        <Routes>
          
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
          </Route>

          <Route path="/admin/" element={
            <AdminRoute>
              <AdminLayout />
            </AdminRoute>
          }>
            <Route index element={<AdminDashboard />} />
            <Route path="tenants" element={<TenantsPage />} />
            <Route path="payments" element={<PaymentsPage />} />
            <Route path="maintenance" element={<MaintenancePage />} />
            <Route path="rental-requests" element={<RentalRequestsPage />} />
            <Route path="rental-requests/:id" element={<RentalRequestDetailPage />} />
            <Route path="unit/:id" element={<UnitPage />} />
            <Route path="send-invoice/:id" element={<SendBillPage />} />
          </Route>

          {/* Protected Routes - Require Authentication */}
          <Route path="/booking" element={
            <PrivateRoute>
              <BookingPage />
            </PrivateRoute>
          } />
          
          <Route path="/booking/waiting" element={
            <PrivateRoute>
              <WaitingPage />
            </PrivateRoute>
          } />

          <Route path="/booking/booked" element={
            <PrivateRoute>
              <BookedPage />
            </PrivateRoute>
          } />

          <Route path="/create-profile" element={
            <PrivateRoute>
              <CreateProfilePage />
            </PrivateRoute>
          } />

          <Route path="/user/:id" element={
            <UserRoute>
              <UserLayout />
            </UserRoute>
          }>
            <Route index element={<ProfilePage />} />
          </Route>

          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
        </Routes>
      </Router>
    </>
  )
}

export default App
