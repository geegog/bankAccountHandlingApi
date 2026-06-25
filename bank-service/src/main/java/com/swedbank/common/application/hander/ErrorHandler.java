package com.swedbank.common.application.hander;

import com.swedbank.common.application.Dto.ErrorResponse;
import com.swedbank.common.application.exception.BadRequestException;
import com.swedbank.common.application.exception.EntityAlreadyExistException;
import com.swedbank.common.application.exception.InsufficientException;
import com.swedbank.common.application.exception.NotFoundException;
import com.swedbank.common.application.exception.MismatchException;
import com.swedbank.common.application.exception.UnauthenticatedException;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ResponseEntity<Object> handleUnauthenticatedException(UnauthenticatedException ex) {
        return new ResponseEntity<>(new ErrorResponse(UNAUTHORIZED.value(), ex.getMessage()), UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(NOT_FOUND.value(), ex.getMessage()), NOT_FOUND);
    }

    @ExceptionHandler(EntityAlreadyExistException.class)
    @ResponseStatus(CONFLICT)
    public ResponseEntity<Object> handleEntityAlreadyExistException(EntityAlreadyExistException ex) {
        return new ResponseEntity<>(new ErrorResponse(CONFLICT.value(), ex.getMessage()), CONFLICT);
    }

    @ExceptionHandler({MismatchException.class, InsufficientException.class, BadRequestException.class})
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<Object> handleBadRequestException(MismatchException ex) {
        return new ResponseEntity<>(new ErrorResponse(BAD_REQUEST.value(), ex.getMessage()), BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        var errorList = ex
            .getBindingResult()
            .getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toList();

        return new ResponseEntity<>(new ErrorResponse(status.value(), String.join(",", errorList)), BAD_REQUEST);
    }
}
