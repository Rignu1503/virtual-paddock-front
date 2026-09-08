package com.virtual_paddock.backend.utils.exeption;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ErrorMessages {

    public static String IdNotFound(String entity) {
        final String message = "There are no records in the entity %s with id";
        return String.format(message, entity);
    }

    public static String alreadyExists(Object value) {
        return String.format("%s already exists in the database", value);
    }

    public static String NotFound(Object value) {
        return String.format("%s not exist in database", value);
    }

    public static String roleInvalidated(Object value) {
        return String.format("%s role is invalid", value);
    }

    public static String ShippingPersonType() {
        return String.format("Both cannot be the sender or the recipient");
    }

}
