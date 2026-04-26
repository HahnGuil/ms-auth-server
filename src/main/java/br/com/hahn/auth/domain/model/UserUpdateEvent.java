package br.com.hahn.auth.domain.model;

import lombok.Data;

/**
 * Represents an event for user updates that need to be synchronized across applications.
 *
 * @author HahnGuil
 * <p>This class contains the email of the user and the UUID of the application
 * that needs to be notified about the user update.</p>
 */
@Data
public class UserUpdateEvent {

    /**
     * The email address of the user being updated.
     */
    private String email;

    /**
     * The UUID of the application that needs to be notified about this update.
     */
    private String applicationName;
}

