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

import static com.pikapika.utils.Utils.BT_PAUSE;

/**
 * Created by anonymousjp on 5/20/17.
 * Updated with Suggestion feature components using coordinate-based approach.
 */
public class PlayGameView extends JpanelBackground implements ActionListener {

    // ... (Các thuộc tính giữ nguyên) ...
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
    private JLabel mapCount;
    private PlayGameListener playGameListener;
    private GridLayout pikachuLayout;
    private Pikachu[][] pikachuIcon; // Mảng chứa các nút Pikachu thực tế
    private int row;
    private int col;
    private int countClicked = 0;
    private Pikachu one;
    private Pikachu two;
    private Pikachu suggestedP1 = null;
    private Pikachu suggestedP2 = null;
    private Timer suggestionTimer;
    private final int SUGGESTION_DURATION_MS = 1500;
    private final Color SUGGEST_BORDER_COLOR = Color.BLACK;


    // Constructors
    public PlayGameView() {
        // Kích thước mặc định nên lấy từ Utils nếu có hằng số
        this(Utils.MAP_ROW, Utils.MAP_COL); // Giả sử Utils có MAP_ROW, MAP_COL
    }

    public PlayGameView(int row, int col) {
        super();
        // Thêm kiểm tra đầu vào cơ bản
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

        // Top Menu Panel Setup
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
        timerProgress.setStringPainted(false); // Thường không cần hiển thị %

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
        btnSuggest.addActionListener(this);

        setupTopMenuLayout();

        // Pikachu Board Panel Setup
        // Kích thước GridLayout là số ô bên trong (không tính viền)
        pikachuLayout = new GridLayout(row - 2, col - 2, 2, 2); // Khoảng cách giữa các nút
        pikachuPanel = new JPanel(pikachuLayout);
        pikachuPanel.setOpaque(false);
        pikachuPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Padding cho panel

        // Add Panels to Main Layout
        add(topMenuPanel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel(new GridBagLayout()); // Dùng GridBag để căn giữa pikachuPanel
        centerPanel.setOpaque(false);
        centerPanel.add(pikachuPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    // --- Tách hàm cài đặt GroupLayout ---
    private void setupTopMenuLayout() {
        // ... (Giữ nguyên implementation của setupTopMenuLayout) ...
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
        // ... (Giữ nguyên implementation của configureButton) ...
        try {
            java.net.URL imgUrl = getClass().getResource(iconPath);
            if (imgUrl != null) {
                Image image = new ImageIcon(imgUrl).getImage();
                Icon icon = new ImageIcon(image.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                button.setIcon(icon);
            } else {
                System.err.println("Không tìm thấy resource icon: " + iconPath);
                button.setText(actionCommand.substring(0,1)); // Hiện chữ cái đầu nếu lỗi icon
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
        // ... (Giữ nguyên implementation của createInfoLabel) ...
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
                if (!clickedPikachu.isVisible()) return; // Bỏ qua nút đã ẩn

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
                            one.removeBorder();
                            one.repaint(); // Yêu cầu vẽ lại để xóa border đỏ
                            countClicked = 0;
                            one = null;
                        } else {
                            countClicked = 0; // Reset nếu one bị null
                            one = null;
                        }
                        // Controller sẽ reset countClicked về 0 sau khi xử lý xong
                        break;
                    default: // Trường hợp lỗi, reset
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
                case "SUGGEST": playGameListener.onSuggestClicked(); break; // Giữ nguyên việc gửi sự kiện lên Controller
                default: Utils.debug(getClass(), "Unknown button command: " + command); break;
            }
        }
    }

    // --- Render Map (màn mới) ---
    public void renderMap(int[][] matrix) {
        clearSuggestion(); // Xóa gợi ý cũ
        pikachuIcon = new Pikachu[row][col]; // Khởi tạo mảng chứa các nút
        pikachuPanel.removeAll(); // Xóa các nút cũ khỏi panel

        // Lặp qua các ô bên trong (không tính viền)
        for (int i = 1; i < row - 1; i++) {
            for (int j = 1; j < col - 1; j++) {
                // Tạo nút Pikachu mới cho ô này
                pikachuIcon[i][j] = createButton(i, j);
                // Lấy icon dựa trên giá trị từ ma trận logic
                Icon icon = getIcon(matrix[i][j]);
                pikachuIcon[i][j].setIcon(icon);
                // Đặt trạng thái hiển thị (ẩn nếu giá trị là 0)
                pikachuIcon[i][j].setVisible(matrix[i][j] != 0);
                // Đặt border mặc định là null (không viền)
                pikachuIcon[i][j].setBorder(null);
                // Thêm nút vào panel chứa (pikachuPanel)
                pikachuPanel.add(pikachuIcon[i][j]);
            }
        }
        // Cập nhật lại layout và vẽ lại panel
        pikachuPanel.revalidate();
        pikachuPanel.repaint();
        // Reset trạng thái click
        countClicked = 0;
        one = null; two = null;
    }

    // --- Update Map (chơi lại / qua màn) ---
    public void updateMap(int[][] matrix) {
        clearSuggestion();
        countClicked = 0;
        one = null; two = null;

        // Lặp qua các ô bên trong
        for (int i = 1; i < row - 1; i++) {
            for (int j = 1; j < col - 1; j++) {
                // Kiểm tra xem nút tại vị trí này có tồn tại không
                if (pikachuIcon[i][j] != null) {
                    // Cập nhật icon
                    pikachuIcon[i][j].setIcon(getIcon(matrix[i][j]));
                    // Reset border về null
                    pikachuIcon[i][j].setBorder(null);
                    // Cập nhật trạng thái hiển thị
                    pikachuIcon[i][j].setVisible(matrix[i][j] != 0);
                    // Yêu cầu nút vẽ lại (đề phòng trường hợp chỉ thay đổi setVisible)
                    pikachuIcon[i][j].repaint();
                } else {
                    // Có thể log lỗi nếu cần: Nút tại [i][j] bị null trong khi update
                    System.err.println("Warning: pikachuIcon[" + i + "][" + j + "] is null during updateMap.");
                }
            }
        }
        // Cập nhật layout và vẽ lại panel chứa
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

    // --- Tạo Pikachu Button ---
    private Pikachu createButton(int r, int c) { // Đổi x,y thành r,c cho rõ ràng là hàng, cột
        Pikachu btn = new Pikachu(r, c);
        btn.setBorder(null); // Không viền mặc định
        btn.setContentAreaFilled(false); // Nền trong suốt
        btn.setFocusPainted(false); // Bỏ viền focus khi click
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Đổi con trỏ khi hover
        btn.addActionListener(this); // Thêm listener để xử lý click
        return btn;
    }

    // --- Setter for Listener ---
    public void setPlayGameListener(PlayGameListener listener) {
        this.playGameListener = listener;
    }

    // --- Update UI Methods ---
    public void updateTimer(String timerText) { this.timer.setText(timerText); }
    public void updateScore(String scoreText) { this.score.setText(scoreText); }
    public void updateMapNum(String mapText) { this.mapCount.setText(mapText); }
    public void setCountClicked(int value) { this.countClicked = value; }
    public void updateProgress(int progressValue) {
        int min = timerProgress.getMinimum(); int max = timerProgress.getMaximum();
        // Đảm bảo giá trị nằm trong khoảng min-max
        timerProgress.setValue(Math.max(min, Math.min(max, progressValue)));
    }
    public void updateMaxProgress(int maxProgress) {
        timerProgress.setMaximum(maxProgress);
        timerProgress.setValue(maxProgress); // Reset thanh progress về đầy khi set max mới
    }
    public int getMaxCountDown() { return timerProgress.getMaximum(); }

    // --- Suggestion Management Methods ---

    /**
     * Phương thức MỚI: Nhận tọa độ từ Controller, tìm nút Pikachu thực tế và hiển thị gợi ý.
     * Giả định: Point.x là hàng, Point.y là cột.
     * @param p1Coord Tọa độ (hàng, cột) của Pikachu thứ nhất.
     * @param p2Coord Tọa độ (hàng, cột) của Pikachu thứ hai.
     */
    public void displaySuggestionAt(Point p1Coord, Point p2Coord) {
        if (p1Coord == null || p2Coord == null) {
            Utils.debug(getClass(), "Received null coordinates for suggestion.");
            return;
        }

        Utils.debug(getClass(), "Attempting to find Pikachu at coordinates: [" + p1Coord.x + "," + p1Coord.y + "] and [" + p2Coord.x + "," + p2Coord.y + "]");

        try {
            // --- Lấy đối tượng Pikachu THỰC TẾ từ mảng pikachuIcon ---
            // *** QUAN TRỌNG: Đảm bảo p1Coord.x là HÀNG, p1Coord.y là CỘT ***
            // Nếu Point của bạn lưu (cột, hàng) thì phải đảo lại: pikachuIcon[p1Coord.y][p1Coord.x]
            Pikachu actualP1 = pikachuIcon[p1Coord.x][p1Coord.y];
            Pikachu actualP2 = pikachuIcon[p2Coord.x][p2Coord.y];

            // Kiểm tra xem có lấy được đối tượng không và chúng có đang hiển thị không
            if (actualP1 != null && actualP1.isVisible() && actualP2 != null && actualP2.isVisible()) {
                Utils.debug(getClass(), "Found actual Pikachu objects: " + actualP1.getIndex() + " and " + actualP2.getIndex());
                // Gọi phương thức nội bộ để thực hiện hiển thị
                showSuggestionInternal(actualP1, actualP2);
            } else {
                Utils.debug(getClass(), "Could not find valid/visible Pikachu objects in grid at suggested coordinates.");
                // Có thể nút đã bị ẩn đi hoặc tọa độ sai
                if(actualP1 == null || actualP2 == null){
                    System.err.println("Error: Null Pikachu object found at suggested coordinates.");
                } else {
                    System.err.println("Error: Suggested Pikachu objects are not visible.");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Log lỗi nếu tọa độ nằm ngoài phạm vi mảng
            Utils.debug(getClass(), "Error: Suggested coordinates [" + p1Coord.x + "," + p1Coord.y + "] or [" + p2Coord.x + "," + p2Coord.y + "] are out of bounds for pikachuIcon array. " + e.getMessage());
        } catch (Exception e) {
            // Bắt các lỗi khác có thể xảy ra
            Utils.debug(getClass(), "Unexpected error accessing pikachuIcon array at suggested coordinates: " + e.getMessage());
            e.printStackTrace(); // In stack trace để debug chi tiết
        }
    }


    /**
     * Phương thức nội bộ: Thực hiện việc vẽ border và bắt đầu timer gợi ý.
     * Được gọi bởi displaySuggestionAt sau khi đã có đối tượng Pikachu thực tế.
     * @param p1 Pikachu thứ nhất cần hiển thị gợi ý.
     * @param p2 Pikachu thứ hai cần hiển thị gợi ý.
     */
    private void showSuggestionInternal(Pikachu p1, Pikachu p2) {
        clearSuggestion(); // Xóa gợi ý cũ trước khi hiển thị gợi ý mới
        this.suggestedP1 = p1;
        this.suggestedP2 = p2;

        // Kiểm tra lại lần nữa cho chắc chắn (dù displaySuggestionAt đã kiểm tra)
        if (this.suggestedP1 != null && this.suggestedP2 != null) {
            Utils.debug(getClass(), "Internal: Showing suggestion for " + suggestedP1.getIndex() + " and " + suggestedP2.getIndex());

            // Vẽ border vàng
            this.suggestedP1.drawBorder(SUGGEST_BORDER_COLOR);
            this.suggestedP2.drawBorder(SUGGEST_BORDER_COLOR);

            // Yêu cầu các nút vẽ lại NGAY LẬP TỨC để hiển thị border
            this.suggestedP1.repaint();
            this.suggestedP2.repaint();

            // Bắt đầu timer để tự động xóa gợi ý sau một khoảng thời gian
            startSuggestionTimer();
        } else {
            // Trường hợp này không nên xảy ra nếu displaySuggestionAt hoạt động đúng
            Utils.debug(getClass(), "Internal Error: Null Pikachu(s) received in showSuggestionInternal.");
        }
    }

    // --- Xóa gợi ý trực quan ---
    public void clearSuggestion() {
        stopSuggestionTimer(); // Dừng timer (nếu đang chạy)

        // Xóa border và repaint cho từng nút *trước khi* gán null
        if (suggestedP1 != null) {
            suggestedP1.removeBorder(); // Xóa border (ví dụ: setBorder(null))
            suggestedP1.repaint();      // <<< Yêu cầu nút vẽ lại để xóa border
            suggestedP1 = null;         // Đặt tham chiếu về null
        }
        if (suggestedP2 != null) {
            suggestedP2.removeBorder();
            suggestedP2.repaint();      // <<< Yêu cầu nút vẽ lại để xóa border
            suggestedP2 = null;         // Đặt tham chiếu về null
        }

        // Việc repaint panel chứa (pikachuPanel) thường không cần thiết
        // trừ khi bạn vẽ đường nối trực tiếp lên panel.
    }

    // --- Bắt đầu timer tự động xóa gợi ý ---
    private void startSuggestionTimer() {
        stopSuggestionTimer(); // Đảm bảo timer cũ đã dừng
        // Tạo timer mới, hành động là gọi clearSuggestion
        suggestionTimer = new Timer(SUGGESTION_DURATION_MS, e -> {
            // Sử dụng SwingUtilities.invokeLater để đảm bảo clearSuggestion chạy trên EDT
            SwingUtilities.invokeLater(this::clearSuggestion);
        });
        suggestionTimer.setRepeats(false); // Chỉ chạy một lần
        suggestionTimer.start();
    }

    // --- Dừng timer gợi ý ---
    private void stopSuggestionTimer() {
        if (suggestionTimer != null && suggestionTimer.isRunning()) {
            suggestionTimer.stop();
        }
        suggestionTimer = null; // Xóa tham chiếu timer cũ
    }

    // --- Listener Interface ---
    public interface PlayGameListener {
        void onReplayClicked();
        void onPauseClicked();
        void onSuggestClicked(); // Controller sẽ gọi khi nút Gợi ý được nhấn
        void onPikachuClicked(int clickCounter, Pikachu... pikachus); // Xử lý click vào Pikachu
    }

    // --- Override setBackgroundImage ---
    @Override
    public void setBackgroundImage(String imagePath) {
        super.setBackgroundImage(imagePath);
        // Thêm repaint nếu cần
        // this.repaint();
    }

    /*
     * --- Bỏ phương thức showSuggestion cũ (hoặc giữ lại với tên khác nếu cần cho mục đích khác) ---
     * Phương thức này không còn được Controller gọi trực tiếp nữa.
     * displaySuggestionAt là điểm vào mới.
     */
    /*
    public void showSuggestion(Pikachu p1, Pikachu p2) {
        // ... code cũ với debug hash code ...
        // ĐÃ ĐƯỢC THAY THẾ BỞI displaySuggestionAt và showSuggestionInternal
    }
    */

} // Kết thúc lớp PlayGameView