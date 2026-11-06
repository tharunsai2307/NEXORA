/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        industrial: {
          950: '#06090e',
          900: '#0b111a',
          850: '#111827',
          800: '#1a2333',
          700: '#26344d',
          600: '#394d6e',
          border: '#1e293b',
          surface: '#0f172a',
          hover: '#1e293b',
        },
        accent: {
          cyan: '#06b6d4',
          emerald: '#10b981',
          amber: '#f59e0b',
          rose: '#ef4444',
          indigo: '#6366f1'
        }
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Consolas', 'Menlo', 'monospace'],
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
