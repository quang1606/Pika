package com.pikapika.view;

import java.awt.Color;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.Objects; // Import Objects for hashCode

/**
 * Represents a Pikachu icon on the game board.
 * Stores its position (row, col) and image type index.
 * Uses EmptyBorder to maintain consistent size and prevent layout shifts.
 */
public class Pikachu extends JButton {
    private int xPoint; // Row index (hàng)
    private int yPoint; // Column index (cột)
    private int imageIndex; // <<< THÊM: Chỉ số loại hình ảnh (ví dụ: 1, 2, ...)

    // --- Hằng số cho độ dày viền ---
    private static final int BORDER_THICKNESS = 3; // Độ dày viền (pixel)

    // --- Border mặc định (không vẽ gì nhưng giữ chỗ) ---
    private static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(
            BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS);

    /**
     * Constructor for Pikachu button.
     * @param x The row index (logical position).
     * @param y The column index (logical position).
     * @param imageIndex The index representing the type of Pikachu image. << THÊM THAM SỐ NÀY
     */
    public Pikachu(int x, int y, int imageIndex) { // <<< THÊM THAM SỐ imageIndex
        super();
        this.xPoint = x;
        this.yPoint = y;
        this.imageIndex = imageIndex; // <<< LƯU LẠI imageIndex
        // --- Đặt border mặc định khi khởi tạo ---
        this.setBorder(DEFAULT_BORDER);
        // Các cài đặt khác cho JButton nếu cần (setContentAreaFilled, setFocusPainted...)
        this.setContentAreaFilled(false);
        this.setFocusPainted(false);
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
     * Gets the image type index of this Pikachu.
     * This is used to check if two Pikachus match.
     * @return The image type index (int).
     */
    public int getIndex() { // <<< SỬA: TRẢ VỀ SỐ NGUYÊN imageIndex
        return this.imageIndex;
    }

    /**
     * Sets the image type index for this Pikachu.
     * Useful for updating the button state without creating a new object (e.g., in updateMap).
     * @param imageIndex The new image type index.
     */
    public void setImageIndex(int imageIndex) { // <<< THÊM: Phương thức để cập nhật index nếu cần
        this.imageIndex = imageIndex;
    }


    /**
     * Draws a visible border around the Pikachu button with the specified color.
     * The thickness is defined by BORDER_THICKNESS.
     * @param color The color of the border.
     */
    public void drawBorder(Color color) {
        this.setBorder(new LineBorder(color, BORDER_THICKNESS));
    }

    /**
     * Removes the visible border by setting it back to the default EmptyBorder.
     * This ensures the button size remains consistent.
     */
    public void removeBorder() {
        this.setBorder(DEFAULT_BORDER);
    }

    /**
     * Gets a string representation of the Pikachu's position.
     * Useful for debugging purposes.
     * @return A string like "[Row=X, Col=Y]".
     */
    public String getCoordinateString() { // <<< ĐỔI TÊN: Phương thức cũ trả về tọa độ
        return "[Row=" + xPoint + ", Col=" + yPoint + "]";
    }

    /**
     * Overriding equals to compare Pikachu objects based on their position AND image index.
     * Two Pikachu buttons are considered equal if they are at the same position
     * AND represent the same image type. (Adjust if only position matters for equality)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pikachu other = (Pikachu) obj;
        // Quyết định xem 'equals' nên dựa vào vị trí hay cả vị trí và index
        // Thông thường, chỉ cần vị trí là đủ để xác định một button duy nhất trên bảng
        return xPoint == other.xPoint && yPoint == other.yPoint;
        // Nếu muốn cả index phải giống nhau mới là equals:
        // return xPoint == other.xPoint && yPoint == other.yPoint && imageIndex == other.imageIndex;
    }

    /**
     * Overriding hashCode is necessary when overriding equals.
     * Base the hash code on the fields used in equals().
     */
    @Override
    public int hashCode() {
        // Dựa trên các trường dùng trong equals()
        return Objects.hash(xPoint, yPoint);
        // Nếu equals dùng cả imageIndex:
        // return Objects.hash(xPoint, yPoint, imageIndex);
    }

    /**
     * Provides a string representation of the Pikachu object, including its position and image index.
     */
    @Override
    public String toString() {
        // Bao gồm cả index và tọa độ để debug dễ hơn
        return "Pikachu[Index=" + imageIndex + ", Row=" + xPoint + ", Col=" + yPoint + "]";
    }
}