/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./target/generated-sources/tailwind/**/*.{html,js}"],
  theme: {
    extend: {},
    colors: {
      'slate-blue': '#6D72C3',
      'pale-purple': '#E5D4ED',
      'rebecca-purple': '#5941A9',
      'davys-gray': '#514F59',
      'dark-purple': '#1D1128',
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
