// import { defineConfig } from "cypress";

// export default defineConfig({
//   e2e: {
//     setupNodeEvents(on, config) {
//       // implement node event listeners here
//     },
//   },
// });
import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    baseUrl: "http://localhost:5173",  // 👈 ใส่ baseUrl ให้ Cypress รู้ว่าจะเปิดเว็บที่ไหน
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
