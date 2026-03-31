import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Toaster } from 'react-hot-toast'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
    <Toaster
      position="top-right"
      toastOptions={{
        style: {
          background: '#1e293b',
          color: '#f1f5f9',
          border: '1px solid #334155',
          fontSize: '14px',
        },
        success: { iconTheme: { primary: '#14b8a6', secondary: '#0f172a' } },
        error:   { iconTheme: { primary: '#f87171', secondary: '#0f172a' } },
      }}
    />
  </StrictMode>
)
