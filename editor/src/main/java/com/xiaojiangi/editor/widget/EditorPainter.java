package com.xiaojiangi.editor.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.OverScroller;

import androidx.annotation.NonNull;

import com.xiaojiangi.editor.text.Text;
import com.xiaojiangi.editor.theme.AbstractColorTheme;
import com.xiaojiangi.editor.theme.EditorColorTheme;

public class EditorPainter {
    private final CodeEditor mEditor;
    private AbstractColorTheme mEditorColorTheme;
    private final OverScroller mOverScroller;
    private final Paint mPaint;
    private final Paint mNumberPaint;
    private final Paint mOtherPaint;
    private float spaceLength;
    private final float dpUnit;
    private int tabCount;
    private float tabWidth;
    private float offset;

    public EditorPainter(CodeEditor codeEditor) {
        this.mEditor = codeEditor;
        mOverScroller = mEditor.getOverScroller();
        mPaint = new Paint();
        mOtherPaint = new Paint();
        mNumberPaint = new Paint();
        mPaint.setAntiAlias(true);
        mOtherPaint.setAntiAlias(true);
        mNumberPaint.setAntiAlias(true);
        dpUnit = mEditor.getDpUnit();
        setSpaceWidth();
    }

    protected void onDraw(Canvas canvas) {
        var mText = mEditor.getContent();
        int visibleLineStart = Math.max((int) (mEditor.getOverScroller().getCurrY() / getLineHeight()), 0);
        int visibleLineEnd = Math.min(mText.size(), (int) ((mEditor.getHeight() + mEditor.getOverScroller().getCurrY()) / getLineHeight() + 1));

        /*
          行号偏移量
         */
        float lineNumberOffset = mPaint.measureText(String.valueOf(mText.size())) + (2 * mEditor.getDpUnit());

        /*
         行号背景偏移量
         */
        float lineNumberBackgroundOffset = lineNumberOffset + (8 * dpUnit);
        offset = lineNumberBackgroundOffset;
        /*
          行文本偏移量
         */
        float lineTextOffset = lineNumberBackgroundOffset + (2 * dpUnit);

        /*
         * 通过 (可视行*行高) 来计算行号背景和行号线的x轴
         */
        float start = visibleLineStart * getLineHeight();
        float end = visibleLineEnd * getLineHeight();

        //补全行号绘制部分
        if (visibleLineEnd == mText.size())
            end += canvas.getHeight();

        drawLineNumberBackground(start, end, lineNumberBackgroundOffset, canvas);
        drawCursorLine(visibleLineStart, visibleLineEnd, lineTextOffset, mText, canvas);
        drawLineNumberLine(start, end, lineNumberBackgroundOffset, canvas);
        drawLineNumberAndText(visibleLineStart, visibleLineEnd, lineNumberOffset, lineTextOffset, canvas);

    }

    /**
     * 绘制行号和文本内容
     *
     * @param lineStart    绘制的第一行
     * @param lineEnd      绘制的最后一行
     * @param numberOffset 行号偏移量
     * @param textOffset   文本偏移量
     * @param canvas       画布
     */
    protected void drawLineNumberAndText(int lineStart, int lineEnd, float numberOffset, float textOffset, Canvas canvas) {
        mNumberPaint.setColor(mEditorColorTheme.getColor(EditorColorTheme.LINE_NUMBER));
        mNumberPaint.setTextAlign(Paint.Align.RIGHT);
        mPaint.setColor(mEditorColorTheme.getColor(EditorColorTheme.TEXT_COLOR));
        mPaint.setTextAlign(Paint.Align.LEFT);
        for (int i = lineStart; i < lineEnd; i++) {
            /* 当前行文本基线 */
            var textBaseLine = (getLineHeight() * i) - mPaint.ascent();

            canvas.drawText(String.valueOf(i + 1), numberOffset, textBaseLine, mNumberPaint);
            var current = mEditor.getContent().get(i).toCharArray();
            canvas.drawText(current, 0, current.length, textOffset, textBaseLine, mPaint);
        }
    }

    protected void drawCursorLine(int visibleLineStart, int visibleLineEnd, float textOffset, Text text, Canvas canvas) {
        if (!(visibleLineStart <= text.getCursorLine() && text.getCursorLine() <= visibleLineEnd)) {
            return;
        }
        mOtherPaint.setColor(mEditorColorTheme.getColor(EditorColorTheme.CURRENT_LINE_BACKGROUND));
        float currX = mEditor.getOverScroller().getCurrX();
        canvas.drawRect(currX, getLineHeight() * text.getCursorLine(),
                currX + canvas.getWidth(),
                getLineHeight() * (text.getCursorLine() + 1),
                mOtherPaint);
        drawCursor(text.getCursorLine(), text.getCursorColumn(), textOffset, text, canvas);
    }

