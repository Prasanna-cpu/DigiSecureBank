package com.digisecurebank.payment_service.exceptions;

import com.digisecurebank.payment_service.response.ApiResponse;
import com.razorpay.RazorpayException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
                                                                  @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
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

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ApiResponse> handleObjectNotFoundException(ObjectNotFoundException ex) {
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ApiResponse> handleForbiddenActionException(ForbiddenActionException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
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

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ApiResponse> handleAccountNotActiveException(AccountNotActiveException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse> handleInsufficientBalanceException(InsufficientBalanceException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RazorpayException.class)
    public ResponseEntity<ApiResponse> handleRazorPayException(RazorpayException ex){
        ApiResponse apiResponse = new ApiResponse(
                ex.getLocalizedMessage(),
                null,
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

}
