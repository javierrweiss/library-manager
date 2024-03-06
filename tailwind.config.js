/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./target/generated-sources/tailwind/**/*.{html,js}"],
  theme: {
    extend: {},
    colors: {
      'slate-blue': '#6D72C3',
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
