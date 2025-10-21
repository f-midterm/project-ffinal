// Route
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Layout
import Layout from "./pages/layout";

// Pages
import HomePage from "./pages/home/page";
import SignupPage from "./pages/signup/page";


function App() {

  return (
    <>
      <Router>
        <Routes>
          
          <Route path="/" element={<Layout />}>
            <Route index element={<HomePage />} />
          </Route>

          <Route path="/signup" element={<SignupPage />} />

        </Routes>
      </Router>
    </>
  )
}

export default App