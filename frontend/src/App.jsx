import HomePage from "./pages/home/page"
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from "./pages/layout";


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