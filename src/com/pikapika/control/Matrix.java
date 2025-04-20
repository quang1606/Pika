package com.pikapika.control;

import com.pikapika.utils.Utils;
import com.pikapika.view.Pikachu;
// Không cần import Pikachu ở đây nữa nếu chỉ xử lý ma trận và tọa độ
// import com.pikapika.view.Pikachu;
import java.awt.Point; // Import Point để làm việc với tọa độ
import java.util.Random;
// Bỏ import Scanner vì không dùng
// import java.util.Scanner;

import static com.pikapika.utils.Utils.PIKACHU_NUMBER;

/**
 *
 * @author Ronaldo Hanh
 * @author Refactored for Coordinate-based logic
 */
public class Matrix {
    private int[][] matrix;
    private static final int CONST_VALUE = 0; // Giá trị ô trống
    private int row;
    private int col;
    // private int value; // Biến này dường như không được sử dụng, có thể bỏ

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    // public int getValue() { return value; } // Không dùng
    // public void setValue(int value) { this.value = value; } // Không dùng

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    // Phương thức này vẫn hữu ích nếu Controller cần cập nhật ma trận dựa trên đối tượng Pikachu từ View
    // public void setXY(Pikachu pikachu, int value) {
    //     if (isValidCoord(pikachu.getXPoint(), pikachu.getYPoint())) {
    //          this.matrix[pikachu.getXPoint()][pikachu.getYPoint()] = value;
    //     }
    // }

    // Phương thức này dùng tọa độ, rất tốt
    public void setXY(int r, int c, int value) {
        if (isValidCoord(r, c)) {
            this.matrix[r][c] = value;
        }
    }

    // Phương thức này vẫn hữu ích
    // public int getXY(Pikachu pikachu) {
    //     if (isValidCoord(pikachu.getXPoint(), pikachu.getYPoint())) {
    //          return matrix[pikachu.getXPoint()][pikachu.getYPoint()];
    //     }
    //     return -1; // Hoặc ném Exception nếu tọa độ không hợp lệ
    // }

    public int getXY(int r, int c) {
        if (isValidCoord(r, c)) {
            return matrix[r][c];
        }
        return -1; // Hoặc ném Exception
    }

    // Hàm trợ giúp kiểm tra tọa độ hợp lệ
    private boolean isValidCoord(int r, int c) {
        return r >= 0 && r < row && c >= 0 && c < col;
    }


    public Matrix(int row, int col) {
        this.setCol(col);
        this.setRow(row);
        renderMatrix();
    }

