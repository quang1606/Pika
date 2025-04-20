package com.pikapika.control;

import com.pikapika.utils.Utils;
import com.pikapika.view.MenuView;
// Pikachu vẫn cần thiết cho onPikachuClicked
import com.pikapika.view.Pikachu;
import com.pikapika.view.PlayGameView;
import com.pikapika.view.SplashView;
import com.pikapika.view.PauseMenuView;
import com.pikapika.view.PauseMenuView.PauseMenuListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
// Import Point để nhận tọa độ từ Matrix
import java.awt.Point;

import static com.pikapika.utils.Utils.MAP_COL;
import static com.pikapika.utils.Utils.MAP_ROW;

/**
 * Created by anonymousjp on 5/20/17.
 * Updated with Auto Play feature integrated with Menu and Play views.
 */
public class GameController extends JFrame {

    // Các View
    private SplashView splashView;
    private MenuView menuView;
    private PlayGameView playGameView;
    private PauseMenuView pauseMenuView;

    // Logic Game
    private Matrix matrix; // Đối tượng Matrix chứa logic game và ma trận
    private Timer timer; // Timer đếm ngược của game
    private int countDown;
    private int score;
    private int scoreSum;
    private int mapNumber;
    private int coupleDone; // Số cặp đã hoàn thành trong màn hiện tại
    private ActionListener timeAction;

    // --- Thành phần cho CardLayout ---
    private JPanel mainPanel; // Panel chính chứa các màn hình (cards)
    private CardLayout cardLayout;
    // Tên định danh cho các màn hình (cards)
    private final String SPLASH_PANEL = "Splash";
    private final String MENU_PANEL = "Menu";
    private final String PLAY_PANEL = "Play";
    private final String PAUSE_PANEL = "Pause";

    // <<< THÊM: Trạng thái và Timer cho Auto Play >>>
    private boolean isAutoPlaying = false;
    private Timer autoPlayTimer;
    private final int AUTO_PLAY_DELAY_MS = 1000; // Độ trễ giữa các bước tự động (ms)


    public GameController(String title) throws HeadlessException {
        super(title);
        // --- Cài đặt Icon ---
        try {
            Image icon = (new ImageIcon(getClass().getResource("../resources/pika_icon.png"))).getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Lỗi tải file icon: ../resources/pika_icon.png - " + e.getMessage());
        }
        // --- Cài đặt cơ bản cho JFrame ---
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    protected void frameInit() {
        super.frameInit();

        // --- Khởi tạo CardLayout và Panel chính ---
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Khởi tạo các View ---
        splashView = new SplashView("../resources/splash_background.png");
        menuView = new MenuView("../resources/menu_bg.png"); // MenuView đã sửa
        playGameView = new PlayGameView(MAP_ROW, MAP_COL);   // PlayGameView đã sửa
        pauseMenuView = new PauseMenuView("../resources/menu_bg.png");

        // --- Thêm các View vào mainPanel ---
        mainPanel.add(splashView, SPLASH_PANEL);
        mainPanel.add(menuView, MENU_PANEL);
        mainPanel.add(playGameView, PLAY_PANEL);
        mainPanel.add(pauseMenuView, PAUSE_PANEL);

        // --- Thêm mainPanel vào ContentPane ---
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // --- Khởi tạo Ma trận ---
        this.matrix = new Matrix(MAP_ROW, MAP_COL); // Sử dụng lớp Matrix đã sửa

        // --- Khởi tạo Timer chính và Action ---
        this.timeAction = e -> { // Sử dụng lambda expression cho gọn
            // <<< THÊM: Tạm dừng timer chính nếu Auto Play đang chạy (tránh hết giờ khi máy đang chơi) >>>
            if (isAutoPlaying) {
                // Không giảm countDown khi máy đang chơi
                return;
            }

            if (countDown > 0) {
                --countDown;
                playGameView.updateProgress(countDown);
                playGameView.updateTimer("Time: " + countDown);
                if (countDown == 0) {
                    timer.stop();
                    // Dừng auto play nếu nó vô tình vẫn chạy
                    stopAutoPlay();
                    Utils.debug(getClass(), "Time out!");
                    JOptionPane.showMessageDialog(playGameView, "HẾT GIỜ, GAME OVER!\nĐiểm của bạn: " + score, "Thông báo", JOptionPane.WARNING_MESSAGE);
                    cardLayout.show(mainPanel, MENU_PANEL);
                }
            } else {
                timer.stop();
            }
        };
        this.timer = new Timer(1000, timeAction);

        // <<< THÊM: Khởi tạo Timer cho Auto Play >>>
        this.autoPlayTimer = new Timer(AUTO_PLAY_DELAY_MS, e -> performAutoPlayStep());
        this.autoPlayTimer.setRepeats(true); // Timer này lặp lại cho mỗi bước

        // --- Cài đặt Listener cho Splash View ---
        this.splashView.setLoadingListener(new SplashView.OnLoadingListener() {
            @Override public void onStartLoading() {}
            @Override public void onLoading() {}
            @Override
            public void onStopLoading() {
                // Chuyển sang Menu trên EDT
                SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, MENU_PANEL));
            }
        });

