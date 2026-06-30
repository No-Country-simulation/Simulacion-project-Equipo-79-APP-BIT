---
status: "proposed"
date: 2026-06-11
---

# Simulacion-project-Equipo-79-APP-BIT

| Miembro del Equipo | 
| :--- | 
| Anthony Anthony | 
| Milena | 
| Antonio Del Hierro | 
| Fernando | 
| Andrea | 
| Caleb | 

## Statement

El sistema deberá proporcionar una web app responsiva (PWA) B2B denominada App BiT, que funcione como una plataforma de matching inclusivo. El sistema deberá permitir a las empresas registrarse, publicar vacantes (incluyendo skills, nivel y región) y utilizar un endpoint `/match` para conectar con profesionales de grupos sub-representados, retornando una shortlist con score de compatibilidad y badge de diversidad. Además, el sistema deberá generar métricas básicas de diversidad para metas ESG.

## Rationale

Las empresas con metas ESG no logran encontrar y contratar talentos de grupos sub-representados de forma eficiente y sin sesgo, perdiendo diversidad e impacto real. Esta herramienta pone la diversidad en el centro de la estrategia de negocio — con datos reales, matching inteligente e impacto medible. No es solo una plataforma de reclutamiento ni solo un panel de métricas.

**Dolores reales que la solución aborda:**

| Dolor / Problema |
| :--- |
| Dificultad para encontrar candidatos calificados de grupos sub-representados. |
| Procesos de selección con sesgo inconsciente que perpetúan la exclusión. |
| Falta de datos confiables para basar decisiones de diversidad. |
| Presión creciente de inversores y reguladores por metas ESG medibles. |
| Desconocimiento de dónde están geográficamente los talentos disponibles. |

## Acceptance Criteria

El MVP del sistema se considerará exitoso cuando cumpla con los siguientes flujos y funcionalidades exigidas:

| ID | Criterio de Aceptación |
| :--- | :--- |
| **CA1** | La empresa se registra y configura su perfil de diversidad y metas ESG. |
| **CA2** | El sistema permite la publicación de vacante con skills, nivel y región, aplicando filtros de diversidad. |
| **CA3** | El sistema cuenta con un endpoint `/match` que retorna una shortlist con score de compatibilidad y badge de diversidad. |
| **CA4** | La interfaz es responsiva (PWA, funciona en celular y escritorio) e incluye una pantalla funcional de shortlist. |
| **CA5** | El reclutador puede seleccionar candidatos de la shortlist e iniciar el proceso de contacto. |
| **CA6** | El dashboard actualiza métricas básicas de diversidad en tiempo real. |
| **CA7** | El repositorio incluye un README con instrucciones de ejecución local y ejemplos de request/response. |

## Verification Method
Test | Demostración

## More Information

### Perfil del Usuario
Reclutadores, gestores de RR.HH. y líderes de diversidad en empresas que necesitan cumplir metas ESG, contratar talentos de grupos sub-representados y demostrar impacto real a stakeholders e inversores.

### Los 5 Servicios (Alcance de la Plataforma)

| Servicio | Descripción |
| :--- | :--- |
| **1. FORMACIONES** | Trayectorias de capacitación en diversidad e inclusión para equipos de RR.HH. y liderazgos corporativos. Contenidos que ayudan a la empresa a crear una cultura inclusiva desde adentro hacia afuera. |
| **2. EMPLEABILIDAD (MVP)** | Publicación de vacantes con matching inteligente con candidatos del módulo B2C. Score de compatibilidad entre perfil y vacante + badge de diversidad. Filtro anti-sesgo para reducir discriminación. |
| **3. EXPERIENCIAS ESTRUCTURANTES** | Eventos corporativos de diversidad — paneles y charlas con líderes de grupos sub-representados para inspirar la cultura interna y mostrar el impacto real. |
| **4. MENTORÍAS** | Conexión con líderes de diversidad en otras empresas para intercambio de buenas prácticas de inclusión. |
| **5. SALUD DEL EQUIPO** | Dashboard de bienestar de los colaboradores por perfil y región, alimentado por datos anonimizados del módulo B2C. |

### Dataset Vísent CDRView
Datos de concentración de personas por zona + cobertura de red ERB (5G/4G/3G) con coordenadas reales de antenas Anatel. Datos emulados con coordenadas reales. Disponible en: github.com/wongola-bit/appbit (incluye README y diccionario de columnas).

**Uso en este desafío:** Mapa de concentración de talentos por región — dónde hay personas calificadas de grupos sub-representados y cuál es la calidad de conectividad en esa zona. Permite a la empresa entender geográficamente dónde están los candidatos antes de publicar la vacante.

### Endpoints Principales

| Método | Endpoint | Request | Response |
| :--- | :--- | :--- | :--- |
| **POST** | `/match` | `{ empresa_id, vacante: { titulo, skills, nivel, region }, filtros: { anti_sesgo, diversidad_minima } }` | `{ candidatos: [{ candidato_id, nombre, score_match, badge_diversidad, skills, lat, lng }], total_analizados, diversidad_resultado }` |
| **GET** | `/insights` | N/A | `{ mapa_talentos: [{ region, concentracion, cobertura_red, perfiles_disponibles }] }` |

### Funcionalidades Opcionales

| Funcionalidad | Descripción |
| :--- | :--- |
| **Mapa Interactivo** | Vía Vísent CDRView — concentración de talentos por zona. |
| **Filtro Anti-sesgo** | Con explicabilidad (por qué el candidato fue o no seleccionado). |
| **Reporte de Diversidad** | Exportable en PDF para stakeholders. |
| **Dashboard** | Salud del equipo por perfil y región. |
| **Notificaciones** | Para reclutadores con nuevos candidatos compatibles. |
| **Integración B2C** | Para matching en tiempo real. |
| **Eventos** | Sección de eventos corporativos de diversidad. |

### Orientaciones Técnicas

| Categoría | Directriz |
| :--- | :--- |
| **Plataforma** | Web App Responsiva (PWA) — funciona en el celular y en el escritorio. Usa la tecnología que tu equipo ya domina (React, Vue, Node.js, Spring Boot, Python, Java u otra). |
| **Stack** | No es obligatorio — cada equipo elige lo que mejor conoce. |
| **Simplificaciones MVP** | El badge de diversidad puede ser un campo declarativo simple. El score anti-sesgo puede implementarse como funcionalidad opcional si el tiempo lo permite. |
| **Buenas Prácticas** | Comienza por el contrato de integración entre los miembros del equipo el Día 1. |
| **Seguridad** | Nunca subas credenciales o claves de API al repositorio. |
| **Deploy** | Railway o Render para el MVP. |
