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
            <Route path="payments" element={<PaymentsPage />} />
            <Route path="maintenance" element={<MaintenancePage />} />
          </Route>

          {/* Protected Routes - Require Authentication */}
          <Route path="/booking" element={
            <PrivateRoute>
              <BookingPage />
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
