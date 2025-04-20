package com.pikapika.view;

import com.pikapika.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// Import Point để nhận tọa độ từ Controller
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer; // Đảm bảo import Timer

// Import các hằng số từ Utils
import static com.pikapika.utils.Utils.*; // Giả sử các hằng số BT_* và MAP_* nằm ở đây

/**
 * Created by anonymousjp on 5/20/17.
 * Updated with Suggestion (coordinate-based) and Auto Play button integration.
 */
public class PlayGameView extends JpanelBackground implements ActionListener {

    // UI Components
    private JPanel topMenuPanel;
    private JPanel pikachuPanel;
    private BorderLayout mainLayout;
    private GroupLayout topMenuLayout;
    private JButton btnReplay;
    private JProgressBar timerProgress;
    private JLabel timer;
    private JLabel score;
    private JButton btnPause;
    private JButton btnSuggest;
    private JButton btnAutoPlay;   // <<< THÊM LẠI NÚT AUTO PLAY >>>
    private JLabel mapCount;

    // Listener
    private PlayGameListener playGameListener;

    // Game Board
    private GridLayout pikachuLayout;
    private Pikachu[][] pikachuIcon; // Mảng chứa các nút Pikachu thực tế
    private int row;
    private int col;

    // Click State
    private int countClicked = 0;
    public Pikachu one; // Để Controller có thể reset nếu cần (hoặc dùng getter/setter)
    private Pikachu two;

    // Suggestion State
    private Pikachu suggestedP1 = null;
    private Pikachu suggestedP2 = null;
    private Timer suggestionTimer;
    private final int SUGGESTION_DURATION_MS = 1500;
    // <<< SỬA LẠI MÀU GỢI Ý THEO YÊU CẦU TRƯỚC >>>
    private final Color SUGGEST_BORDER_COLOR = Color.BLACK;


    // Constructors
    public PlayGameView() {
        this(Utils.MAP_ROW, Utils.MAP_COL);
    }

    public PlayGameView(int row, int col) {
        super();
        if (row < 3 || col < 3) {
            throw new IllegalArgumentException("Rows and columns must be at least 3 for border.");
        }
        this.row = row;
        this.col = col;
        initUI();
    }

