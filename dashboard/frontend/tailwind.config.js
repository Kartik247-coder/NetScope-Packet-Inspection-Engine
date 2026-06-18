/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}"
  ],
  theme: {
    extend: {
      colors: {
        bg: "#0b1220",
        panel: "rgba(255,255,255,0.06)",
        border: "rgba(255,255,255,0.10)"
      }
    }
  },
  plugins: []
};

