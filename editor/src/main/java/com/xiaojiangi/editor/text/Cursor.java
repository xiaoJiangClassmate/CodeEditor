package com.xiaojiangi.editor.text;

public final class Cursor {
    private final Text text;
    /**
     * 光标所在的行
     */
    public int line;
    /**
     * 光标前一个字符的索引
     */
    public int column;

    public Cursor(Text text) {
        this.text = text;
        line = column = 0;
    }

    /**
     * 设置光标所在位置
     *
     * @param line   光标所在行的位置
     * @param column 光标前一个字符的索引
     */
    public void set(int line, int column) {
        if (line < 0 || column < 0)
            return;
        this.line = line;
        this.column = column;
    }

    /**
     * 光标左移一个字符
     */
    public void moveToLeft() {
        if (column == 0)
            return;
        column--;
    }

    /**
     * 光标右移一个字符
     */
    public void moveToRight() {
        if (column == text.get(line).length())
            return;
        column++;
    }

    /**
     * 光标往上移动一行
     */
    public void moveToTop() {
        if (line == 0)
            return;

        line--;
        int length = text.get(line).length();
        if (length < column)
            column = length;
    }

    /**
     * 光标往下移动一行
     */
    public void moveToBottom() {
        if (text.size() == line + 1)
            return;

        line++;
        int length = text.get(line).length();
        if (length < column)
            column = length;

    }

}
