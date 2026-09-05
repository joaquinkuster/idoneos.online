# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 02 de septiembre de 2026  
**Versión anterior:** `estado_sistema_v.0.14.md` (02 de septiembre de 2026)  
**Versión actual:** `estado_sistema_v.0.15.md` (02 de septiembre de 2026)  

---

## 1. Resumen Ejecutivo

En la versión `v.0.15` se corrigieron los errores HTTP 400 en las descargas de reportes PDF y se rediseñó integralmente el módulo de emisión y renderizado de Certificados Digitales Académicos con el diseño formal Landscape aprobado en Figma Canvas:

1. **Resolución de HTTP ERROR 400 en Descarga de Informes (CU-96 y CU-97):**
   - **Diagnóstico:** Los endpoints `/reportes/alumnos/descargar` y `/reportes/ingresos/descargar` fallaban al persistir la entidad `Reporte` por falta de la relación obligatoria con la entidad `Curso` y el mapeo de `Administrador` desde el usuario autenticado.
   - **Corrección:** Sincronización en [ReportesService.java](file:///c:/Users/laza_/OneDrive/Desktop/idoneos.online/src/main/java/com/app/idoneos/service/modulo_reportes/ReportesService.java) y [ReportesController.java](file:///c:/Users/laza_/OneDrive/Desktop/idoneos.online/src/main/java/com/app/idoneos/controller/modulo_reportes/ReportesController.java) para persistir adecuadamente `(tipo, admin, curso)` sin generar excepciones ni interrumpir el streaming del PDF.

2. **Rediseño Integral de Certificados Digitales en Alta Fidelidad (CU-43 / CU-91):**
   - **Generador PDF ([CertificadoService.java](file:///c:/Users/laza_/OneDrive/Desktop/idoneos.online/src/main/java/com/app/idoneos/service/modulo_inscripciones/CertificadoService.java)):**
     - Formato apaisado **Landscape A4 (842 x 595 pt)** con doble marco perimetral institucional en Oro (`#D4A03D`) y Navy Oscuro (`#081426`).
     - Inclusión de encabezado institucional con logo nítido, avales académicos **FCEQyN — UNaM** y **Régimen de Idóneos CNV (Res. 19.340)**.
     - Tipografía formal con cuerpo de texto acreditativo, nombre del egresado, DNI, programa académico y carga horaria (120 horas cátedra).
     - Bloque de firmas formales de autoridades (*Lic. Fausto Spotorno* y *Lic. Lazaro Martinez*).
     - Pie de página con código QR dinámico integrado para verificación de autenticidad pública, número correlativo de registro digital y fecha en texto legal.
   - **Vista Web del Certificado ([certificado-vista.html](file:///c:/Users/laza_/OneDrive/Desktop/idoneos.online/src/main/resources/templates/pages/alumno/certificado-vista.html)):**
     - Creación de la pantalla web responsive que replica exactamente la estética Landscape del diploma con botones de descarga directa en PDF y enlace a validación pública.

3. **Verificación y Calidad de Código:**
   - Compilación exitosa con Maven (`mvn test-compile`) sin errores ni advertencias de tipos.

---

## 2. Detalle de Archivos Modificados y Creados

- `src/main/java/com/app/idoneos/service/modulo_reportes/ReportesService.java`
- `src/main/java/com/app/idoneos/controller/modulo_reportes/ReportesController.java`
- `src/main/java/com/app/idoneos/service/modulo_inscripciones/CertificadoService.java`
- `src/main/resources/templates/pages/alumno/certificado-vista.html`
- `docs/estado_sistema/estado_sistema_v.0.15.md`
