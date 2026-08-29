/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        canvas: {
          light: "#F7F5EE",
          mint: "#E8F5E9",
          cream: "#FAF8F5",
          dark: "#121A15",
        },
        accent: {
          mint: "#56B386",
          mintLight: "#D7F5E4",
          mintDark: "#1E4833",
          lavender: "#866FB3",
          lavenderLight: "#E2E6FF",
          lavenderDark: "#383387",
          peach: "#FFB74D",
          peachLight: "#FFECC7",
          coral: "#FF8A65",
          teal: "#319795",
          slate: "#16211B",
          slateSecondary: "#3A4D42",
          muted: "#75857C",
          border: "#D8DFD5",
        }
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
        display: ['Outfit', 'Plus Jakarta Sans', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
