package com.pikapika.view;

import java.awt.Color;
import javax.swing.*;
import javax.swing.border.Border; // Import Border
import javax.swing.border.EmptyBorder; // Import EmptyBorder
import javax.swing.border.LineBorder;

/**
 * Represents a Pikachu icon on the game board.
 * Uses EmptyBorder to maintain consistent size and prevent layout shifts.
 */
public class Pikachu extends JButton {
    private int xPoint; // Row index
    private int yPoint; // Column index

    // --- Hằng số cho độ dày viền ---
    private static final int BORDER_THICKNESS = 3; // Độ dày viền (pixel)

    // --- Border mặc định (không vẽ gì nhưng giữ chỗ) ---
    private static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(
            BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS);

    /**
     * Constructor for Pikachu button.
     * @param x The row index (logical position).
     * @param y The column index (logical position).
     */
    public Pikachu(int x, int y) {
        super();
        this.xPoint = x;
        this.yPoint = y;
        // --- Đặt border mặc định khi khởi tạo ---
        // Điều này đảm bảo nút luôn chiếm đủ không gian cho viền,
        // ngay cả khi viền không được vẽ màu.
        this.setBorder(DEFAULT_BORDER);
    }

    /**
     * Gets the row index of this Pikachu.
     * @return The row index.
     */
    public int getXPoint() {
        return xPoint;
    }

    /**
     * Gets the column index of this Pikachu.
     * @return The column index.
     */
    public int getYPoint() {
        return yPoint;
    }

    /**
     * Draws a visible border around the Pikachu button with the specified color.
     * The thickness is defined by BORDER_THICKNESS.
     * @param color The color of the border.
     */
    public void drawBorder(Color color) {
        // Vẽ LineBorder với màu và độ dày đã định
        this.setBorder(new LineBorder(color, BORDER_THICKNESS));
    }

    /**
     * Removes the visible border by setting it back to the default EmptyBorder.
     * This ensures the button size remains consistent.
     */
    public void removeBorder() {
        // Đặt lại border về EmptyBorder mặc định
        this.setBorder(DEFAULT_BORDER);
    }

    /**
     * Gets a string representation of the Pikachu's position (index).
     * Useful for debugging purposes.
     * @return A string like "[Row=X, Col=Y]".
     */
    public String getIndex() {
        return "[Row=" + xPoint + ", Col=" + yPoint + "]";
    }

    /**
     * Overriding equals to compare Pikachu objects based on their position.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pikachu other = (Pikachu) obj;
        return xPoint == other.xPoint && yPoint == other.yPoint;
    }

    /**
     * Overriding hashCode is necessary when overriding equals.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + xPoint;
        result = 31 * result + yPoint;
        return result;
        // Hoặc dùng: return java.util.Objects.hash(xPoint, yPoint); // Java 7+
    }

    /**
     * Provides a string representation of the Pikachu object, including its position.
     */
    @Override
    public String toString() {
        return "Pikachu" + getIndex();
    }
}