package uz.uptimehub.resource.dto.exception;

public class RequiredSpecificationNotAvailableException extends RuntimeException {
    public RequiredSpecificationNotAvailableException(String message) {
        super(message);
    }
}
