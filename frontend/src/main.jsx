import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// ? deps
import { BrowserRouter, Routes, Route } from "react-router";
import { Toaster } from 'sileo';
// ? components
import BaseLayout from './layouts/BaseLayout.jsx'
import './index.css'
import App from './App.jsx'
import RegisterCompany from './pages/RegisterCompany.jsx';
import CreateJobOffer from './pages/CreateJobOffer.jsx';
import Jobs from './pages/Jobs.jsx';
import CandidatesList from './pages/CandidatesList.jsx';
import Insights from './pages/Insights.jsx';
import Settings from './pages/Settings.jsx';
import Support from './pages/Support.jsx';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <BaseLayout>
        <Toaster position="top-center" />
        <Routes>
          <Route path='/' element={<App />} />
          <Route path='/register-company' element={<RegisterCompany />} />
          <Route path='/create-job' element={<CreateJobOffer />} />
          <Route path='/job' element={<Jobs />} />
          <Route path='/job/:jobId/candidates' element={<CandidatesList />} />
          <Route path='/insights' element={<Insights />} />
          <Route path='/settings' element={<Settings />} />
          <Route path='/support' element={<Support />} />
        </Routes>
      </BaseLayout>
    </BrowserRouter>
  </StrictMode>,
)
