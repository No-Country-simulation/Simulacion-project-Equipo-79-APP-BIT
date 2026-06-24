package com.appbit.backend.modules.candidate.dto;

// =============================================================================
// CONTEXTO DEL PROYECTO — léelo antes de tocar este archivo
// =============================================================================
// Este proyecto es una plataforma de matching (emparejamiento) entre empresas
// y candidatos de LATAM. El backend está hecho en Spring Boot (Java).
//
// Spring Boot es un framework que nos permite crear APIs REST fácilmente.
// Una API REST es un servicio web: el frontend le hace preguntas (requests)
// y el backend le responde con datos (responses).
//
// El flujo general es:
//
//   Frontend (React)  ──request──>  Backend (Spring Boot)  ──consulta──>  Base de datos
//                     <──response──                         <──resultado──
//
// Este archivo vive en:
//   backend/src/main/java/com/appbit/backend/modules/candidate/dto/
//
// La carpeta "dto" contiene objetos que definen QUÉ datos enviamos al frontend.
// La carpeta "entity" contiene objetos que representan las tablas de la base de datos.
// =============================================================================

import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// =============================================================================
// ¿QUÉ ES UN DTO?
// =============================================================================
// DTO = Data Transfer Object (Objeto de Transferencia de Datos).
//
// Tenemos dos tipos de objetos en el backend:
//
//   1. ENTIDAD (Candidate.java):
//      Representa la tabla "candidate" en la base de datos.
//      Tiene TODOS los campos del candidato, incluidos datos sensibles
//      como nombre, email, género, etc.
//
//   2. DTO (este archivo):
//      Es lo que el backend le envía al frontend.
//      Solo incluye los campos que QUEREMOS mostrar. Es como un filtro.
//
// Ejemplo:
//   Entidad Candidate (base de datos)     AnonymousCandidateResponse (frontend ve)
//   ──────────────────────────────────    ────────────────────────────────────────
//   id:              42            →      candidateId:      42
//   nombre:          "Juan Pérez"  →      (NO incluido — dato personal)
//   email:           "j@mail.com"  →      (NO incluido — dato personal)
//   diversityBadge:  "LGBTQ+"      →      (NO incluido — puede causar sesgo)
//   skills:          ["Java","SQL"] →      skills:    ["Java", "SQL"]
//   experienceLevel: SENIOR         →      experienceLevel: SENIOR
//   latitude:        -27.413        →      latitude:  -27.413
//   longitude:       -48.475        →      longitude: -48.475
//
// ¿Por qué anonimizar? Por dos razones:
//   - LGPD: Ley brasileña de protección de datos (similar al GDPR europeo).
//     Prohíbe exponer datos personales innecesariamente.
//   - Anti-sesgo: Las empresas deben evaluar candidatos por sus habilidades,
//     no por su nombre, género o identidad.
// =============================================================================

// @Schema le dice a Swagger cómo describir este objeto en la documentación.
// Swagger es una herramienta que genera documentación interactiva automáticamente.
// La puedes ver en: http://localhost:8080/swagger-ui/index.html
@Schema(
        name = "AnonymousCandidateResponse",
        description = "Candidato con datos anonimizados. Cumple con la LGPD y principios anti-sesgo. " +
                "No expone nombre, email, género ni ningún dato personal identificable."
)

