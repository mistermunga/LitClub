package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite primary key for {@link com.litclub.Backend.entity.UserBook}.
 *
 * <p>Defines the unique pairing of a User and a Book in the user’s library.</p>
 */
@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class UserBookID implements Serializable {

    @Column(name = "user_id")
    private Long userID;

    @Column(name = "book_id")
    private Long bookID;
}
