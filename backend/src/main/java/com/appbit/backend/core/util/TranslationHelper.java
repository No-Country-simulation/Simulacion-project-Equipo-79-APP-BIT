package com.appbit.backend.core.util;

public class TranslationHelper {

    public static String translateBadge(String badge) {
        if (badge == null) return null;
        return switch (badge.toUpperCase()) {
            case "TALENTO_REGIONAL" -> "REGIONAL_TALENT";
            case "TALENTO_RURAL" -> "RURAL_TALENT";
            case "TALENTO_JOVEN" -> "YOUNG_TALENT";
            case "TALENTO_SENIOR" -> "SENIOR_TALENT";
            case "TALENTO_RECONVERSION" -> "RECONVERSION_TALENT";
            case "MUJER_STEM" -> "STEM_WOMAN";
            default -> badge;
        };
    }

    public static String translateStatus(String status) {
        if (status == null) return null;
        return switch (status.toUpperCase()) {
            case "CUMPLIDA" -> "ACHIEVED";
            case "EN_PROGRESO" -> "IN_PROGRESS";
            case "NO_ALCANZADA" -> "NOT_ACHIEVED";
            default -> status;
        };
    }
}