    protected void drawCursor(int cursorLine, int cursorColumn, float textOffset, Text text, Canvas canvas) {
        int tab = 0;
        float tabOffset = measureTextWidth(new char[]{'\t'});
        for (int i = 0; i < cursorColumn; i++) {
            if (text.get(cursorLine).charAt(i) == '\t')
                tab++;
        }
        float offset = (tab * tabWidth) - (tab * tabOffset);
        offset += textOffset + measureTextWidth(text.get(cursorLine).subSequence(0, cursorColumn).toCharArray());
        mOtherPaint.setColor(mEditorColorTheme.getColor(EditorColorTheme.CURSOR_COLOR));
        mOtherPaint.setStrokeWidth(4.6f);
        mOtherPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawLine(offset, cursorLine * getLineHeight(), offset, (cursorLine + 1) * getLineHeight(), mOtherPaint);

    }

    /**
     * 绘制行号背景
     *
     * @param top    开始位置
     * @param bottom 结束位置
     * @param offset 偏移
     */
    protected void drawLineNumberBackground(float top, float bottom, float offset, Canvas canvas) {
        if (mEditor.isFixedLineNumber())
            mOtherPaint.setAlpha(255);
        mOtherPaint.setColor(mEditorColorTheme.getColor(EditorColorTheme.LINE_NUMBER_BACKGROUND));
        mOtherPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawRect(0f, top, offset, bottom, mOtherPaint);
    }

    /**
     * 绘制行号线
     *
     * @param lineTopY    开始位置
     * @param lineBottomY 结束位置
     * @param offset      偏移
     */
    protected void drawLineNumberLine(float lineTopY, float lineBottomY, float offset, Canvas canvas) {
        mOtherPaint.setColor(mEditorColorTheme.getColor(EditorColorTheme.LINE_COLOR));
        mOtherPaint.setStrokeWidth(2);
        canvas.drawLine(offset, lineTopY, offset, lineBottomY, mOtherPaint);
    }

    /**
     * 绘制视图背景颜色
     */
    protected void drawViewBackground() {
        mEditor.setBackgroundColor(mEditorColorTheme.getColor(EditorColorTheme.CODE_BACKGROUND));
    }

    /**
     * 设置控件主题样式
     * @param colorTheme 主题
     */
    protected void setColorTheme(@NonNull AbstractColorTheme colorTheme) {
        mEditorColorTheme = colorTheme;
        drawViewBackground();
    }

    /**
     * 设置字体样式
     * @param typeface 字体样式
     */
    protected void setTextTypeface(@NonNull Typeface typeface) {
        mPaint.setTypeface(typeface);
        mNumberPaint.setTypeface(typeface);
    }

    /**
     * 设置字体大小
     * @param size 字体大小
     */
    public void setTextSize(float size) {
        mNumberPaint.setTextSize(size * dpUnit);
        mPaint.setTextSize(size * dpUnit);
    }

    /**
     * @see #setTabWidth(int)
     */
    protected float getTabWidth() {
        return tabWidth;
    }

    /**
     * 设置制表符宽度
     * @param tabSpaceCount 制表符空格数量
     */
    protected void setTabWidth(int tabSpaceCount) {
        tabCount = tabSpaceCount;
        tabWidth = tabSpaceCount * spaceLength;
    }

    /**
     * @return 得到制表符空格的数量
     */
    protected float getTabCount() {
        return tabCount;
    }

    /**
     * @see #setSpaceWidth()
     * 返回空格的宽度
     */
    protected float getSpaceWidth() {
        return spaceLength;
    }

    protected void setSpaceWidth() {
        spaceLength = mPaint.measureText(" ");
    }

    /**
     * 得到行的高度
     * @return 行高
     */
    protected float getLineHeight() {
        return mPaint.descent() - mPaint.ascent();
    }

    protected float measureTextWidth(char c) {
        return mPaint.measureText(new char[]{c}, 0, 1);
    }

    protected float measureTextWidth(char[] chars) {
        return mPaint.measureText(chars, 0, chars.length);
    }

    /**
     * 返回行号部分的偏移量
     *
     * @return 偏移量
     */
    protected float getOffset() {
        return offset;
    }


    /**
     * 获取可视行的第一行
     *
     * @return 可视行首行
     */
    protected int getVisibleLineStart() {
        return Math.max((int) (mEditor.getOverScroller().getCurrY() / getLineHeight()), 0);

    }

    /**
     * 获取可视行的最后一行
     *
     * @return 可视行尾行
     */
    protected int getVisibleLineEnd() {
        return Math.min(mEditor.getContent().size(), (int) ((mEditor.getHeight() + mEditor.getOverScroller().getCurrY()) / getLineHeight() + 1));

    }
}