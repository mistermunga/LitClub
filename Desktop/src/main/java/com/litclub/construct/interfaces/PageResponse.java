package com.litclub.construct.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic DTO for handling paginated responses from Spring's Page.
 *
 * <p>This class mirrors the structure of Spring Data's Page interface
 * to enable proper JSON deserialization on the client side.</p>
 *
 * @param <T> the type of content in the page
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    @JsonProperty("pageable")
    private Pageable pageable;

    @JsonProperty("totalElements")
    private long totalElements;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("last")
    private boolean last;

    @JsonProperty("first")
    private boolean first;

    @JsonProperty("size")
    private int size;

    @JsonProperty("number")
    private int number;

    @JsonProperty("numberOfElements")
    private int numberOfElements;

    @JsonProperty("empty")
    private boolean empty;

    // ==================== CONSTRUCTORS ====================

    public PageResponse() {
    }

    public PageResponse(List<T> content, int pageNumber, int pageSize,
                        long totalElements, int totalPages) {
        this.content = content;
        this.number = pageNumber;
        this.size = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.numberOfElements = content.size();
        this.empty = content.isEmpty();
        this.first = pageNumber == 0;
        this.last = pageNumber >= totalPages - 1;
    }

    // ==================== GETTERS & SETTERS ====================

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
        this.numberOfElements = content != null ? content.size() : 0;
        this.empty = content == null || content.isEmpty();
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Checks if there are more pages after this one.
     */
    public boolean hasNext() {
        return !last;
    }

    /**
     * Checks if there are previous pages before this one.
     */
    public boolean hasPrevious() {
        return !first;
    }

    /**
     * Gets the next page number, or current if this is the last page.
     */
    public int getNextPageNumber() {
        return hasNext() ? number + 1 : number;
    }

    /**
     * Gets the previous page number, or current if this is the first page.
     */
    public int getPreviousPageNumber() {
        return hasPrevious() ? number - 1 : number;
    }

    // ==================== NESTED PAGEABLE CLASS ====================

    /**
     * Simplified Pageable information from Spring's Pageable.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pageable {

        @JsonProperty("pageNumber")
        private int pageNumber;

        @JsonProperty("pageSize")
        private int pageSize;

        @JsonProperty("offset")
        private long offset;

        @JsonProperty("paged")
        private boolean paged;

        @JsonProperty("unpaged")
        private boolean unpaged;

        public Pageable() {
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public boolean isPaged() {
            return paged;
        }

        public void setPaged(boolean paged) {
            this.paged = paged;
        }

        public boolean isUnpaged() {
            return unpaged;
        }

        public void setUnpaged(boolean unpaged) {
            this.unpaged = unpaged;
        }
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "content size=" + (content != null ? content.size() : 0) +
                ", page=" + number +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                '}';
    }
}