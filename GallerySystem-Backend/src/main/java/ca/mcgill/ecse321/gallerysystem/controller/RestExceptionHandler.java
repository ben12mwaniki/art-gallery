package ca.mcgill.ecse321.gallerysystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import ca.mcgill.ecse321.gallerysystem.dto.ErrorResponseDto;
import ca.mcgill.ecse321.gallerysystem.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
                        IllegalArgumentException exception, WebRequest request) {

                ErrorResponseDto errorResponse = new ErrorResponseDto(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                exception.getMessage());
                errorResponse.setPath(request.getDescription(false));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
                        ResourceNotFoundException exception, WebRequest request) {

                ErrorResponseDto errorResponse = new ErrorResponseDto(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                exception.getMessage());
                errorResponse.setPath(request.getDescription(false));

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
                        MethodArgumentNotValidException ex, WebRequest request) {

                // Collect field errors with their messages
                Map<String, String> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                error -> error.getField(),
                                                error -> error.getDefaultMessage(),
                                                (msg1, msg2) -> msg1 + " | " + msg2 // in case of duplicate fields
                                ));

                // Create a summary message
                String summary = fieldErrors.values().stream().collect(Collectors.joining(", "));

                ErrorResponseDto errorResponse = new ErrorResponseDto(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                summary,
                                fieldErrors);
                errorResponse.setPath(request.getDescription(false));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDto> handleGenericException(
                        Exception exception, WebRequest request) {

                ErrorResponseDto errorResponse = new ErrorResponseDto(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred: " + exception.getMessage());
                errorResponse.setPath(request.getDescription(false));

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}