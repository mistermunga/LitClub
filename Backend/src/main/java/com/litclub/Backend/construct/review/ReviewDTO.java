package com.litclub.Backend.construct.review;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class ReviewDTO {
    private Book book;
    private User user;
    private Integer rating;
    private String content;
}
