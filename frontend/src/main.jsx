import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// ? deps
import { BrowserRouter, Routes, Route } from "react-router";
// ? components
import BaseLayout from './layouts/BaseLayout.jsx'
import './index.css'
import App from './App.jsx'
import RegisterCompany from './pages/RegisterCompany.jsx';
import CreateJobOffer from './pages/CreateJobOffer.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <BaseLayout>
        <Routes>
          <Route path='/' element={<App />} />
          <Route path='/register-company' element={<RegisterCompany />} />
          <Route path='/create-job' element={<CreateJobOffer />} />
        </Routes>
      </BaseLayout>
    </BrowserRouter>
  </StrictMode>,
)
