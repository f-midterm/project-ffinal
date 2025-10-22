// Route
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Layout
import Layout from "./pages/layout";
import AdminLayout from './pages/admin/layout';

// Pages 
import HomePage from "./pages/home/page";
import LoginPage from './pages/login/page';
import AdminDashboard from './pages/admin/dashboard/page';



function App() {

  return (
    <>
      <Router>
        <Routes>
          
          {/* Home */}
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
          </Route>

          {/* Admin */}
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboard />} />
          </Route>

          <Route path="/login" element={<LoginPage />} />

        </Routes>
      </Router>
    </>
  )
}

export default App