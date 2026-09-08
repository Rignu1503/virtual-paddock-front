package com.virtual_paddock.backend.api.error_handler;

import com.virtual_paddock.backend.api.dtos.errors.BaseErrorResponse;
import com.virtual_paddock.backend.api.dtos.errors.ErrorsResp;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadResquestController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handleBadRequest(MethodArgumentNotValidException exception) {
        List<String> errors = new ArrayList<>();
        exception.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

        return ErrorsResp.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    public BaseErrorResponse badRequest(BadRequestException exception) {
        List<String> errors = new ArrayList<>();
        errors.add(exception.getMessage());

        return ErrorsResp.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(org.apache.coyote.BadRequestException.class)
    public BaseErrorResponse badRequestCoyote(org.apache.coyote.BadRequestException exception) {
        List<String> errors = new ArrayList<>();
        errors.add(exception.getMessage());

        return ErrorsResp.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public BaseErrorResponse handleAccessDenied(org.springframework.security.access.AccessDeniedException exception) {
        List<String> errors = new ArrayList<>();
        errors.add("Acceso denegado: No tienes permisos suficientes para realizar esta acción.");

        return ErrorsResp.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .status(HttpStatus.FORBIDDEN.name())
                .errors(errors)
                .build();
    }
}
