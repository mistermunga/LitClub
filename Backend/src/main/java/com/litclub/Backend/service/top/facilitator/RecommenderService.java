package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.repository.UserBooksRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommenderService {

    private final UserBooksRepository userBooksRepository;

    public RecommenderService(
            UserBooksRepository userBooksRepository
    ) {
        this.userBooksRepository = userBooksRepository;
    }

    @Transactional(readOnly = true)
    public Page<Book> findRecommendedBooks(User user, Pageable pageable) {
        return userBooksRepository.findRecommendedBooks(user, pageable);
    }
}
