package com.cleverpos.common;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String,String>> status(ResponseStatusException e){
    return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason() == null ? "Request failed" : e.getReason())); }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException e){
    var f = e.getBindingResult().getFieldError();
    return ResponseEntity.badRequest().body(Map.of("message", f == null ? "Invalid data" : f.getField() + ": " + f.getDefaultMessage())); }
  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Map<String,String>> dup(DataIntegrityViolationException e){
    return ResponseEntity.status(409).body(Map.of("message", "Duplicate or invalid data")); }
}
