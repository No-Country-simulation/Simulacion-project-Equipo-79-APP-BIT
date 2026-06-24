package com.appbit.backend.core.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(
        name = "ErrorResponse",
        description = "Estructura estándar de error que retorna la API cuando algo falla. " +
                "Nunca expone stacktraces de Java al cliente."
)
public record ErrorResponse(

        @Schema(description = "Código HTTP del error", example = "400")
        int status,

        @Schema(description = "Descripción corta del tipo de error", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje legible del error", example = "El título de la vacante es obligatorio")
        String message,

        @Schema(description = "Errores por campo (solo en errores de validación)", nullable = true)
        Map<String, String> fieldErrors,

        @Schema(description = "Fecha y hora en que ocurrió el error")
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null, LocalDateTime.now());
    }

    public static ErrorResponse ofValidation(Map<String, String> fieldErrors) {
        return new ErrorResponse(400, "Validation Failed", "Uno o más campos son inválidos", fieldErrors, LocalDateTime.now());
    }
}