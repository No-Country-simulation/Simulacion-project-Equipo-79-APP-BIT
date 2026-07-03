import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { HashRouter } from "react-router";
import RootLayout from './RootLayout.jsx';
import './index.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <HashRouter>
      <RootLayout />
    </HashRouter>
  </StrictMode>,
)
