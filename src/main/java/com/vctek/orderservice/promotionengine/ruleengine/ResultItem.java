package com.vctek.orderservice.promotionengine.ruleengine;

import java.io.Serializable;

public class ResultItem implements Serializable {
    private String message;
    private MessageLevel level;
    private String path;
    private int line;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageLevel getLevel() {
        return level;
    }

    public void setLevel(MessageLevel level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
