
# Simulacion-project-Equipo-79-APP-BIT

| Miembro del Equipo | 
| :--- | 
| Anthony Parra | 
| Milena Moran | 
| Antonio Del Hierro | 
| Fernando Rodríguez | 
| Andrea Fernández | 
| Caleb Seña| 

## Cómo correr el proyecto localmente

Instrucciones detalladas y ejemplos de request/response en:

- [`backend/README.md`](backend/README.md) — cómo levantar la API (con o sin base de datos externa), cómo configurar tu propia clave de IA sin tocar el repo, y ejemplos `curl` de los endpoints principales.
- [`frontend/README.md`](frontend/README.md) — variables de entorno y comandos de desarrollo.

Resumen rápido:

```bash
# Backend (perfil local, sin DB externa ni clave de IA obligatoria)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# arriba en http://localhost:8080, Swagger en /swagger-ui.html

# Frontend
cd frontend
npm install
npm run dev
# arriba en http://localhost:5173 (o el puerto que indique Vite)
```

## Alcance 

El sistema proporciona una web app responsiva (PWA) B2B denominada App BiT, que funciona como una plataforma de matching inclusivo. El sistema permite a las empresas registrarse, publicar vacantes (incluyendo skills, nivel y región) y utilizar un endpoint `/match` para conectar con profesionales de grupos sub-representados, retornando una shortlist con score de compatibilidad y badge de diversidad. Además, el sistema genera métricas básicas de diversidad para metas ESG.

## Problemática 

Las empresas con metas ESG no logran encontrar y contratar talentos de grupos sub-representados de forma eficiente y sin sesgo, perdiendo diversidad e impacto real. Esta herramienta pone la diversidad en el centro de la estrategia de negocio — con datos reales, matching inteligente e impacto medible. No es solo una plataforma de reclutamiento ni solo un panel de métricas.

**Dolores reales que la solución aborda:**

| Dolor / Problema |
| :--- |
| Dificultad para encontrar candidatos calificados de grupos sub-representados. |
| Procesos de selección con sesgo inconsciente que perpetúan la exclusión. |
| Falta de datos confiables para basar decisiones de diversidad. |
| Presión creciente de inversores y reguladores por metas ESG medibles. |
| Desconocimiento de dónde están geográficamente los talentos disponibles. |

## Criterios 

El MVP del sistema cumplió con los siguientes flujos y funcionalidades exigidas:

| ID | Criterio de Aceptación |
| :--- | :--- |
| **CA1** | La empresa se registra y configura su perfil de diversidad y metas ESG. |
| **CA2** | El sistema permite la publicación de vacante con skills, nivel y región, aplicando filtros de diversidad. |
| **CA3** | El sistema cuenta con un endpoint `/match` que retorna una shortlist con score de compatibilidad y badge de diversidad. |
| **CA4** | La interfaz es responsiva (PWA, funciona en celular y escritorio) e incluye una pantalla funcional de shortlist. |
| **CA5** | El reclutador puede seleccionar candidatos de la shortlist e iniciar el proceso de contacto. |
| **CA6** | El dashboard actualiza métricas básicas de diversidad en tiempo real. |
| **CA7** | El repositorio incluye un README con instrucciones de ejecución local y ejemplos de request/response. |


# Funcionalidades 

### Perfil del Usuario 
Reclutadores, gestores de RR.HH. y líderes de diversidad en empresas que necesitan cumplir metas ESG, contratar talentos de grupos sub-representados y demostrar impacto real a stakeholders e inversores.

### Dataset Vísent CDRView
Se usan datos de concentración de personas por zona + cobertura de red ERB (5G/4G/3G) con coordenadas reales de antenas de la Anatel. Datos emulados con coordenadas reales obtenidos del siguiente repositorio: github.com/wongola-bit/appbit.
Para mostrar mapa de concentración de talentos por región, dónde hay personas calificadas de grupos sub-representados y cuál es la calidad de conectividad en esa zona. Permite a la empresa entender geográficamente dónde están los candidatos antes de publicar la vacante.

### Endpoints Principales
Se pueden ver en la siguiente documentación:
https://tonyy1-bit.hf.space/swagger-ui/index.html

### Funcionalidades Adicionales

| Funcionalidad | Descripción |
| :--- | :--- |
| **Mapa Interactivo** | Vía Vísent CDRView — concentración de talentos por zona. |
| **Filtro Anti-sesgo** | Con explicabilidad (por qué el candidato fue o no seleccionado). |
| **Reporte de Diversidad** | Visualización de badge de diversidad para stakeholders. |

### Orientaciones Técnicas

| Categoría | Directriz |
| :--- | :--- |
| **Plataforma** | Web App Responsiva (PWA) — funciona en el celular y en el escritorio. Usa la tecnología que tu equipo ya domina (React, Spring Boot). |
| **Stack** |JavaScript, Java, React, Tailwind CSS, Spring Boot, PostgreSQL, Docker, Miro, Swagger UI, Claude Code|
| **Buenas Prácticas** | Se implementó el marco de trabajo SCRUM desarrollando 3 ceremonias por Sprint: Planning, Daily y Review Meet |
| **Seguridad** | Nunca subas credenciales o claves de API al repositorio. |
| **Deploy** | Render para el MV. |
