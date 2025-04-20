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
    private JButton btnAutoPlay;
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
    public Pikachu one;
    private Pikachu two;

    // Suggestion State
    private Pikachu suggestedP1 = null;
    private Pikachu suggestedP2 = null;
    private Timer suggestionTimer;
    private final int SUGGESTION_DURATION_MS = 1500;
    private final Color SUGGEST_BORDER_COLOR = Color.BLUE;


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

        btnAutoPlay = new JButton("Tự Chơi");
        btnAutoPlay.setFont(new Font("Arial", Font.BOLD, 12));
        btnAutoPlay.setMargin(new Insets(2, 5, 2, 5));
        btnAutoPlay.setActionCommand("AUTO_PLAY");
        btnAutoPlay.setToolTipText("Để máy tự động chơi màn này");
        btnAutoPlay.addActionListener(this);

        setupTopMenuLayout();

        // Pikachu Board Panel Setup
        // Kích thước GridLayout là số ô thực tế, không tính viền
        pikachuLayout = new GridLayout(row - 2, col - 2, 2, 2);
        pikachuPanel = new JPanel(pikachuLayout);
        pikachuPanel.setOpaque(false);
        pikachuPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Add Panels to Main Layout
        add(topMenuPanel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel(new GridBagLayout()); // Dùng GridBagLayout để căn giữa pikachuPanel
        centerPanel.setOpaque(false);
        centerPanel.add(pikachuPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    // --- Tách hàm cài đặt GroupLayout ---
    private void setupTopMenuLayout() {
        // Giữ nguyên phần layout của bạn
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
        // Giữ nguyên phần cấu hình nút của bạn
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
        // Giữ nguyên phần tạo label của bạn
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    // --- Xử lý sự kiện ---
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (e.getSource() instanceof Pikachu) {
            if (playGameListener != null) {
                Pikachu clickedPikachu = (Pikachu) e.getSource();
                // Bỏ qua click nếu nút không hiển thị (đã bị ăn)
                if (!clickedPikachu.isVisible()) {
                    Utils.debug(getClass(), "Clicked on an invisible Pikachu. Ignoring.");
                    // Reset nếu đang click dở dang
                    if(countClicked == 1 && one != null){
                        one.removeBorder();
                        one.repaint();
                    }
                    countClicked = 0;
                    one = null;
                    two = null;
                    return;
                }


                ++countClicked;
                switch (countClicked) {
                    case 1:
                        // Lưu ô đầu tiên và báo cho Controller
                        one = clickedPikachu;
                        playGameListener.onPikachuClicked(countClicked, one);
                        break;
                    case 2:
                        // Kiểm tra xem có phải click lại ô cũ không
                        if (one != null && one.equals(clickedPikachu)) {
                            Utils.debug(getClass(), "Clicked same Pikachu again, deselecting.");
                            // Bỏ chọn ô đầu tiên
                            one.removeBorder();
                            one.repaint();
                            countClicked = 0; // Reset về trạng thái chưa chọn gì
                            one = null;
                        } else if (one != null) {
                            // Click vào ô thứ hai khác ô đầu tiên
                            two = clickedPikachu;
                            // Báo cho Controller xử lý cặp one và two
                            playGameListener.onPikachuClicked(countClicked, one, two);
                            // Controller sẽ quyết định reset countClicked hay không
                        } else {
                            // Trường hợp lạ: countClicked=2 nhưng 'one' là null? Reset đề phòng.
                            Utils.debug(getClass(), "Warning: countClicked=2 but 'one' is null. Resetting.");
                            countClicked = 0;
                            one = null;
                        }
                        break;
                    default: // Click quá 2 lần hoặc trạng thái không hợp lệ -> Reset
                        Utils.debug(getClass(), "Invalid click state (count > 2 or unexpected). Resetting.");
                        if (one != null) { one.removeBorder(); one.repaint(); }
                        if (two != null) { two.removeBorder(); two.repaint(); } // Đảm bảo two cũng được reset nếu có
                        countClicked = 0;
                        one = null;
                        two = null;
                        break;
                }
            }
        } else if (command != null && playGameListener != null) { // Xử lý các nút điều khiển (Replay, Pause, Suggest, AutoPlay)
            switch (command) {
                case "REPLAY": playGameListener.onReplayClicked(); break;
                case BT_PAUSE: playGameListener.onPauseClicked(); break;
                case "SUGGEST": playGameListener.onSuggestClicked(); break;
                case "AUTO_PLAY": playGameListener.onAutoPlayClicked(); break;
                default: Utils.debug(getClass(), "Unknown button command: " + command); break;
            }
        }
    }

    // --- Render Map (màn mới) ---
    public void renderMap(int[][] matrix) {
        clearSuggestion(); // Xóa gợi ý cũ
        pikachuIcon = new Pikachu[row][col]; // Khởi tạo mảng chứa Pikachu
        pikachuPanel.removeAll(); // Xóa hết các nút cũ trên panel

        // Lặp qua các ô bên trong (không tính viền ngoài của ma trận logic)
        for (int i = 1; i < row - 1; i++) {
            for (int j = 1; j < col - 1; j++) {
                int imageIndex = matrix[i][j]; // Lấy chỉ số hình ảnh từ ma trận logic
                // Tạo nút Pikachu mới với tọa độ (i, j) và chỉ số hình ảnh
                pikachuIcon[i][j] = createButton(i, j, imageIndex); // <<< SỬA Ở ĐÂY
                Icon icon = getIcon(imageIndex); // Lấy đối tượng Icon
                pikachuIcon[i][j].setIcon(icon); // Đặt icon cho nút
                pikachuIcon[i][j].setVisible(imageIndex != 0); // Chỉ hiển thị nếu chỉ số ảnh > 0
                pikachuPanel.add(pikachuIcon[i][j]); // Thêm nút vào panel
            }
        }

        pikachuPanel.revalidate(); // Yêu cầu layout lại panel
        pikachuPanel.repaint();    // Vẽ lại panel
        // Reset trạng thái click
        countClicked = 0;
        one = null;
        two = null;
    }

    // --- Update Map (chơi lại / qua màn / xóa cặp) ---
    public void updateMap(int[][] matrix) {
        clearSuggestion(); // Xóa gợi ý cũ
        // Không reset one, two ở đây, Controller sẽ quyết định khi nào reset
        // countClicked = 0; one = null; two = null; // <<< TẠM BỎ RESET Ở ĐÂY

        for (int i = 1; i < row - 1; i++) {
            for (int j = 1; j < col - 1; j++) {
                if (pikachuIcon[i][j] != null) {
                    int newImageIndex = matrix[i][j]; // Lấy chỉ số ảnh mới từ ma trận
                    Icon newIcon = getIcon(newImageIndex); // Lấy Icon mới

                    // Chỉ cập nhật nếu trạng thái thay đổi (icon hoặc visibility)
                    // Điều này có thể tối ưu hiệu suất một chút
                    boolean needsUpdate = false;
                    if (pikachuIcon[i][j].getIndex() != newImageIndex) { // Kiểm tra index logic
                        pikachuIcon[i][j].setImageIndex(newImageIndex); // <<< SỬA Ở ĐÂY: Cập nhật index logic
                        needsUpdate = true;
                    }
                    if (pikachuIcon[i][j].getIcon() != newIcon) { // So sánh đối tượng Icon (có thể không chính xác nếu tạo mới liên tục)
                        pikachuIcon[i][j].setIcon(newIcon); // <<< Cập nhật hình ảnh hiển thị
                        needsUpdate = true;
                    }
                    if (pikachuIcon[i][j].isVisible() != (newImageIndex != 0)) {
                        pikachuIcon[i][j].setVisible(newImageIndex != 0); // <<< Cập nhật trạng thái hiển thị
                        needsUpdate = true;
                    }

                    // Nếu có sự thay đổi, đảm bảo không còn viền và repaint
                    if (needsUpdate) {
                        pikachuIcon[i][j].removeBorder(); // Xóa viền (quan trọng khi load lại map)
                        pikachuIcon[i][j].repaint();      // Yêu cầu vẽ lại nút này
                    }
                } else {
                    // Lỗi này không nên xảy ra nếu renderMap chạy đúng
                    System.err.println("Warning: pikachuIcon[" + i + "][" + j + "] is null during updateMap.");
                }
            }
        }
        // Không cần revalidate/repaint toàn bộ panel nếu chỉ ẩn/hiện nút
        // pikachuPanel.revalidate();
        // pikachuPanel.repaint();
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

    // --- Tạo Pikachu Button ---
    // <<< SỬA: Thêm tham số imageIndex >>>
    private Pikachu createButton(int r, int c, int imageIndex) {
        // <<< SỬA: Truyền imageIndex vào constructor của Pikachu >>>
        Pikachu btn = new Pikachu(r, c, imageIndex);
        // Các cài đặt khác đã chuyển vào constructor Pikachu (setBorder, setContentAreaFilled,...)
        // btn.setContentAreaFilled(false); // Đã làm trong constructor Pikachu
        // btn.setFocusPainted(false);     // Đã làm trong constructor Pikachu
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this); // Gán listener cho nút
        return btn;
    }

    // --- Setter for Listener ---
    public void setPlayGameListener(PlayGameListener listener) {
        this.playGameListener = listener;
    }

    // --- Update UI Methods ---
    // Giữ nguyên các hàm update UI, đảm bảo dùng invokeLater
    public void updateTimer(String timerText) { SwingUtilities.invokeLater(() -> this.timer.setText(timerText)); }
    public void updateScore(String scoreText) { SwingUtilities.invokeLater(() -> this.score.setText(scoreText)); }
    public void updateMapNum(String mapText) { SwingUtilities.invokeLater(() -> this.mapCount.setText(mapText)); }
    public void setCountClicked(int value) { this.countClicked = value; } // Cần được gọi từ Controller khi cần reset
    public void updateProgress(int progressValue) { SwingUtilities.invokeLater(() -> { int min = timerProgress.getMinimum(); int max = timerProgress.getMaximum(); timerProgress.setValue(Math.max(min, Math.min(max, progressValue))); }); }
    public void updateMaxProgress(int maxProgress) { SwingUtilities.invokeLater(() -> { timerProgress.setMaximum(maxProgress); timerProgress.setValue(maxProgress); }); }
    public int getMaxCountDown() { return timerProgress.getMaximum(); }

    // --- Lấy Pikachu tại tọa độ ---
    public Pikachu getPikachuAt(int r, int c) {
        // Kiểm tra biên của mảng pikachuIcon
        if (r >= 0 && r < row && c >= 0 && c < col && pikachuIcon != null && pikachuIcon[r] != null && c < pikachuIcon[r].length) {
            return pikachuIcon[r][c];
        }
        // Tọa độ không hợp lệ hoặc mảng chưa khởi tạo/sai kích thước
        // Utils.debug(getClass(), "getPikachuAt: Invalid coordinates or null array element: r=" + r + ", c=" + c);
        return null;
    }

    // --- Cập nhật trạng thái nút Auto Play ---
    public void updateAutoPlayButtonState(boolean isPlaying) {
        SwingUtilities.invokeLater(() -> {
            if (isPlaying) {
                btnAutoPlay.setText("Dừng Auto");
                btnAutoPlay.setToolTipText("Nhấn để dừng chế độ tự động chơi");
                // Tùy chọn: Vô hiệu hóa các nút khác khi đang auto play
                // btnPause.setEnabled(false);
                // btnReplay.setEnabled(false);
                // btnSuggest.setEnabled(false);
            } else {
                btnAutoPlay.setText("Tự Chơi");
                btnAutoPlay.setToolTipText("Để máy tự động chơi màn này");
                // Tùy chọn: Kích hoạt lại các nút khác
                // btnPause.setEnabled(true);
                // btnReplay.setEnabled(true);
                // btnSuggest.setEnabled(true);
            }
            // Luôn cho phép bấm nút AutoPlay để có thể dừng
            btnAutoPlay.setEnabled(true);
        });
    }



    // --- Suggestion Management Methods ---
    // Cần đảm bảo getIndex() trả về int
    public void displaySuggestionAt(Point p1Coord, Point p2Coord) {
        if (p1Coord == null || p2Coord == null) {
            Utils.debug(getClass(), "displaySuggestionAt: Received null coordinates.");
            return;
        }
        Utils.debug(getClass(), "Attempting to find Pikachu at coordinates for suggestion: P1" + p1Coord + ", P2" + p2Coord);

        Pikachu actualP1 = getPikachuAt(p1Coord.x, p1Coord.y);
        Pikachu actualP2 = getPikachuAt(p2Coord.x, p2Coord.y);

        if (actualP1 != null && actualP1.isVisible() && actualP2 != null && actualP2.isVisible()) {
            // Thêm kiểm tra index ở đây để chắc chắn Controller gửi cặp đúng
            if (actualP1.getIndex() == actualP2.getIndex()) {
                Utils.debug(getClass(), "Found actual Pikachu objects for suggestion: P1 Index=" + actualP1.getIndex() + ", P2 Index=" + actualP2.getIndex());
                showSuggestionInternal(actualP1, actualP2);
            } else {
                Utils.debug(getClass(), "Error in suggestion logic: Controller provided mismatching pair. P1 Index=" + actualP1.getIndex() + ", P2 Index=" + actualP2.getIndex());
            }
        } else {
            Utils.debug(getClass(), "Could not find visible Pikachu objects at suggested coordinates.");
            if(actualP1 == null) Utils.debug(getClass(), "Reason: actualP1 is null");
            else if(!actualP1.isVisible()) Utils.debug(getClass(), "Reason: actualP1 is not visible");
            if(actualP2 == null) Utils.debug(getClass(), "Reason: actualP2 is null");
            else if(!actualP2.isVisible()) Utils.debug(getClass(), "Reason: actualP2 is not visible");
        }
    }

    private void showSuggestionInternal(Pikachu p1, Pikachu p2) {
//
        this.suggestedP1 = p1;
        this.suggestedP2 = p2;
        if (this.suggestedP1 != null && this.suggestedP2 != null) {
            Utils.debug(getClass(), "Internal: Showing suggestion border for P1 Index=" + suggestedP1.getIndex() + " and P2 Index=" + suggestedP2.getIndex());
            // Sử dụng invokeLater để đảm bảo cập nhật UI trên EDT
            SwingUtilities.invokeLater(() -> {
                if (suggestedP1 != null) { // Kiểm tra lại phòng trường hợp bị clear giữa chừng
                    suggestedP1.drawBorder(SUGGEST_BORDER_COLOR);
                    suggestedP1.repaint();
                }
                if (suggestedP2 != null) {
                    suggestedP2.drawBorder(SUGGEST_BORDER_COLOR);
                    suggestedP2.repaint();
                }
            });
            startSuggestionTimer(); // Bắt đầu timer tự xóa gợi ý
        } else {
            Utils.debug(getClass(), "Internal: Cannot show suggestion, one or both Pikachu objects became null.");
        }
    }

    public void clearSuggestion() {
        stopSuggestionTimer(); // Dừng timer (nếu đang chạy)
        // Sử dụng invokeLater để đảm bảo cập nhật UI trên EDT
        SwingUtilities.invokeLater(() -> {
            if (suggestedP1 != null) {
                suggestedP1.removeBorder();
                suggestedP1.repaint();
                suggestedP1 = null; // Đặt lại thành null
            }
            if (suggestedP2 != null) {
                suggestedP2.removeBorder();
                suggestedP2.repaint();
                suggestedP2 = null; // Đặt lại thành null
            }
        });
    }

    private void startSuggestionTimer() {
        stopSuggestionTimer(); // Dừng timer cũ trước khi tạo timer mới
        suggestionTimer = new Timer(SUGGESTION_DURATION_MS, e -> SwingUtilities.invokeLater(this::clearSuggestion) );
        suggestionTimer.setRepeats(false); // Chỉ chạy một lần
        suggestionTimer.start();
    }

    private void stopSuggestionTimer() {
        if (suggestionTimer != null && suggestionTimer.isRunning()) {
            suggestionTimer.stop();
        }
        suggestionTimer = null; // Giải phóng timer
    }

    // --- Listener Interface ---
    public interface PlayGameListener {
        void onReplayClicked();
        void onPauseClicked();
        void onSuggestClicked();
        void onAutoPlayClicked();
        void onPikachuClicked(int clickCounter, Pikachu... pikachus); // Truyền cả đối tượng Pikachu
    }

    // --- Override setBackgroundImage ---
    @Override
    public void setBackgroundImage(String imagePath) {
        super.setBackgroundImage(imagePath);
        this.repaint(); // Yêu cầu vẽ lại toàn bộ panel khi nền thay đổi
    }

} // Kết thúc lớp PlayGameView