package com.hsms.categoryservice.exception;


public class DuplicateCategoryException extends RuntimeException {

    public DuplicateCategoryException(String msg) {
        super(msg);
    }
}