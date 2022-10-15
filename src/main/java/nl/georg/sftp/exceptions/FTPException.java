package nl.georg.sftp.exceptions;

public class FTPException extends RuntimeException {

    public FTPException(String message,
                        Throwable cause) {
        super(message, cause);
    }
}
