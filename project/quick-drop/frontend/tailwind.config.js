/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'dark-bg': '#0a0a0f',
        'dark-card': '#1a1a2e',
        'dark-card-light': '#161625',
        'flash-blue': '#3b82f6',
        'insight-purple': '#a855f7',
        'todo-green': '#22c55e',
      },
      boxShadow: {
        'glow-blue': '0 0 20px rgba(59, 130, 246, 0.5)',
        'glow-purple': '0 0 20px rgba(168, 85, 247, 0.5)',
        'glow-green': '0 0 20px rgba(34, 197, 94, 0.5)',
      }
    },
  },
  plugins: [],
}
