#!/usr/bin/env node
"use strict";

// Offline regression guard for the 1.1.3 UI patch. NO GitHub Actions needed.
// Run locally: node android/tests/check-fluidez-menu.js
const fs = require("node:fs");
const path = require("node:path");
const crypto = require("node:crypto");
const android = path.resolve(__dirname, "..");
const repoRoot = path.resolve(android, "..");
const file = (relative) => fs.readFileSync(path.join(repoRoot, relative));
const code = file("android/app/src/main/java/app/miturno/MainActivity.kt").toString("utf8");
const gradle = file("android/app/build.gradle.kts").toString("utf8");
function gitSha(buffer) {
  return crypto.createHash("sha1")
    .update(Buffer.from("blob " + buffer.length + "\0", "utf8"))
    .update(buffer).digest("hex");
}
const expected = {
  rules: "75a84b35096aad361116c92de145c7bf5bcb40e6",
  store: "adfe8a20c7a30def35c7bf7e5c1eebc92176985a"
};
let checks = 0;
function assert(ok, label) {
  if (!ok) throw new Error("FAIL: " + label);
  console.log("OK: " + label);
  checks++;
}
assert(
  gitSha(file("android/app/src/main/java/app/miturno/CalendarRules.kt")) === expected.rules,
  "Reglas, anclas y horarios originales intactos"
);
assert(
  gitSha(file("android/app/src/main/java/app/miturno/Store.kt")) === expected.store,
  "Persistencia de los datos laborales intacta"
);
assert(
  /versionCode\s*=\s*5/.test(gradle) && /versionName\s*=\s*"1\.1\.3"/.test(gradle),
  "Version de prueba 1.1.3 / codigo 5"
);
assert(code.includes('private fun menu() = openDrawer(if (second) "settings" else "extras")'),
  "Calendario 1 abre directamente Horas Extra y Calendario 2 sus ajustes"
);
assert(!code.includes("drawerMenuContents()") && !code.includes('Triple("▦", "Calendarios"'),
  "No existe la pantalla intermedia redundante de Calendarios"
);
assert(code.includes('private fun replaceCalendarSection(') &&
  code.includes('private fun refreshStripSelection('), "Actualizacion granular de secciones y tira");
const day = code.slice(code.indexOf("    private fun selectDate(d: LocalDate) {"),
  code.indexOf("    private fun lookup() {"));
assert(day.includes("replaceCalendarSection(lookupPanel)") &&
  day.includes("monthGrid?.invalidate()") &&
  day.includes("refreshStripSelection(previous, d)"),
  "Dia seleccionado actualiza tarjeta, mes y tira en el mismo arbol de vistas");
const body = code.slice(code.indexOf("    private fun render(animate:"), code.indexOf("    private fun menu()"));
assert(body.indexOf("setContentView(drawerHost)") > body.indexOf("root.addView(bottom)"),
  "La pantalla completa se construye antes de presentarse");
assert(!code.includes("body.alpha = 0f") && !code.includes(".alpha(0f)\n                .translationY"),
  "No se funde toda la interfaz a transparente");
assert(code.includes('heading(records, "Guardias trabajadas y fracciones")') &&
  code.indexOf('heading(records, "Guardias trabajadas y fracciones")') < code.indexOf('heading(rep, "Reporte de guardias y vacaciones")'),
  "Las guardias aparecen antes que los reportes, como en WebView");
assert(code.includes('memberEditor()\n        generalSettings()'),
  "Integrantes y ajustes nativos accesibles sin indice extra");
assert(code.includes('private fun drawerBack() {\n        if (drawerScreen != null) closeDrawer()'),
  "Atras de Android cierra directamente el panel");
console.log("UI source checks passed: " + checks);
