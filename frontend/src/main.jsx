import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from "react-router";
import RootLayout from './RootLayout.jsx';
import './index.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <RootLayout />
    </BrowserRouter>
  </StrictMode>,
)
