package io.hhplus.tdd;

import lombok.Builder;

public record ErrorResponse(
        String code,
        String message
) {
    @Builder(builderMethodName = "of")
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