        // --- Cài đặt Listener cho Menu View (Đã cập nhật interface) ---
        this.menuView.setOnClickMenuListener(new MenuView.OnClickMenuListener() {
            @Override
            public void onNewGameClicked(int type) {
                // Dừng auto play nếu đang chạy (ví dụ từ màn trước)
                stopAutoPlay();
                // --- Logic khởi tạo game bình thường ---
                Utils.debug(getClass(), "New Game Clicked - Type: " + type);
                startGame(type, false); // Gọi hàm helper, false nghĩa là không tự động chơi
            }

            // <<< THAY ĐỔI: Xử lý sự kiện Auto Play từ Menu >>>
            @Override
            public void onAutoPlayClicked() {
                Utils.debug(getClass(), "Auto Play Clicked from Menu");
                // Chọn độ khó mặc định hoặc có thể thêm lựa chọn
                int gameType = MenuView.TYPE_MEDIUM;
                startGame(gameType, true); // Gọi hàm helper, true nghĩa là tự động chơi ngay
            }

            @Override
            public void onQuitClicked() {
                Utils.debug(getClass(), "Quit Clicked from Menu");
                confirmAndQuit(menuView); // Gọi hàm helper để thoát
            }
            // Không còn onSettingClicked()
        });

        // --- Cài đặt Listener cho Play Game View (Đã cập nhật interface) ---
        this.playGameView.setPlayGameListener(new PlayGameView.PlayGameListener() {

            @Override
            public void onSuggestClicked() {
                // <<< THÊM: Không cho gợi ý khi đang Auto Play >>>
                if (isAutoPlaying) {
                    Utils.debug(getClass(), "[Controller] Suggest ignored (Auto Play active)");
                    return;
                }
                // Logic gợi ý cũ giữ nguyên, đảm bảo dùng invokeLater
                Utils.debug(getClass(), "[Controller] Suggest button action received");
                if (matrix != null && playGameView != null && timer.isRunning()) {
                    Point[] suggestionCoords = matrix.suggestPairCoords();
                    if (suggestionCoords != null && suggestionCoords.length == 2) {
                        Point coord1 = suggestionCoords[0]; Point coord2 = suggestionCoords[1];
                        Utils.debug(getClass(), "[Controller] Suggestion coordinates found: [" + coord1.x + "," + coord1.y + "] & [" + coord2.x + "," + coord2.y + "]");
                        SwingUtilities.invokeLater(() -> playGameView.displaySuggestionAt(coord1, coord2));
                    } else {
                        Utils.debug(getClass(), "[Controller] No suggestion available.");
                        SwingUtilities.invokeLater(() -> {
                            playGameView.clearSuggestion();
                            JOptionPane.showMessageDialog(playGameView,"Không tìm thấy nước đi gợi ý nào!","Hết nước?",JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } else { Utils.debug(getClass(), "[Controller] Suggest ignored (game not running or components null)"); }
            }

            @Override
            public void onReplayClicked() {
                // <<< THÊM: Dừng Auto Play nếu đang chạy khi Replay >>>
                stopAutoPlayIfNeeded(); // Dừng auto play trước
                Utils.debug(getClass(), "[Controller] Replay Clicked");
                timer.stop(); // Dừng timer chính
                // Sử dụng invokeLater cho các cập nhật UI
                SwingUtilities.invokeLater(() -> {
                    playGameView.clearSuggestion();
                    score = scoreSum; coupleDone = 0; // Reset score về đầu màn
                    countDown = playGameView.getMaxCountDown(); // Lấy lại thời gian ban đầu của màn
                    Utils.debug(getClass(), "Replaying level " + mapNumber + " with countdown: " + countDown);
                    playGameView.updateMap(matrix.renderMatrix()); // Tạo ma trận mới
                    playGameView.updateProgress(countDown);
                    playGameView.updateScore("Score: " + score);
                    playGameView.updateTimer("Time: " + countDown);
                    playGameView.updateMapNum("Map: " + mapNumber);
                    timer.start(); // Khởi động lại timer chính
                });
            }

            @Override
            public void onPauseClicked() {
                // <<< THÊM: Dừng Auto Play nếu đang chạy khi Pause >>>
                stopAutoPlayIfNeeded();
                Utils.debug(getClass(), "[Controller] Pause Clicked");
                if (timer.isRunning()) {
                    timer.stop();
                    // Sử dụng invokeLater để chuyển card layout
                    SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, PAUSE_PANEL));
                }
            }

            // <<< THÊM: Xử lý nút Auto Play trên màn chơi >>>
            @Override
            public void onAutoPlayClicked() {
                Utils.debug(getClass(), "[Controller] Auto Play button clicked on Play Screen");
                if (isAutoPlaying) {
                    stopAutoPlay();
                } else {
                    // Chỉ bắt đầu nếu timer chính đang chạy (game đang active)
                    if (timer.isRunning()) {
                        startAutoPlay();
                    } else {
                        Utils.debug(getClass(), "[Controller] Cannot start Auto Play from Play Screen (game not running).");
                    }
                }
            }


            @Override
            public void onPikachuClicked(int clickCounter, Pikachu... pikachus) {
                // <<< THÊM: Bỏ qua click của người dùng nếu đang Auto Play >>>
                if (isAutoPlaying) {
                    Utils.debug(getClass(), "[Controller] User click ignored (Auto Play active)");
                    // Xóa viền nếu người dùng vô tình click ô đầu tiên
                    if(clickCounter == 1 && pikachus.length > 0 && pikachus[0] != null){
                        SwingUtilities.invokeLater(() -> {
                            pikachus[0].removeBorder();
                            pikachus[0].repaint();
                        });
                        playGameView.setCountClicked(0);
                    }
                    return;
                }

                // --- Logic xử lý click cũ giữ nguyên ---
                // (Đảm bảo có invokeLater cho các cập nhật UI)
                Utils.debug(getClass(), "[Controller] Pikachu Clicked: count=" + clickCounter + "...");
                if (!timer.isRunning() && clickCounter == 1) { /* xử lý bỏ chọn khi pause */
                    if (pikachus != null && pikachus.length > 0 && pikachus[0] != null) {
                        SwingUtilities.invokeLater(() -> { pikachus[0].removeBorder(); pikachus[0].repaint(); });
                    }
                    playGameView.setCountClicked(0);
                    return;
                }
                if (!timer.isRunning() && clickCounter == 2) { /* bỏ qua click 2 khi pause */ return; }

                if (clickCounter == 1) {
                    SwingUtilities.invokeLater(() -> {
                        playGameView.clearSuggestion();
                        pikachus[0].drawBorder(Color.red);
                        pikachus[0].repaint();
                    });
                } else if (clickCounter == 2) {
                    SwingUtilities.invokeLater(() -> { pikachus[1].drawBorder(Color.red); pikachus[1].repaint(); });
                    timer.stop(); // <<< DỪNG TIMER CHÍNH >>>

                    Pikachu p1 = pikachus[0]; Pikachu p2 = pikachus[1];
                    boolean isMatch = matrix.algorithm(p1.getXPoint(), p1.getYPoint(), p2.getXPoint(), p2.getYPoint());

                    if (isMatch) {
                        // Cập nhật logic trước
                        matrix.setXY(p1.getXPoint(), p1.getYPoint(), 0);
                        matrix.setXY(p2.getXPoint(), p2.getYPoint(), 0);
                        coupleDone++; score += 100;

                        // Cập nhật UI và kiểm tra trạng thái game sau
                        SwingUtilities.invokeLater(() -> {
                            p1.removeBorder(); p1.repaint(); p2.removeBorder(); p2.repaint();
                            p1.setVisible(false); p2.setVisible(false);
                            playGameView.updateScore("Score: " + score);
                            checkGameStatus(); // Gọi hàm helper kiểm tra trạng thái
                        });
                    } else {
                        // Xử lý chọn sai
                        SwingUtilities.invokeLater(() -> {
                            p1.removeBorder(); p1.repaint(); p2.removeBorder(); p2.repaint();
                            playGameView.setCountClicked(0);
                            timer.start(); // <<< KHỞI ĐỘNG LẠI TIMER CHÍNH >>>
                        });
                    }
                }
            } // end onPikachuClicked

        }); // --- Kết thúc cài đặt PlayGameListener ---

        // --- Cài đặt Listener cho Pause Menu View ---
        this.pauseMenuView.setPauseMenuListener(new PauseMenuListener() {
            @Override
            public void onContinueCliked() {
                Utils.debug(getClass(), "[Controller] Continue Clicked");
                // Chuyển lại màn chơi và tiếp tục timer chính
                SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, PLAY_PANEL));
                if (countDown > 0) {
                    timer.start(); // KHÔNG start auto play khi continue
                }
            }
            @Override
            public void onBackMenuClicked() {
                Utils.debug(getClass(), "[Controller] Back to Menu Clicked from Pause");
                int choice = JOptionPane.showConfirmDialog(pauseMenuView,"Quay về Menu chính sẽ kết thúc màn chơi hiện tại.\nBạn có chắc chắn?","Xác nhận quay về Menu",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    stopAutoPlayIfNeeded(); // Dừng auto play nếu đang chạy
                    timer.stop();          // Dừng timer chính
                    SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, MENU_PANEL));
                }
            }
            @Override
            public void onQuitClicked() {
                Utils.debug(getClass(), "[Controller] Quit Clicked from Pause");
                confirmAndQuit(pauseMenuView); // Gọi hàm helper
            }
        }); // --- Kết thúc cài đặt PauseMenuListener ---
    } // end frameInit

    // --- Hàm helper để bắt đầu game ---
    private void startGame(int gameType, boolean startAuto) {
        // Reset trạng thái game
        score = 0; scoreSum = 0; mapNumber = 1; coupleDone = 0;
        switch (gameType) {
            case MenuView.TYPE_EASY:   countDown = 180; break;
            case MenuView.TYPE_MEDIUM: countDown = 120; break;
            case MenuView.TYPE_HARD:   countDown = 90;  break;
            default:                   countDown = 120; break;
        }
        Utils.debug(getClass(), "Starting game (Auto=" + startAuto + ") with countdown: " + countDown);

        // --- Thực hiện các cập nhật UI trong invokeLater ---
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo màn chơi mới
            playGameView.renderMap(matrix.renderMatrix());
            int i = (new Random()).nextInt(5);
            playGameView.setBackgroundImage("../resources/bg_" + i + ".png");
            playGameView.updateMaxProgress(countDown);
            playGameView.updateProgress(countDown);
            playGameView.updateScore("Score: " + score);
            playGameView.updateTimer("Time: " + countDown);
            playGameView.updateMapNum("Map: " + mapNumber);
            playGameView.clearSuggestion();
            playGameView.updateAutoPlayButtonState(false); // Đảm bảo nút auto play reset

            // Chuyển sang màn hình Play Game
            cardLayout.show(mainPanel, PLAY_PANEL);
            timer.start(); // Bắt đầu đếm ngược chính

            // Nếu yêu cầu auto play, gọi startAutoPlay SAU KHI màn hình đã hiển thị và timer chính đã chạy
            if (startAuto) {
                // Có thể thêm một độ trễ nhỏ trước khi bắt đầu auto play để người dùng kịp nhìn
                Timer delayTimer = new Timer(100, e -> startAutoPlay());
                delayTimer.setRepeats(false);
                delayTimer.start();
                // Hoặc gọi trực tiếp nếu không cần trễ:
                // startAutoPlay();
            }
        });
    }

    // --- Hàm helper kiểm tra trạng thái game ---
    // Được gọi sau khi người dùng hoặc máy ăn 1 cặp
    private void checkGameStatus() {
        int totalPairs = (matrix.getRow() - 2) * (matrix.getCol() - 2) / 2;
        if (coupleDone == totalPairs) { // Hoàn thành màn
            Utils.debug(getClass(), "[Controller] Level " + mapNumber + " complete!");
            stopAutoPlayIfNeeded(); // Dừng auto play nếu đang chạy
            timer.stop(); // Dừng timer chính

            // Tính điểm thưởng, chuẩn bị màn mới hoặc kết thúc game
            score += countDown * 10 + 500; scoreSum = score; // Lưu điểm đầu màn mới
            ++mapNumber;

            if (mapNumber <= 3) { // Qua màn mới
                countDown = Math.max(45, playGameView.getMaxCountDown() - 15 * (mapNumber - 1));
                coupleDone = 0;
                Utils.debug(getClass(), "Starting level " + mapNumber + " with countdown: " + countDown);
                // Cập nhật UI cho màn mới
                SwingUtilities.invokeLater(()->{
                    playGameView.updateMaxProgress(countDown);
                    playGameView.updateProgress(countDown);
                    playGameView.updateMap(matrix.renderMatrix()); // Lấy ma trận mới
                    playGameView.updateTimer("Time: " + countDown);
                    playGameView.updateMapNum("Map: " + mapNumber);
                    playGameView.updateScore("Score: " + score);
                    playGameView.clearSuggestion();
                    playGameView.updateAutoPlayButtonState(false); // Reset nút auto play
                    timer.start(); // <<< BẮT ĐẦU TIMER CHÍNH CHO MÀN MỚI >>>
                    // KHÔNG tự động start auto play
                });
            } else { // Thắng game
                Utils.debug(getClass(), "[Controller] Game Won!");
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(playGameView, "CHÚC MỪNG BẠN ĐÃ CHIẾN THẮNG!\nTổng điểm: " + score, "Chiến thắng!", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, MENU_PANEL); // Về Menu
                });
            }
        } else if (!matrix.canPlay()) { // Hết nước đi
            Utils.debug(getClass(), "[Controller] No more moves possible!");
            stopAutoPlayIfNeeded(); // Dừng auto play
            timer.stop(); // Dừng timer chính
            // Thông báo hết nước
            SwingUtilities.invokeLater(()->{
                JOptionPane.showMessageDialog(playGameView, "Không còn nước đi nào!\nGAME OVER!\nĐiểm của bạn: " + score, "Hết nước", JOptionPane.WARNING_MESSAGE);
                cardLayout.show(mainPanel, MENU_PANEL); // Về Menu
            });
        } else { // Tiếp tục màn chơi (nếu là người chơi)
            if (!isAutoPlaying) { // Chỉ reset click và start timer nếu là người chơi
                playGameView.setCountClicked(0);
                timer.start(); // <<< KHỞI ĐỘNG LẠI TIMER CHÍNH >>>
            }
            // Nếu là auto play, không làm gì ở đây, autoPlayTimer sẽ tự gọi bước tiếp theo
        }
    }


    // --- Hàm helper để thoát game ---
    private void confirmAndQuit(Component parent) {
        int choice = JOptionPane.showConfirmDialog(parent,"Bạn có chắc chắn muốn thoát game?","Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            stopAutoPlayIfNeeded(); // Dừng auto play trước khi thoát
            timer.stop();          // Dừng timer chính
            dispose();
            System.exit(0);
        }
    }

    // --- Các phương thức điều khiển Auto Play ---
    private void startAutoPlay() {
        // Chỉ bắt đầu nếu game đang chạy (timer chính đang chạy) và chưa auto play
        if (!isAutoPlaying && timer.isRunning()) {
            // Hủy bỏ lựa chọn đang chờ của người dùng nếu có
            if (playGameView.one != null) {
                Pikachu p = playGameView.one;
                SwingUtilities.invokeLater(() -> { p.removeBorder(); p.repaint(); });
                playGameView.one = null;
                playGameView.setCountClicked(0);
            }
            playGameView.clearSuggestion();

            isAutoPlaying = true;
            Utils.debug(getClass(), "[Controller] Starting Auto Play...");
            SwingUtilities.invokeLater(() -> playGameView.updateAutoPlayButtonState(true));
            // TẠM DỪNG timer chính khi máy chơi để tránh hết giờ
            timer.stop();
            autoPlayTimer.start(); // Bắt đầu timer tự động
        } else {
            Utils.debug(getClass(), "[Controller] Cannot start Auto Play (Game not running or already auto playing).");
        }
    }

    private void stopAutoPlay() {
        if (isAutoPlaying) {
            autoPlayTimer.stop();
            isAutoPlaying = false;
            Utils.debug(getClass(), "[Controller] Stopping Auto Play.");
            // KHỞI ĐỘNG LẠI timer chính nếu còn thời gian
            if (countDown > 0 && !timer.isRunning()) {
                timer.start();
            }
            SwingUtilities.invokeLater(() -> playGameView.updateAutoPlayButtonState(false));
        }
    }

    // Hàm helper để dừng auto play nếu đang chạy
    private void stopAutoPlayIfNeeded(){
        if(isAutoPlaying){
            stopAutoPlay();
        }
    }

    // --- Phương thức thực hiện một bước Auto Play ---
    private void performAutoPlayStep() {
        if (!isAutoPlaying) { autoPlayTimer.stop(); return; } // Dừng nếu không còn auto play

        Utils.debug(getClass(), "[Controller] Performing Auto Play Step...");
        Point[] coords = matrix.suggestPairCoords();

        if (coords != null) { // Tìm thấy nước đi
            Point c1 = coords[0]; Point c2 = coords[1];
            Pikachu p1 = playGameView.getPikachuAt(c1.x, c1.y);
            Pikachu p2 = playGameView.getPikachuAt(c2.x, c2.y);

            if (p1 != null && p2 != null && p1.isVisible() && p2.isVisible()) {
                // Cập nhật logic game
                matrix.setXY(c1.x, c1.y, 0); matrix.setXY(c2.x, c2.y, 0);
                coupleDone++; score += 100;

                // Cập nhật UI và kiểm tra trạng thái (trong invokeLater)
                final Pikachu fp1 = p1; final Pikachu fp2 = p2;
                SwingUtilities.invokeLater(() -> {
                    fp1.setVisible(false); fp2.setVisible(false);
                    playGameView.updateScore("Score: " + score);
                    checkGameStatus(); // Kiểm tra thắng/qua màn/hết nước
                });
            } else { // Lỗi: không tìm thấy nút hợp lệ
                Utils.debug(getClass(), "[Controller] Auto Play Error: Could not find valid Pikachu objects. Stopping.");
                stopAutoPlay();
            }
        } else { // Hết nước đi
            Utils.debug(getClass(), "[Controller] Auto Play: No suggestion found. Stopping.");
            stopAutoPlay();
            // Kiểm tra lại trạng thái cuối cùng (có thể đã thắng)
            SwingUtilities.invokeLater(this::checkGameStatus); // Gọi checkGameStatus trên EDT
        }
    } // end performAutoPlayStep

    // --- Phương thức start() ---
    public void start() {
        setSize(Utils.WINDOW_WIDTH, Utils.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(mainPanel, SPLASH_PANEL);
            if (splashView != null) {
                splashView.start();
            } else {
                System.err.println("Lỗi: SplashView chưa được khởi tạo!");
                cardLayout.show(mainPanel, MENU_PANEL);
            }
        });
    }

} // end GameController