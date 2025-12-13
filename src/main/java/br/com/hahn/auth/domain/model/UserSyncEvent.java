package br.com.hahn.auth.domain.model;

import lombok.Data;

/**
 * Represents an event for synchronizing user data between systems.
 *
 * @author HahnGuil
 * <p>This class contains information about the user and the application
 * involved in the synchronization process.</p>
 */
@Data
public class UserSyncEvent {

    /**
     * The unique identifier of the user.
     */
    private String uuid;

    /**
     * The code of the application associated with the synchronization event.
     */
    private String applicationCode;
}