    /*Tao Random Matrix*/
    public int[][] renderMatrix() {
        this.matrix = new int[row][col];
        // Khởi tạo tất cả là ô trống (đã bao gồm viền)
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                matrix[i][j] = CONST_VALUE;
            }
        }

        // Tính số cặp cần tạo
        int totalCells = (row - 2) * (col - 2);
        if (totalCells % 2 != 0) {
            totalCells--; // Đảm bảo số ô là chẵn nếu cần
        }
        int numPairs = totalCells / 2;

        Random random = new Random();
        // Tạo các cặp giá trị ngẫu nhiên
        for (int pairIndex = 0; pairIndex < numPairs; pairIndex++) {
            int value = random.nextInt(PIKACHU_NUMBER) + 1;
            // Tìm 2 vị trí trống để đặt cặp giá trị
            placeValueInEmptyCell(value);
            placeValueInEmptyCell(value);
        }
        Utils.debug(getClass(), "Matrix rendered with " + numPairs + " pairs.");
        return this.matrix;
    }

    // Hàm trợ giúp đặt giá trị vào ô trống ngẫu nhiên bên trong viền
    private void placeValueInEmptyCell(int value) {
        Random random = new Random();
        int r, c;
        do {
            r = random.nextInt(row - 2) + 1; // Từ 1 đến row-2
            c = random.nextInt(col - 2) + 1; // Từ 1 đến col-2
        } while (matrix[r][c] != CONST_VALUE); // Lặp lại nếu ô đã có giá trị
        matrix[r][c] = value;
    }


    /* ----- PHẦN LOGIC KIỂM TRA ĐƯỜNG ĐI ĐÃ SỬA ĐỔI ----- */

    /* TH1: Kiểm tra đường thẳng */
    /** Kiểm tra đường thẳng giữa 2 cột c1, c2 trên cùng hàng r */
    private boolean checkLineX(int c1, int c2, int r) {
        if (!isValidCoord(r, c1) || !isValidCoord(r, c2)) return false; // Kiểm tra tọa độ hợp lệ
        int minCol = Math.min(c1, c2);
        int maxCol = Math.max(c1, c2);
        for (int c = minCol + 1; c < maxCol; c++) {
            if (matrix[r][c] != CONST_VALUE) {
                return false; // Có vật cản
            }
        }
        return true; // Không có vật cản
    }

    /** Kiểm tra đường thẳng giữa 2 hàng r1, r2 trên cùng cột c */
    private boolean checkLineY(int r1, int r2, int c) {
        if (!isValidCoord(r1, c) || !isValidCoord(r2, c)) return false; // Kiểm tra tọa độ hợp lệ
        int minRow = Math.min(r1, r2);
        int maxRow = Math.max(r1, r2);
        for (int r = minRow + 1; r < maxRow; r++) {
            if (matrix[r][c] != CONST_VALUE) {
                return false; // Có vật cản
            }
        }
        return true; // Không có vật cản
    }

    /* TH2: Kiểm tra đường chữ nhật (L-shape) */
    /** Kiểm tra đường chữ L qua góc (r1, c2) hoặc (r2, c1) */
    private boolean checkRect(int r1, int c1, int r2, int c2) {
        if (!isValidCoord(r1, c1) || !isValidCoord(r2, c2)) return false;

        // Kiểm tra góc (r1, c2)
        if (matrix[r1][c2] == CONST_VALUE) { // Nếu góc trống
            if (checkLineX(c1, c2, r1) && checkLineY(r1, r2, c2)) {
                return true;
            }
        }
        // Kiểm tra góc (r2, c1)
        if (matrix[r2][c1] == CONST_VALUE) { // Nếu góc trống
            if (checkLineY(r1, r2, c1) && checkLineX(c1, c2, r2)) {
                return true;
            }
        }
        return false;
    }


    /* TH3: Kiểm tra mở rộng (U-shape) */
    /** Kiểm tra mở rộng theo chiều ngang (sang trái hoặc phải) */
    private boolean checkMoreLineX(int r1, int c1, int r2, int c2, int direction) {
        if (!isValidCoord(r1, c1) || !isValidCoord(r2, c2)) return false;

        // Tìm cột bắt đầu và kết thúc để quét ngang
        int startCol = (direction == -1) ? Math.min(c1, c2) - 1 : Math.max(c1, c2) + 1;
        int currentCol = startCol;

        // Quét ngang ra ngoài theo hướng `direction`
        while (isValidCoord(r1, currentCol) && matrix[r1][currentCol] == CONST_VALUE) {
            // Nếu tìm thấy đường dọc nối tới hàng của điểm kia tại cột này
            if (isValidCoord(r2, currentCol) && matrix[r2][currentCol] == CONST_VALUE && checkLineY(r1, r2, currentCol)) {
                // Kiểm tra tiếp đường ngang từ (r2, currentCol) đến (r2, c2)
                if (checkLineX(currentCol, c2, r2)) {
                    return true;
                }
            }
            // Di chuyển đến cột tiếp theo
            if (direction == 0) break; // Tránh vòng lặp vô hạn nếu direction = 0 (lỗi)
            currentCol += direction;
            // Dừng nếu đi ra ngoài biên trái/phải cùng
            if (currentCol < 0 || currentCol >= col) break;
        }
        return false;
    }

    /** Kiểm tra mở rộng theo chiều dọc (lên trên hoặc xuống dưới) */
    private boolean checkMoreLineY(int r1, int c1, int r2, int c2, int direction) {
        if (!isValidCoord(r1, c1) || !isValidCoord(r2, c2)) return false;

        // Tìm hàng bắt đầu và kết thúc để quét dọc
        int startRow = (direction == -1) ? Math.min(r1, r2) - 1 : Math.max(r1, r2) + 1;
        int currentRow = startRow;

        // Quét dọc ra ngoài theo hướng `direction`
        while (isValidCoord(currentRow, c1) && matrix[currentRow][c1] == CONST_VALUE) {
            // Nếu tìm thấy đường ngang nối tới cột của điểm kia tại hàng này
            if (isValidCoord(currentRow, c2) && matrix[currentRow][c2] == CONST_VALUE && checkLineX(c1, c2, currentRow)) {
                // Kiểm tra tiếp đường dọc từ (currentRow, c2) đến (r2, c2)
                if (checkLineY(currentRow, r2, c2)) {
                    return true;
                }
            }
            // Di chuyển đến hàng tiếp theo
            if (direction == 0) break; // Tránh vòng lặp vô hạn nếu direction = 0 (lỗi)
            currentRow += direction;
            // Dừng nếu đi ra ngoài biên trên/dưới cùng
            if (currentRow < 0 || currentRow >= row) break;
        }
        return false;
    }

    /* ---- Algorithm chính sử dụng tọa độ ---- */
    /**
     * Kiểm tra xem có đường đi hợp lệ giữa hai điểm (r1, c1) và (r2, c2) không.
     * Cả hai điểm phải có cùng giá trị và khác ô trống.
     * @param r1 Hàng của điểm 1
     * @param c1 Cột của điểm 1
     * @param r2 Hàng của điểm 2
     * @param c2 Cột của điểm 2
     * @return true nếu có đường đi, false nếu không.
     */
    public boolean algorithm(int r1, int c1, int r2, int c2) {
        // Các kiểm tra ban đầu như cũ...

        // Biến để đếm số lần gấp góc
        int turns = 0;

        // 1. Đường thẳng (I shape)
        if (r1 == r2) { // Cùng hàng
            if (checkLineX(c1, c2, r1)) {
                turns = 1; // Đường thẳng
                return checkTurns(turns);
            }
        }
        if (c1 == c2) { // Cùng cột
            if (checkLineY(r1, r2, c1)) {
                turns = 1; // Đường thẳng
                return checkTurns(turns);
            }
        }

        // 2. Đường chữ L (L shape)
        if (checkRect(r1, c1, r2, c2)) {
            turns = 2; // Đường gấp 1 góc
            return checkTurns(turns);
        }

        // 3. Đường mở rộng (U shape)
        // Các hướng mở rộng
        // 3. Đường mở rộng (U shape)
        if (checkMoreLine(r1, c1, r2, c2)) {
            return true;
        }

        return false;
    }

    // Phương thức kiểm tra số lần gấp góc
    private boolean checkTurns(int turns) {
        return turns <= 3; // Không quá 3 đoạn thẳng
    }

    private boolean checkMoreLine(int r1, int c1, int r2, int c2) {
        int maxRow = matrix.length;
        int maxCol = matrix[0].length;
        boolean[][] visited = new boolean[maxRow][maxCol];

        return dfs(r1, c1, r2, c2, -1, 0, visited);
    }

    private boolean dfs(int row, int col, int targetRow, int targetCol, int prevDir, int turns, boolean[][] visited) {
        // Kiểm tra vượt quá số lần gấp khúc cho phép
        if (turns > 2) return false;

        // Nếu đến được điểm đích
        if (row == targetRow && col == targetCol) return true;

        visited[row][col] = true;

        // Các hướng di chuyển: lên, phải, xuống, trái
        int[] dRow = {-1, 0, 1, 0};
        int[] dCol = {0, 1, 0, -1};

        for (int dir = 0; dir < 4; dir++) {
            int newRow = row + dRow[dir];
            int newCol = col + dCol[dir];

            // Kiểm tra giới hạn bảng và ô chưa được thăm
            if (newRow >= 0 && newRow < matrix.length && newCol >= 0 && newCol < matrix[0].length
                    && !visited[newRow][newCol] && (matrix[newRow][newCol] == CONST_VALUE || (newRow == targetRow && newCol == targetCol))) {
                int newTurns = (prevDir == -1 || prevDir == dir) ? turns : turns + 1;
                if (dfs(newRow, newCol, targetRow, targetCol, dir, newTurns, visited)) {
                    return true;
                }
            }
        }

        visited[row][col] = false; // Backtrack
        return false;
    }



    /**
     * Tìm và trả về TỌA ĐỘ của một cặp Pikachu có thể nối được.
     * @return Một mảng chứa 2 đối tượng Point đại diện cho tọa độ [hàng, cột]
     *         của cặp gợi ý, hoặc null nếu không tìm thấy.
     *         Point.x sẽ là hàng, Point.y sẽ là cột.
     */
    public Point[] suggestPairCoords() {
        for (int r1 = 0; r1 < row; r1++) {
            for (int c1 = 0; c1 < col; c1++) {
                int val1 = matrix[r1][c1];
                if (val1 == CONST_VALUE) continue; // Bỏ qua ô trống

                // Bắt đầu vòng lặp thứ hai từ vị trí tiếp theo để tránh trùng lặp và tự so sánh
                for (int r2 = r1; r2 < row; r2++) {
                    // Nếu cùng hàng, bắt đầu cột thứ hai từ cột tiếp theo của điểm 1
                    int startC2 = (r1 == r2) ? c1 + 1 : 0;
                    for (int c2 = startC2; c2 < col; c2++) {
                        int val2 = matrix[r2][c2];
                        if (val1 == val2) { // Chỉ kiểm tra nếu cùng giá trị
                            // Gọi hàm algorithm đã sửa đổi với tọa độ
                            if (algorithm(r1, c1, r2, c2)) {
                                // Trả về tọa độ bằng Point (x=hàng, y=cột)
                                return new Point[]{new Point(r1, c1), new Point(r2, c2)};
                            }
                        }
                    }
                }
            }
        }
        return null; // không có cặp nào nối được
    }

    /**
     * Kiểm tra xem trên bảng còn nước đi hợp lệ nào không.
     * @return true nếu còn nước đi, false nếu hết.
     */
    public boolean canPlay() {
        // Gọi suggestPairCoords, nếu nó trả về khác null tức là còn nước đi
        return suggestPairCoords() != null;
    }



    /* Các phương thức cũ không cần thiết nữa vì đã có renderMatrix tốt hơn */
    // private int demPT(int value) { ... } // Không cần nữa
    // private void change(int value) { ... } // Không cần nữa

    /* Các phương thức checkRec cũ có thể bị loại bỏ hoặc giữ lại nếu bạn muốn so sánh */
    // private boolean checkRecX(Pikachu p1, Pikachu p2) { ... }
    // private boolean checkRecY(Pikachu p1, Pikachu p2) { ... }

}