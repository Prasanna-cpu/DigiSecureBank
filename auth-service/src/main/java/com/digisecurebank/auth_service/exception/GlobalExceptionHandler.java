package com.digisecurebank.auth_service.exception;

import com.digisecurebank.auth_service.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMessage);
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUsernameNotFoundException(UsernameNotFoundException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ApiResponse> handleObjectNotFoundException(ObjectNotFoundException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictingResourcesException.class)
    public ResponseEntity<ApiResponse> handleConflictingResourcesException(ConflictingResourcesException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.CONFLICT,
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.UNPROCESSABLE_CONTENT,
                HttpStatus.UNPROCESSABLE_CONTENT.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

}
