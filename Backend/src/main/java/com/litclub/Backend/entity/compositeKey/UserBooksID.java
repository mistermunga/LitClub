package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class UserBooksID implements Serializable {

    @Column(name = "user_id")
    private Long userID;

    @Column(name = "book_id")
    private String bookID;
}
