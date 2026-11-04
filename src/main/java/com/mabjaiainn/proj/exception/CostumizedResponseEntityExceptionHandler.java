/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * @author linnkjoe
 */
@ControllerAdvice
public class CostumizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler{
    
        @ExceptionHandler(Exception.class)
        public final ResponseEntity<ErrorDetails> handleAllException(Exception ex,
                WebRequest request) throws Exception{
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                ex.getMessage(), request.getDescription(false));
                return new ResponseEntity<ErrorDetails>(errorDetails, 
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Sem isto, o handler generico acima transformaria 403/401 em 500.
        @ExceptionHandler(AccessDeniedException.class)
        public final ResponseEntity<ErrorDetails> handleAccessDenied(Exception ex,
                WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                "Sem permissao para esta acao", request.getDescription(false));
                return new ResponseEntity<ErrorDetails>(errorDetails,
                        HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(AuthenticationException.class)
        public final ResponseEntity<ErrorDetails> handleAuthentication(Exception ex,
                WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                "Credenciais invalidas", request.getDescription(false));
                return new ResponseEntity<ErrorDetails>(errorDetails,
                        HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(SourceNotFound.class)
        public final ResponseEntity<ErrorDetails> handleUserNotFoundException(Exception ex,
                WebRequest request) throws Exception{
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                ex.getMessage(), request.getDescription(false));
                return new ResponseEntity<ErrorDetails>(errorDetails, 
                        HttpStatus.NOT_FOUND);
        }
        
        @ExceptionHandler(BusinessException.class)
        public final ResponseEntity<ErrorDetails> handleUserBusinessException(BusinessException ex,
                WebRequest request) throws Exception{
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                ex.getMessage(), request.getDescription(false));
                return new ResponseEntity<ErrorDetails>(errorDetails, 
                        HttpStatus.BAD_REQUEST);
        }
        
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request){
                ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                "Total Errors: " +ex.getErrorCount() + " First Error: " + ex.getFieldError().getDefaultMessage(), request.getDescription(false));
                return new ResponseEntity<>(errorDetails, 
                        HttpStatus.BAD_REQUEST);
        }
        

}
