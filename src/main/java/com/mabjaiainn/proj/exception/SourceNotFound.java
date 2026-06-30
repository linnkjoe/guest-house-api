/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author linnkjoe
 */@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class SourceNotFound extends RuntimeException {

    public SourceNotFound(String message) {
        super(message);
    }
    
}