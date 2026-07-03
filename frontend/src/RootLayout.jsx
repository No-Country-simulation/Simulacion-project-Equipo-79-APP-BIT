import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Routes, Route } from 'react-router';
import { Toaster } from 'sileo';
import { ClerkProvider } from '@clerk/react';
import App from './App.jsx';
import BaseLayout from './layouts/BaseLayout.jsx';
import RegisterCompany from './pages/RegisterCompany.jsx';
import CreateJobOffer from './pages/CreateJobOffer.jsx';
import EditJobOffer from './pages/EditJobOffer.jsx';
import Jobs from './pages/Jobs.jsx';
import CandidatesList from './pages/CandidatesList.jsx';
import Insights from './pages/Insights.jsx';
import Settings from './pages/Settings.jsx';
import Support from './pages/Support.jsx';
import SignInPage from './pages/SignInPage.jsx';
import SignUpPage from './pages/SignUpPage.jsx';
import RequireAuth from './components/RequireAuth.jsx';

function RootLayout() {
  const navigate = useNavigate();

  useEffect(() => {
    const redirect = sessionStorage.getItem('redirect');
    if (redirect) {
      sessionStorage.removeItem('redirect');
      navigate(redirect, { replace: true });
    }
  }, [navigate]);

  return (
    <ClerkProvider
      publishableKey={import.meta.env.NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY}
      routerPush={(to) => navigate(to)}
      routerReplace={(to) => navigate(to, { replace: true })}
      signInUrl='/sign-in'
      afterSignInUrl='/'
      afterSignUpUrl='/register-company'
    >
      <Toaster position="top-center" />
      <Routes>
        <Route path='/sign-in' element={<SignInPage />} />
        <Route path='/sign-up' element={<SignUpPage />} />
        <Route element={<BaseLayout />}>
          <Route element={<RequireAuth />}>
            <Route path='/' element={<App />} />
            <Route path='/register-company' element={<RegisterCompany />} />
            <Route path='/create-job' element={<CreateJobOffer />} />
            <Route path='/edit-job/:id' element={<EditJobOffer />} />
            <Route path='/job' element={<Jobs />} />
            <Route path='/job/:jobId/candidates' element={<CandidatesList />} />
            <Route path='/insights' element={<Insights />} />
            <Route path='/settings' element={<Settings />} />
            <Route path='/support' element={<Support />} />
          </Route>
        </Route>
      </Routes>
    </ClerkProvider>
  );
}

export default RootLayout;
