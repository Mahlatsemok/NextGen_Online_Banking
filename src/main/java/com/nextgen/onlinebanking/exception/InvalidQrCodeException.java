package com.nextgen.onlinebanking.exception;

public class InvalidQrCodeException extends IllegalArgumentException {

    public InvalidQrCodeException(String message) {
        super(message);
    }
}
