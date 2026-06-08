package com.transport.tms.GlobalException;

import com.transport.tms.Constant.ErrorVO;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(
                this.getErrorVO(HttpStatus.BAD_REQUEST.value(), ex.getBindingResult().getAllErrors()
                                .stream()
                                .map(o -> String.format("%s: %s", o.getObjectName(), o.getDefaultMessage()))
                                .collect(Collectors.toList()), request, INTERNAL_SERVER_ERROR
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest webRequest) {
        ex.printStackTrace();
        return ResponseEntity.badRequest().body(this.getErrorVO(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, webRequest, ex.getMessage()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(ApplicationException ex, WebRequest webRequest) {
        return ResponseEntity.badRequest().body(this.getErrorVO(ex.statusCode, ex.errors, webRequest, ex.errorMessage));
    }

    @ExceptionHandler(UNAuthorizedException.class)
    @ResponseStatus (HttpStatus.UNAUTHORIZED)
    public @ResponseBody Object handleUNAuthorizedException(UNAuthorizedException ex, WebRequest webRequest) {
        return this.getErrorVO(ex.getStatusCode(), null, webRequest, ex.getErrorMessage());
    }

    private ErrorVO getErrorVO(int statusCode, List<String> errors, WebRequest request, String message) {
        final ErrorVO errorVO = new ErrorVO();
        errorVO.setErrorCode(statusCode);
        errorVO.setErrors(errors);
        errorVO.setMessage(message);
        errorVO.setPath(this.getRequestURI(request));
        return errorVO;
    }

    private String getRequestURI(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
