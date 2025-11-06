package com.litclub.construct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reply extends Note {

    private Note parent;

    public Note getParent() {
        return parent;
    }

    public void setParent(Note parent) {
        this.parent = parent;
    }
}
