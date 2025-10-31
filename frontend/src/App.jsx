import HomePage from "./pages/home/page"
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from "./pages/layout";
import LoginPage from "./pages/login/page";
import SignupPage from "./pages/signup/page";
import AdminLayout from "./pages/admin/layout";
import AdminDashboard from "./pages/admin/dashboard/page";
import TenantsPage from "./pages/admin/tenants/page";
import TenantEditPage from "./pages/admin/tenants/[id]/edit";
import PaymentsPage from "./pages/admin/payments/page";
import MaintenancePage from "./pages/admin/maintenance/page";
import CreateProfilePage from "./pages/create-profile/page";
import BookingPage from "./pages/booking/page";
import WaitingPage from "./pages/booking/waiting/page";
import PrivateRoute from "./components/auth/PrivateRoute";
import RentalRequestsPage from "./pages/admin/rental-requests/page";
import RentalRequestDetailPage from "./pages/admin/rental-requests/[id]/form";
import UnitDetailPage from "./pages/admin/units/[id]/page";
import UnitEditPage from "./pages/admin/units/[id]/edit";

function App() {

  return (
    <>
      <Router>
        <Routes>
          
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
          </Route>

          <Route path="/admin/" element={<AdminLayout />}>
            <Route index element={<AdminDashboard />} />
            <Route path="tenants" element={<TenantsPage />} />
            <Route path="tenants/:id/edit" element={<TenantEditPage />} />
            <Route path="payments" element={<PaymentsPage />} />
            <Route path="maintenance" element={<MaintenancePage />} />
            <Route path="rental-requests" element={<RentalRequestsPage />} />
            <Route path="rental-requests/:id" element={<RentalRequestDetailPage />} />
            <Route path="units/:id" element={<UnitDetailPage />} />
            <Route path="units/:id/edit" element={<UnitEditPage />} />
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
          
          <Route path="/create-profile" element={
            <PrivateRoute>
              <CreateProfilePage />
            </PrivateRoute>
          } />

          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
        </Routes>
      </Router>
    </>
  )
}

export default App
