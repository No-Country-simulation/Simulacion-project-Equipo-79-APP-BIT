import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// ? deps
import { BrowserRouter, Routes, Route } from "react-router";
// ? components
import BaseLayout from './layouts/BaseLayout.jsx'
import './index.css'
import App from './App.jsx'
import RegisterCompany from './pages/RegisterCompany.jsx';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BaseLayout>
      <BrowserRouter>
        <Routes>
          <Route path='/' element={<App />} />
          <Route path='/register-company' element={<RegisterCompany />} />
        </Routes>
      </BrowserRouter>
    </BaseLayout>
  </StrictMode>,
)
