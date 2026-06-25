import { useAuth } from '@clerk/react'
import { Navigate, Outlet } from 'react-router'

const RequireAuth = ({ children }) => {
  const { isLoaded, isSignedIn } = useAuth();

  if (!isLoaded) {
    return null;
  }

  return isSignedIn ? <Outlet /> : <Navigate to="/" replace />
}

export default RequireAuth