// =============================================================================
// ¿QUÉ ES UN record EN JAVA?
// =============================================================================
// Un "record" es un tipo especial de clase (disponible desde Java 16) que
// sirve para objetos que solo GUARDAN datos y no cambian después de crearse.
//
// Cuando escribes "record", Java genera automáticamente por ti:
//   - El constructor (para crear el objeto con los datos)
//   - Los getters (métodos para leer cada campo: candidateId(), skills(), etc.)
//   - equals(), hashCode() y toString() (métodos estándar de Java)
//
// Sin "record", tendrías que escribir decenas de líneas manualmente.
// Con "record", todo eso lo hace Java solo.
//
// Los campos del record se declaran entre los paréntesis, como parámetros.
// =============================================================================
public record AnonymousCandidateResponse(

        // ── CAMPO 1: ID del candidato ──────────────────────────────────────────
        // El ID que tiene en la base de datos. Solo se usa internamente para que
        // el motor de matching identifique a cada candidato en los resultados.
        // No revela información personal (un número no dice quién es la persona).
        // Long = número entero grande en Java (puede ser negativo o muy grande).
        @Schema(description = "Identificador del candidato en el sistema", example = "42")
        Long candidateId,

        // ── CAMPO 2: Habilidades técnicas ──────────────────────────────────────
        // Lista de habilidades del candidato. Ejemplos: "Java", "Python", "React".
        // List<String> = una lista de textos en Java.
        // @ArraySchema le dice a Swagger que este campo es una lista de strings.
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        // ── CAMPO 3: Nivel de experiencia ──────────────────────────────────────
        // Usamos ExperienceLevel (un enum) en lugar de String.
        //
        // ¿Qué es un enum? Es una lista CERRADA de opciones fijas.
        // ExperienceLevel solo acepta: JUNIOR, MID o SENIOR.
        // Si usáramos String, alguien podría poner "EXPERTO", "GOD", etc.,
        // y eso causaría errores. El enum lo previene.
        //
        // ExperienceLevel está definido en:
        //   modules/company/entity/ExperienceLevel.java
        @Schema(
                description = "Nivel de experiencia laboral del candidato",
                example = "SENIOR",
                allowableValues = {"JUNIOR", "MID", "SENIOR"}
        )
        ExperienceLevel experienceLevel,

        // ── CAMPOS 4 y 5: Coordenadas geográficas ──────────────────────────────
        // Latitud y longitud del candidato. Se usan para:
        //   - Mostrar candidatos en el mapa del frontend
        //   - Calcular cobertura regional en el módulo de Insights
        // double = número decimal en Java (ejemplo: -27.413)
        @Schema(description = "Latitud geográfica del candidato", example = "-27.413")
        double latitude,

        @Schema(description = "Longitud geográfica del candidato", example = "-48.475")
        double longitude

) {
    // =========================================================================
    // CONSTRUCTOR COMPACTO — validación defensiva
    // =========================================================================
    // En un record puedes escribir un "constructor compacto" para validar o
    // limpiar datos ANTES de que el objeto se termine de crear.
    //
    // PROBLEMA: Si un candidato en la base de datos no tiene habilidades
    // registradas, candidate.getSkills() puede devolver null (vacío total).
    // Si más adelante intentamos recorrer esa lista null, Java lanza
    // NullPointerException → el programa se rompe (crash).
    //
    // SOLUCIÓN: Si skills llega como null, lo reemplazamos con una lista vacía.
    // List.of() crea una lista vacía e inmutable (no se puede modificar).
    // Así nos garantizamos que skills NUNCA sea null en este objeto.
    // =========================================================================
    public AnonymousCandidateResponse {
        if (skills == null) {
            skills = List.of(); // Lista vacía en lugar de null → sin crashes
        }
    }

    // =========================================================================
    // MÉTODO DE FÁBRICA: from(Candidate candidate)
    // =========================================================================
    // Este método convierte una entidad Candidate (que viene de la BD)
    // en este DTO (lo que enviaremos al frontend).
    // Es el "traductor" entre la base de datos y la API.
    //
    // ¿Por qué "static"?
    //   Un método static pertenece a la CLASE, no a un objeto en particular.
    //   Se puede llamar sin crear primero un AnonymousCandidateResponse:
    //     AnonymousCandidateResponse dto = AnonymousCandidateResponse.from(candidato);
    //
    // ¿Por qué se llama "from"?
    //   Es una convención en Java para métodos que transforman un tipo en otro.
    //   Otros ejemplos en Java: LocalDate.from(...), Instant.from(...).
    //
    // Flujo de uso en el proyecto:
    //   1. CandidateController recibe un request del frontend
    //   2. Consulta la BD → obtiene una entidad Candidate con todos sus datos
    //   3. Llama a AnonymousCandidateResponse.from(candidate)
    //   4. Este método "filtra" los datos y devuelve solo lo permitido
    //   5. El controller envía ese DTO al frontend (sin datos personales)
    // =========================================================================
    public static AnonymousCandidateResponse from(Candidate candidate) {

        // Construimos el DTO mapeando campo a campo desde la entidad.
        // Solo copiamos los datos técnicos — los personales se quedan en la entidad.
        return new AnonymousCandidateResponse(
                candidate.getId(),              // id de la BD  →  candidateId del DTO
                candidate.getSkills(),          // List<String> ya es String, copia directa
                candidate.getExperienceLevel(), // ExperienceLevel enum, copia directa
                candidate.getLatitude(),        // double, copia directa
                candidate.getLongitude()        // double, copia directa
        );
    }
}