    // UI Initialization
    private void initUI() {
        mainLayout = new BorderLayout();
        this.setLayout(mainLayout);

        topMenuPanel = new JPanel();
        topMenuLayout = new GroupLayout(topMenuPanel);
        topMenuPanel.setLayout(topMenuLayout);
        topMenuPanel.setOpaque(false);
        topMenuPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        // Initialize Top Menu Components
        btnReplay = new JButton();
        configureButton(btnReplay, "../resources/resum.png", "REPLAY");
        btnReplay.addActionListener(this);

        timerProgress = new JProgressBar(0, 100);
        timerProgress.setValue(100);
        timerProgress.setStringPainted(false);

        timer = createInfoLabel("Time: 100");
        score = createInfoLabel("Score: 0");
        mapCount = createInfoLabel("Map: 1");

        btnPause = new JButton();
        configureButton(btnPause, "../resources/pause.png", BT_PAUSE);
        btnPause.addActionListener(this);

        btnSuggest = new JButton("Gợi ý");
        btnSuggest.setFont(new Font("Arial", Font.BOLD, 12));
        btnSuggest.setMargin(new Insets(2, 5, 2, 5));
        btnSuggest.setActionCommand("SUGGEST");
        btnSuggest.setToolTipText("Hiển thị một cặp có thể ăn");
        btnSuggest.addActionListener(this);

        // <<< KHỞI TẠO VÀ CẤU HÌNH NÚT AUTO PLAY >>>
        btnAutoPlay = new JButton("Tự Chơi"); // Văn bản ban đầu
        btnAutoPlay.setFont(new Font("Arial", Font.BOLD, 12));
        btnAutoPlay.setMargin(new Insets(2, 5, 2, 5));
        btnAutoPlay.setActionCommand("AUTO_PLAY"); // Action Command mới
        btnAutoPlay.setToolTipText("Để máy tự động chơi màn này");
        btnAutoPlay.addActionListener(this); // Thêm listener

        // <<< CẬP NHẬT LAYOUT ĐỂ CHỨA btnAutoPlay >>>
        setupTopMenuLayout();

        // Pikachu Board Panel Setup
        pikachuLayout = new GridLayout(row - 2, col - 2, 2, 2);
        pikachuPanel = new JPanel(pikachuLayout);
        pikachuPanel.setOpaque(false);
        pikachuPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Add Panels to Main Layout
        add(topMenuPanel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(pikachuPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    // --- Tách hàm cài đặt GroupLayout (Đã cập nhật) ---
    private void setupTopMenuLayout() {
        // <<< SỬA ĐỔI LAYOUT ĐỂ CHỨA btnAutoPlay >>>
        topMenuLayout.setHorizontalGroup(
                topMenuLayout.createSequentialGroup()
                        .addComponent(btnReplay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(topMenuLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(topMenuLayout.createSequentialGroup()
                                        .addComponent(timer)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(mapCount)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(score)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnSuggest) // Nút Gợi ý
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED) // Khoảng cách
                                        .addComponent(btnAutoPlay) // <<< NÚT TỰ CHƠI >>>
                                )
                                .addComponent(timerProgress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPause, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        topMenuLayout.setVerticalGroup(
                topMenuLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(btnReplay, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(topMenuLayout.createSequentialGroup()
                                .addGroup(topMenuLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(timer)
                                        .addComponent(mapCount)
                                        .addComponent(score)
                                        .addComponent(btnSuggest) // Nút Gợi ý
                                        .addComponent(btnAutoPlay) // <<< NÚT TỰ CHƠI >>>
                                )
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timerProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addComponent(btnPause, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        Dimension buttonSize = new Dimension(45, 45);
        btnReplay.setPreferredSize(buttonSize);
        btnPause.setPreferredSize(buttonSize);
    }


    // --- Hàm trợ giúp cấu hình nút Icon ---
    private void configureButton(JButton button, String iconPath, String actionCommand) {
        // ... (Giữ nguyên) ...
        try {
            java.net.URL imgUrl = getClass().getResource(iconPath);
            if (imgUrl != null) {
                Image image = new ImageIcon(imgUrl).getImage();
                Icon icon = new ImageIcon(image.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                button.setIcon(icon);
            } else {
                System.err.println("Không tìm thấy resource icon: " + iconPath);
                button.setText(actionCommand.substring(0,1));
            }
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setBorder(null);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setActionCommand(actionCommand);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } catch (Exception e) {
            System.err.println("Lỗi cấu hình nút với icon: " + iconPath + " - " + e.getMessage());
            button.setText(actionCommand.substring(0,1));
        }
    }

    // --- Hàm trợ giúp tạo JLabel ---
    private JLabel createInfoLabel(String text) {
        // ... (Giữ nguyên) ...
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    // --- Xử lý sự kiện (Đã cập nhật) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (e.getSource() instanceof Pikachu) {
            if (playGameListener != null) {
                Pikachu clickedPikachu = (Pikachu) e.getSource();
                if (!clickedPikachu.isVisible()) return;

                ++countClicked;
                switch (countClicked) {
                    case 1:
                        one = clickedPikachu;
                        playGameListener.onPikachuClicked(countClicked, one);
                        break;
                    case 2:
                        if (one != null && !one.equals(clickedPikachu)) {
                            two = clickedPikachu;
                            playGameListener.onPikachuClicked(countClicked, one, two);
                        } else if (one != null && one.equals(clickedPikachu)) {
                            Utils.debug(getClass(), "Clicked same Pikachu again, deselecting.");
                            one.removeBorder(); one.repaint();
                            countClicked = 0; one = null;
                        } else { countClicked = 0; one = null; }
                        break;
                    default:
                        countClicked = 0;
                        if (one != null) { one.removeBorder(); one.repaint(); }
                        if (two != null) { two.removeBorder(); two.repaint(); }
                        one = null; two = null;
                        break;
                }
            }
        } else if (command != null && playGameListener != null) { // Xử lý các nút điều khiển
            switch (command) {
                case "REPLAY": playGameListener.onReplayClicked(); break;
                case BT_PAUSE: playGameListener.onPauseClicked(); break;
                case "SUGGEST": playGameListener.onSuggestClicked(); break;
                case "AUTO_PLAY": playGameListener.onAutoPlayClicked(); break; // <<< THÊM CASE AUTO_PLAY >>>
                default: Utils.debug(getClass(), "Unknown button command: " + command); break;
            }
        }
    }

    // --- Render Map (màn mới) ---
    public void renderMap(int[][] matrix) {
        // ... (Giữ nguyên) ...
        clearSuggestion();
        pikachuIcon = new Pikachu[row][col];
        pikachuPanel.removeAll();
        for (int i = 1; i < row - 1; i++) {
            for (int j = 1; j < col - 1; j++) {
                pikachuIcon[i][j] = createButton(i, j); // Sử dụng createButton đã sửa
                Icon icon = getIcon(matrix[i][j]);
                pikachuIcon[i][j].setIcon(icon);
                pikachuIcon[i][j].setVisible(matrix[i][j] != 0);
                // pikachuIcon[i][j].setBorder(null); // createButton đã set border mặc định
                pikachuPanel.add(pikachuIcon[i][j]);
            }
        }
        pikachuPanel.revalidate();
        pikachuPanel.repaint();
        countClicked = 0; one = null; two = null;
    }

    // --- Update Map (chơi lại / qua màn) ---
    public void updateMap(int[][] matrix) {
        // ... (Giữ nguyên, đảm bảo reset border đúng cách) ...
        clearSuggestion();
        countClicked = 0; one = null; two = null;
        for (int i = 1; i < row - 1; i++) {
            for (int j = 1; j < col - 1; j++) {
                if (pikachuIcon[i][j] != null) {
                    pikachuIcon[i][j].setIcon(getIcon(matrix[i][j]));
                    pikachuIcon[i][j].removeBorder(); // <<< SỬ DỤNG removeBorder ĐỂ RESET >>>
                    pikachuIcon[i][j].setVisible(matrix[i][j] != 0);
                    pikachuIcon[i][j].repaint();
                } else {
                    System.err.println("Warning: pikachuIcon[" + i + "][" + j + "] is null during updateMap.");
                }
            }
        }
        pikachuPanel.revalidate();
        pikachuPanel.repaint();
    }

    // --- Lấy Icon ---
    private Icon getIcon(int index) {
        // ... (Giữ nguyên implementation của getIcon) ...
        if (index > 0) {
            try {
                String imagePath = String.format("../resources/ic_%d.png", index);
                java.net.URL imgUrl = getClass().getResource(imagePath);
                if (imgUrl != null) {
                    int width = 40, height = 40;
                    Image image = new ImageIcon(imgUrl).getImage();
                    return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
                } else {
                    System.err.println("Không tìm thấy resource icon: " + imagePath);
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải icon index " + index + ": " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    // --- Tạo Pikachu Button (Đã cập nhật để dùng border mặc định từ Pikachu.java) ---
    private Pikachu createButton(int r, int c) {
        Pikachu btn = new Pikachu(r, c); // Constructor Pikachu đã set EmptyBorder
        // btn.setBorder(null); // <<< BỎ ĐI, ĐÃ SET TRONG CONSTRUCTOR PIKACHU >>>
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);
        return btn;
    }

    // --- Setter for Listener ---
    public void setPlayGameListener(PlayGameListener listener) {
        this.playGameListener = listener;
    }

    // --- Update UI Methods ---
    public void updateTimer(String timerText) { SwingUtilities.invokeLater(() -> this.timer.setText(timerText)); } // Thêm invokeLater
    public void updateScore(String scoreText) { SwingUtilities.invokeLater(() -> this.score.setText(scoreText)); } // Thêm invokeLater
    public void updateMapNum(String mapText) { SwingUtilities.invokeLater(() -> this.mapCount.setText(mapText)); } // Thêm invokeLater
    public void setCountClicked(int value) { this.countClicked = value; }
    public void updateProgress(int progressValue) { SwingUtilities.invokeLater(() -> { int min = timerProgress.getMinimum(); int max = timerProgress.getMaximum(); timerProgress.setValue(Math.max(min, Math.min(max, progressValue))); }); } // Thêm invokeLater
    public void updateMaxProgress(int maxProgress) { SwingUtilities.invokeLater(() -> { timerProgress.setMaximum(maxProgress); timerProgress.setValue(maxProgress); }); } // Thêm invokeLater
    public int getMaxCountDown() { return timerProgress.getMaximum(); }

    // <<< THÊM LẠI: Phương thức lấy Pikachu tại tọa độ >>>
    public Pikachu getPikachuAt(int r, int c) {
        if (r >= 0 && r < row && c >= 0 && c < col && pikachuIcon != null && pikachuIcon[r] != null) {
            // Đảm bảo truy cập trong giới hạn mảng thực tế
            if(r < pikachuIcon.length && c < pikachuIcon[r].length){
                return pikachuIcon[r][c];
            }
        }
        // Log lỗi nếu tọa độ không hợp lệ
        // System.err.println("getPikachuAt: Invalid coordinates or null array: r=" + r + ", c=" + c);
        return null;
    }

    // <<< THÊM LẠI: Phương thức cập nhật trạng thái nút Auto Play >>>
    public void updateAutoPlayButtonState(boolean isPlaying) {
        SwingUtilities.invokeLater(() -> { // Đảm bảo cập nhật UI trên EDT
            if (isPlaying) {
                btnAutoPlay.setText("Dừng Auto");
                btnAutoPlay.setToolTipText("Nhấn để dừng chế độ tự động chơi");
                // Có thể vô hiệu hóa các nút khác ở đây (tùy chọn)
                // btnPause.setEnabled(false); btnReplay.setEnabled(false); btnSuggest.setEnabled(false);
            } else {
                btnAutoPlay.setText("Tự Chơi");
                btnAutoPlay.setToolTipText("Để máy tự động chơi màn này");
                // Kích hoạt lại các nút khác (nếu đã vô hiệu hóa)
                // btnPause.setEnabled(true); btnReplay.setEnabled(true); btnSuggest.setEnabled(true);
            }
            btnAutoPlay.setEnabled(true); // Luôn bật nút này để có thể dừng
        });
    }



    // --- Suggestion Management Methods (Giữ nguyên các phương thức đã sửa) ---
    public void displaySuggestionAt(Point p1Coord, Point p2Coord) {
        // ... (Giữ nguyên implementation) ...
        if (p1Coord == null || p2Coord == null) { /*...*/ return; }
        Utils.debug(getClass(), "Attempting to find Pikachu at coordinates: [" + p1Coord.x + "," + p1Coord.y + "] and [" + p2Coord.x + "," + p2Coord.y + "]");
        try {
            Pikachu actualP1 = getPikachuAt(p1Coord.x, p1Coord.y); // <<< SỬ DỤNG GETTER >>>
            Pikachu actualP2 = getPikachuAt(p2Coord.x, p2Coord.y); // <<< SỬ DỤNG GETTER >>>
            if (actualP1 != null && actualP1.isVisible() && actualP2 != null && actualP2.isVisible()) {
                Utils.debug(getClass(), "Found actual Pikachu objects: " + actualP1.getIndex() + " and " + actualP2.getIndex());
                showSuggestionInternal(actualP1, actualP2);
            } else { /*...*/ }
        } catch (ArrayIndexOutOfBoundsException e) { /*...*/ }
        catch (Exception e) { /*...*/ }
    }

    private void showSuggestionInternal(Pikachu p1, Pikachu p2) {
        // ... (Giữ nguyên implementation - đảm bảo dùng repaint) ...
        clearSuggestion();
        this.suggestedP1 = p1; this.suggestedP2 = p2;
        if (this.suggestedP1 != null && this.suggestedP2 != null) {
            Utils.debug(getClass(), "Internal: Showing suggestion for " + suggestedP1.getIndex() + " and " + suggestedP2.getIndex());
            this.suggestedP1.drawBorder(SUGGEST_BORDER_COLOR); // Màu đỏ
            this.suggestedP2.drawBorder(SUGGEST_BORDER_COLOR);
            this.suggestedP1.repaint();
            this.suggestedP2.repaint();
            startSuggestionTimer();
        } else { /*...*/ }
    }

    public void clearSuggestion() {
        // ... (Giữ nguyên implementation - đảm bảo dùng repaint) ...
        stopSuggestionTimer();
        if (suggestedP1 != null) {
            suggestedP1.removeBorder(); suggestedP1.repaint(); suggestedP1 = null;
        }
        if (suggestedP2 != null) {
            suggestedP2.removeBorder(); suggestedP2.repaint(); suggestedP2 = null;
        }
    }

    private void startSuggestionTimer() {
        // ... (Giữ nguyên implementation - đảm bảo dùng invokeLater) ...
        stopSuggestionTimer();
        suggestionTimer = new Timer(SUGGESTION_DURATION_MS, e -> SwingUtilities.invokeLater(this::clearSuggestion) );
        suggestionTimer.setRepeats(false);
        suggestionTimer.start();
    }

    private void stopSuggestionTimer() {
        // ... (Giữ nguyên implementation) ...
        if (suggestionTimer != null && suggestionTimer.isRunning()) { suggestionTimer.stop(); }
        suggestionTimer = null;
    }

    // --- Listener Interface (Đã cập nhật) ---
    public interface PlayGameListener {
        void onReplayClicked();
        void onPauseClicked();
        void onSuggestClicked();
        void onAutoPlayClicked(); // <<< THÊM LẠI PHƯƠNG THỨC NÀY >>>
        void onPikachuClicked(int clickCounter, Pikachu... pikachus);
    }

    // --- Override setBackgroundImage ---
    @Override
    public void setBackgroundImage(String imagePath) {
        super.setBackgroundImage(imagePath);
        // this.repaint();
    }

} // Kết thúc lớp PlayGameView
