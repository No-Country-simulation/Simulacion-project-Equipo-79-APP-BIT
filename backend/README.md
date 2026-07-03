---
title: Crm
emoji: 👀
colorFrom: gray
colorTo: red
sdk: docker
app_port: 7860
pinned: false
---

# Backend — App BiT

API REST en Spring Boot 3 / Java 21 para la plataforma de matching inclusivo. Expone la gestión de empresas, vacantes, candidatos, matching con IA, reclutamiento e insights de diversidad.

## Requisitos

- Java 21
- No necesitas Maven instalado: usa el wrapper incluido (`./mvnw`).

## Correr en local (sin base de datos externa)

El perfil `local` levanta el backend con una base H2 en memoria (se resiembra en cada arranque con `data.sql`) y con el agente de IA deshabilitado por defecto, así que funciona sin ninguna clave configurada:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

El backend queda arriba en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

Para correr contra Postgres (perfil por defecto, sin `-Dspring-boot.run.profiles=local`) necesitas exportar antes:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/appbit
export DB_USERNAME=postgres
export DB_PASSWORD=tu-password
export SPRING_AI_OPENAI_API_KEY=tu-clave
./mvnw spring-boot:run
```

## Configurar tu propia clave de IA (sin tocar el repo)

El matching (`POST /jobs/matches` / `POST /match`) usa Spring AI apuntando a un endpoint compatible con OpenAI. **No hace falta pedir la clave de nadie más ni editar ningún archivo del repo** — cada quien exporta sus propias variables de entorno antes de levantar el backend:

```bash
export SPRING_AI_OPENAI_API_KEY=tu-clave
export SPRING_AI_OPENAI_BASE_URL=https://tu-proveedor       # sin "/v1" al final, Spring AI lo agrega solo
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=tu-modelo
```

Si no exportas nada, usa por defecto Gemini (`gemini-2.5-flash`) — o, en el perfil `local`, directamente cae al matching de respaldo por reglas (no rompe nada si no tienes clave).

Ejemplos de proveedores compatibles:

| Proveedor | `SPRING_AI_OPENAI_BASE_URL` | `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` |
| :--- | :--- | :--- |
| Google Gemini (default) | `https://generativelanguage.googleapis.com/v1beta/openai/` | `gemini-2.5-flash` |
| NVIDIA build (DeepSeek u otros) | `https://integrate.api.nvidia.com` | `deepseek-ai/deepseek-v4-pro` (o el que corresponda a tu clave) |
| OpenCode Zen | `https://opencode.ai/zen` | el model id que te muestre tu cuenta |

**Nota para modelos "de razonamiento" (ej. `deepseek-v4-pro` en NVIDIA build):** si el modelo tarda demasiado o nunca responde, puede necesitar desactivar el modo "thinking" con una propiedad extra. Esto no se puede pasar por variable de entorno (Spring no arma bien un mapa anidado desde línea de comandos); agrégala en tu propio `backend/src/main/resources/application-local.properties` local (no lo subas) o pásala con `--spring.config.additional-location`:

```properties
spring.ai.openai.chat.options.extra-body.chat_template_kwargs.thinking=false
```

Si el proveedor falla, tarda más de 35 segundos o responde con un JSON inválido, el endpoint **no se cae**: calcula un score de respaldo por reglas (solapamiento de skills + nivel de experiencia + región) y sigue devolviendo 200. Ver `MatchingAgentService.buildFallbackResults()`.

## Endpoints principales (ejemplos)

```bash
# Registrar empresa
curl -X POST http://localhost:8080/companies \
  -H "Content-Type: application/json" \
  -d '{"name":"TechSolutions S.A.","industrySector":"Tecnología","esgGoals":"30% diversidad en cargos técnicos"}'

# Crear vacante
curl -X POST http://localhost:8080/jobs \
  -H "Content-Type: application/json" \
  -d '{"title":"Desarrollador Backend","experienceLevel":"MID","region":"Florianopolis","requiredSkills":["Java","Spring Boot"],"companyId":1}'

# Buscar candidatos compatibles (matching con IA + fallback automático)
# POST /jobs/matches y POST /match hacen exactamente lo mismo — /match existe como
# alias porque así lo pide el enunciado del MVP.
curl -X POST http://localhost:8080/match \
  -H "Content-Type: application/json" \
  -d '{"title":"Desarrollador Backend","experienceLevel":"MID","region":"Florianopolis","skills":["Java","Spring Boot"]}'

# Respuesta (array plano, un objeto por candidato):
# [
#   {
#     "candidateId": 189,
#     "compatibilityScore": 100,
#     "diversityScore": 0,
#     "matchingSkills": ["Java", "Spring Boot"],
#     "inclusionReason": "Cumple 2/2 skills requeridos...",
#     "diversityBadge": null
#   }
# ]

# Métricas de diversidad para el dashboard
curl http://localhost:8080/dashboard/esg

# Mapa de concentración de talento por región
curl http://localhost:8080/insights

# Iniciar contacto con un candidato
curl -X POST http://localhost:8080/recruitment \
  -H "Content-Type: application/json" \
  -d '{"jobId":1,"candidateId":189}'
```

> **Nota sobre el contrato de `/match`:** el `README.md` raíz del proyecto documenta un formato de respuesta con wrapper (`{ candidatos: [...], total_analizados, diversidad_resultado }`) y expone `nombre` del candidato. La implementación real usa un array plano y **nunca expone nombre ni ningún dato identificable del candidato** — es una anonimización deliberada para que el agente de IA no pueda sesgarse por datos personales al calcular el score. Si necesitas el wrapper exacto del spec original, es un cambio de contrato a decidir en equipo (no solo agregarlo, porque implicaría exponer `nombre`).
