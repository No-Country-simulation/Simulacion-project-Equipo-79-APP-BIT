import { useAuth } from '@clerk/react'
import { Navigate, Outlet } from 'react-router'

const RequireAuth = () => {
  const { isLoaded, isSignedIn } = useAuth();

  if (!isLoaded) {
    return null;
  }

  return isSignedIn ? <Outlet /> : <Navigate to="/sign-in" replace />
}

export default RequireAuth
