import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// ? deps
import { BrowserRouter, Routes, Route, useNavigate } from "react-router";
import { Toaster } from 'sileo';
import { ClerkProvider } from '@clerk/react';
// ? components
import App from './App.jsx'
import BaseLayout from './layouts/BaseLayout.jsx'
import RegisterCompany from './pages/RegisterCompany.jsx';
import CreateJobOffer from './pages/CreateJobOffer.jsx';
import Jobs from './pages/Jobs.jsx';
import CandidatesList from './pages/CandidatesList.jsx';
import Insights from './pages/Insights.jsx';
import Settings from './pages/Settings.jsx';
import Support from './pages/Support.jsx';
import SignInPage from './pages/SignInPage.jsx';
import SignUpPage from './pages/SignUpPage.jsx';
// ? css
import './index.css'
import RequireAuth from './components/RequireAuth.jsx';

function RootLayout() {
  const navigate = useNavigate();

  return (
    <ClerkProvider
      publishableKey={import.meta.env.NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY}
      routerPush={(to) => navigate(to)}
      routerReplace={(to) => navigate(to, { replace: true })}
      signInUrl='/sign-in'
      afterSignInUrl='/'
      afterSignUpUrl='/register-company'
    >
      <BaseLayout>
        <Toaster position="top-center" />
        <Routes>
          <Route path='/sign-in' element={<SignInPage />} />
          <Route path='/sign-up' element={<SignUpPage />} />
          <Route element={<RequireAuth />}>
            <Route path='/' element={<App />} />
            <Route path='/register-company' element={<RegisterCompany />} />
            <Route path='/create-job' element={<CreateJobOffer />} />
            <Route path='/job' element={<Jobs />} />
            <Route path='/job/:jobId/candidates' element={<CandidatesList />} />
            <Route path='/insights' element={<Insights />} />
            <Route path='/settings' element={<Settings />} />
            <Route path='/support' element={<Support />} />
          </Route>
        </Routes>
      </BaseLayout>
    </ClerkProvider>
  )
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <RootLayout />
    </BrowserRouter>
  </StrictMode>,
)
