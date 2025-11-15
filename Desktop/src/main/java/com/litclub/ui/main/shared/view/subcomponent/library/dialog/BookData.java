package com.litclub.ui.main.shared.view.subcomponent.library.dialog;

import com.litclub.construct.enums.BookStatus; /**
 * Data class for book creation form.
 */
public record BookData(
        String title,
        String author,
        String isbn,
        BookStatus status,
        String notes
) {}
