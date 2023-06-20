/** @type {import('tailwindcss').Config} */
const config = {
  content: ['./index.hmtl', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {},
    fontFamily: {
      sans: ['Rowdies', 'sans-serif'],
    },
  },
  plugins: [require('daisyui')],
  darkMode: 'class',
};

module.exports = config;
