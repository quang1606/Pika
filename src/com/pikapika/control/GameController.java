package com.pikapika.control;

import com.pikapika.utils.Utils;
import com.pikapika.view.MenuView;
// Pikachu vẫn cần thiết cho onPikachuClicked và các hàm check cũ nếu chưa xóa
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
 * Updated with Suggestion feature (uses coordinates) & CardLayout.
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
        menuView = new MenuView("../resources/menu_bg.png");
        playGameView = new PlayGameView(MAP_ROW, MAP_COL);
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

        // --- Khởi tạo Timer và Action ---
        this.timeAction = e -> { // Sử dụng lambda expression cho gọn
            if (countDown > 0) {
                --countDown;
                playGameView.updateProgress(countDown);
                playGameView.updateTimer("Time: " + countDown);
                if (countDown == 0) {
                    timer.stop();
                    Utils.debug(getClass(), "Time out!");
                    JOptionPane.showMessageDialog(playGameView, "HẾT GIỜ, GAME OVER!\nĐiểm của bạn: " + score, "Thông báo", JOptionPane.WARNING_MESSAGE);
                    cardLayout.show(mainPanel, MENU_PANEL);
                }
            } else {
                timer.stop();
            }
        };
        this.timer = new Timer(1000, timeAction);

        // --- Cài đặt Listener cho Splash View ---
        this.splashView.setLoadingListener(new SplashView.OnLoadingListener() {
            @Override public void onStartLoading() {}
            @Override public void onLoading() {}
            @Override
            public void onStopLoading() {
                cardLayout.show(mainPanel, MENU_PANEL);
            }
        });

        // --- Cài đặt Listener cho Menu View ---
        this.menuView.setOnClickMenuListener(new MenuView.OnClickMenuListener() {
            @Override
            public void onNewGameClicked(int type) {
                Utils.debug(getClass(), "New Game Clicked - Type: " + type);
                score = 0; scoreSum = 0; mapNumber = 1; coupleDone = 0;
                switch (type) {
                    case MenuView.TYPE_EASY:   countDown = 180; break;
                    case MenuView.TYPE_MEDIUM: countDown = 120; break;
                    case MenuView.TYPE_HARD:   countDown = 90;  break;
                    default:                   countDown = 120; break;
                }
                Utils.debug(getClass(), "Starting game with countdown: " + countDown);

                // Khởi tạo màn chơi mới
                playGameView.renderMap(matrix.renderMatrix()); // renderMatrix giờ chỉ trả về int[][]
                int i = (new Random()).nextInt(5);
                playGameView.setBackgroundImage("../resources/bg_" + i + ".png");
                playGameView.updateMaxProgress(countDown);
                playGameView.updateProgress(countDown);
                playGameView.updateScore("Score: " + score);
                playGameView.updateTimer("Time: " + countDown);
                playGameView.updateMapNum("Map: " + mapNumber);
                playGameView.clearSuggestion();

                cardLayout.show(mainPanel, PLAY_PANEL);
                timer.start();
            }

            @Override
            public void onSettingClicked() {
                Utils.debug(getClass(), "Setting Clicked - Not implemented yet");
                JOptionPane.showMessageDialog(menuView, "Chức năng Cài đặt chưa được cài đặt!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void onQuitClicked() {
                Utils.debug(getClass(), "Quit Clicked from Menu");
                int choice = JOptionPane.showConfirmDialog(menuView,"Bạn có chắc chắn muốn thoát game?","Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });

        // --- Cài đặt Listener cho Play Game View ---
        this.playGameView.setPlayGameListener(new PlayGameView.PlayGameListener() {

            // --- Xử lý nút Gợi ý (HIỂN THỊ gợi ý) ---
            @Override
            public void onSuggestClicked() {
                Utils.debug(getClass(), "[Controller] Suggest button action received");
                if (matrix != null && playGameView != null && timer.isRunning()) {
                    // <<< SỬA ĐỔI: Gọi phương thức trả về tọa độ >>>
                    Point[] suggestionCoords = matrix.suggestPairCoords();

                    if (suggestionCoords != null && suggestionCoords.length == 2) {
                        Point coord1 = suggestionCoords[0];
                        Point coord2 = suggestionCoords[1];
                        // <<< SỬA ĐỔI: Cập nhật log để hiển thị tọa độ >>>
                        Utils.debug(getClass(), "[Controller] Suggestion coordinates found: [" + coord1.x + "," + coord1.y + "] & [" + coord2.x + "," + coord2.y + "]");

                        // <<< SỬA ĐỔI: Gọi phương thức mới của View để hiển thị gợi ý theo tọa độ >>>
                        // Sử dụng invokeLater để đảm bảo an toàn luồng khi cập nhật UI
                        SwingUtilities.invokeLater(() -> {
                            playGameView.displaySuggestionAt(coord1, coord2);
                        });

                    } else {
                        Utils.debug(getClass(), "[Controller] No suggestion available (suggestPairCoords returned null or invalid).");
                        // Xóa gợi ý cũ nếu có và thông báo
                        SwingUtilities.invokeLater(() -> {
                            playGameView.clearSuggestion();
                            JOptionPane.showMessageDialog(playGameView,"Không tìm thấy nước đi gợi ý nào!","Hết nước?",JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } else {
                    Utils.debug(getClass(), "[Controller] Suggest ignored (game not running or components null)");
                }
            }

            // --- Xử lý nút Chơi lại ---
            @Override
            public void onReplayClicked() {
                Utils.debug(getClass(), "[Controller] Replay Clicked");
                timer.stop();
                // Sử dụng invokeLater cho các cập nhật UI
                SwingUtilities.invokeLater(() -> {
                    playGameView.clearSuggestion();
                    score = scoreSum; coupleDone = 0;
                    countDown = playGameView.getMaxCountDown();
                    Utils.debug(getClass(), "Replaying level " + mapNumber + " with countdown: " + countDown);
                    playGameView.updateMap(matrix.renderMatrix()); // Lấy ma trận mới từ logic
                    playGameView.updateProgress(countDown);
                    playGameView.updateScore("Score: " + score);
                    playGameView.updateTimer("Time: " + countDown);
                    playGameView.updateMapNum("Map: " + mapNumber);
                    timer.start(); // Khởi động lại timer sau khi UI cập nhật
                });
            }

            // --- Xử lý nút Tạm dừng ---
            @Override
            public void onPauseClicked() {
                Utils.debug(getClass(), "[Controller] Pause Clicked");
                if (timer.isRunning()) {
                    timer.stop();
                    // Sử dụng invokeLater để chuyển card layout
                    SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, PAUSE_PANEL));
                }
            }

            // --- Xử lý khi người dùng click vào Pikachu ---
            @Override
            public void onPikachuClicked(int clickCounter, Pikachu... pikachus) {
                // Debug message
                Utils.debug(getClass(), "[Controller] Pikachu Clicked: count=" + clickCounter + ", p1=" + (pikachus.length > 0 ? pikachus[0].getIndex() : "null") + (pikachus.length > 1 && pikachus[1] != null ? ", p2=" + pikachus[1].getIndex() : ""));

                // --- Xử lý click khi game đang PAUSE ---
                if (!timer.isRunning() && clickCounter == 1) {
                    if (pikachus != null && pikachus.length > 0 && pikachus[0] != null) {
                        // Sử dụng invokeLater để cập nhật UI
                        SwingUtilities.invokeLater(() -> {
                            pikachus[0].removeBorder();
                            pikachus[0].repaint(); // Đảm bảo xóa border hiển thị
                        });
                    }
                    playGameView.setCountClicked(0);
                    Utils.debug(getClass(), "[Controller] Click handled to deselect while paused");
                    return;
                }
                if (!timer.isRunning() && clickCounter == 2) {
                    Utils.debug(getClass(), "[Controller] Second click ignored (timer not running)");
                    return;
                }
                // --- Kết thúc xử lý khi PAUSE ---

                // --- Xử lý click khi game đang RUNNING ---
                if (clickCounter == 1) {
                    // Sử dụng invokeLater
                    SwingUtilities.invokeLater(() -> {
                        playGameView.clearSuggestion(); // Xóa gợi ý trực quan
                        pikachus[0].drawBorder(Color.red);
                        pikachus[0].repaint(); // Hiển thị border ngay
                    });
                } else if (clickCounter == 2) {
                    // Sử dụng invokeLater
                    SwingUtilities.invokeLater(() -> {
                        pikachus[1].drawBorder(Color.red);
                        pikachus[1].repaint(); // Hiển thị border ngay
                    });

                    timer.stop(); // Dừng timer NGAY LẬP TỨC để xử lý logic

                    Pikachu p1 = pikachus[0]; Pikachu p2 = pikachus[1];

                    // <<< SỬA ĐỔI: Gọi algorithm với tọa độ >>>
                    boolean isMatch = matrix.algorithm(p1.getXPoint(), p1.getYPoint(), p2.getXPoint(), p2.getYPoint());

                    if (isMatch) {
                        Utils.debug(getClass(), "[Controller] Match found!");
                        // --- Xử lý chọn đúng (Cập nhật logic trước) ---
                        matrix.setXY(p1.getXPoint(), p1.getYPoint(), 0);
                        matrix.setXY(p2.getXPoint(), p2.getYPoint(), 0);
                        coupleDone++; score += 100;

                        // --- Cập nhật UI sau (trong invokeLater) ---
                        SwingUtilities.invokeLater(() -> {
                            p1.removeBorder(); p1.repaint();
                            p2.removeBorder(); p2.repaint();
                            p1.setVisible(false);
                            p2.setVisible(false);
                            playGameView.updateScore("Score: " + score);

                            // --- Kiểm tra trạng thái game (trong invokeLater vì có thể hiện Dialog) ---
                            int totalPairs = (matrix.getRow() - 2) * (matrix.getCol() - 2) / 2;
                            if (coupleDone == totalPairs) {
                                Utils.debug(getClass(), "[Controller] Level " + mapNumber + " complete!");
                                ++mapNumber;
                                if (mapNumber <= 3) {
                                    score += countDown * 10 + 500; scoreSum = score;
                                    countDown = Math.max(45, playGameView.getMaxCountDown() - 15 * (mapNumber - 1));
                                    coupleDone = 0;
                                    Utils.debug(getClass(), "Starting level " + mapNumber + " with countdown: " + countDown);
                                    playGameView.updateMaxProgress(countDown);
                                    playGameView.updateProgress(countDown);
                                    playGameView.updateMap(matrix.renderMatrix()); // Lấy ma trận mới
                                    playGameView.updateTimer("Time: " + countDown);
                                    playGameView.updateMapNum("Map: " + mapNumber);
                                    playGameView.updateScore("Score: " + score);
                                    playGameView.clearSuggestion();
                                    timer.start();
                                } else {
                                    Utils.debug(getClass(), "[Controller] Game Won!");
                                    JOptionPane.showMessageDialog(playGameView, "CHÚC MỪNG BẠN ĐÃ CHIẾN THẮNG!\nTổng điểm: " + score, "Chiến thắng!", JOptionPane.INFORMATION_MESSAGE);
                                    cardLayout.show(mainPanel, MENU_PANEL);
                                }
                            } else if (!matrix.canPlay()) { // Kiểm tra hết nước bằng logic mới
                                Utils.debug(getClass(), "[Controller] No more moves possible!");
                                JOptionPane.showMessageDialog(playGameView, "Không còn nước đi nào!\nGAME OVER!\nĐiểm của bạn: " + score, "Hết nước", JOptionPane.WARNING_MESSAGE);
                                cardLayout.show(mainPanel, MENU_PANEL);
                            } else { // Tiếp tục màn
                                playGameView.setCountClicked(0);
                                timer.start();
                            }
                        }); // Kết thúc invokeLater cho xử lý đúng

                    } else {
                        // --- Xử lý chọn sai (Cập nhật UI trong invokeLater) ---
                        SwingUtilities.invokeLater(() -> {
                            Utils.debug(getClass(), "[Controller] Match failed.");
                            p1.removeBorder(); p1.repaint();
                            p2.removeBorder(); p2.repaint();
                            playGameView.setCountClicked(0);
                            timer.start(); // Bắt đầu lại timer sau khi xóa border
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
                // Sử dụng invokeLater
                SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, PLAY_PANEL));
                if (countDown > 0) {
                    timer.start();
                }
            }
            @Override
            public void onBackMenuClicked() {
                Utils.debug(getClass(), "[Controller] Back to Menu Clicked from Pause");
                int choice = JOptionPane.showConfirmDialog(pauseMenuView,"Quay về Menu chính sẽ kết thúc màn chơi hiện tại.\nBạn có chắc chắn?","Xác nhận quay về Menu",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    // Sử dụng invokeLater
                    SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, MENU_PANEL));
                }
            }
            @Override
            public void onQuitClicked() {
                Utils.debug(getClass(), "[Controller] Quit Clicked from Pause");
                int choice = JOptionPane.showConfirmDialog(pauseMenuView,"Bạn có chắc chắn muốn thoát game?","Xác nhận thoát",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose(); System.exit(0);
                }
            }
        }); // --- Kết thúc cài đặt PauseMenuListener ---
    } // end frameInit

    /**
     * Bắt đầu game bằng cách hiển thị Splash Screen.
     */
    public void start() {
        setSize(Utils.WINDOW_WIDTH, Utils.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);

        // Sử dụng invokeLater để đảm bảo hiển thị card diễn ra trên EDT
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(mainPanel, SPLASH_PANEL);
            if (splashView != null) {
                splashView.start();
            } else {
                System.err.println("Lỗi: SplashView chưa được khởi tạo! Chuyển thẳng đến Menu.");
                cardLayout.show(mainPanel, MENU_PANEL);
            }
        });
    }
}