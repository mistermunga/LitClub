package com.litclub.Backend.construct.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class ReviewDTO {
    private Long bookID;
    private Long userID;
    private Integer rating;
    private String content;
}
