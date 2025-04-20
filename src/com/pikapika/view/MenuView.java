package com.pikapika.view;

// import com.pikapika.utils.Utils; // Utils đã được import qua static import
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Import tất cả hằng số từ Utils
import static com.pikapika.utils.Utils.*;

/**
 * Created by anonymousjp on 5/20/17.
 * Updated to replace Setting button with Auto Play button.
 */
public class MenuView extends JpanelBackground implements ActionListener{
    // Các hằng số loại game giữ nguyên
    public final static int TYPE_EASY = 0;
    public final static int TYPE_MEDIUM = 1;
    public final static int TYPE_HARD = 2;

    // Khai báo các nút
    private JButton btEasy;
    private JButton btMedium;
    private JButton btHard;
    // private JButton btSetting; // <<< BỎ ĐI >>>
    private JButton btQuit;

    // Listener
    private OnClickMenuListener onClickMenuListener;

    public MenuView(String backgroundPath) {
        super(backgroundPath);
        // setVisible(false); // Bỏ dòng này, việc hiển thị/ẩn do CardLayout quản lý
        initUI();
    }

    private void initUI() {
        // Khởi tạo các nút
        btEasy = new JButton();
        btMedium = new JButton();
        btHard = new JButton();
        // btSetting = new JButton(); // <<< BỎ ĐI >>>
        btQuit = new JButton();

        // Cấu hình nút Easy
        btEasy.setText(BT_EASY); // Giả sử BT_EASY được định nghĩa trong Utils
        btEasy.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)); // Đổi thành HAND_CURSOR
        btEasy.addActionListener(this);

        // Cấu hình nút Medium
        btMedium.setText(BT_MEDIUM); // Giả sử BT_MEDIUM được định nghĩa trong Utils
        btMedium.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btMedium.addActionListener(this);

        // Cấu hình nút Hard
        btHard.setText(BT_HARD); // Giả sử BT_HARD được định nghĩa trong Utils
        btHard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btHard.addActionListener(this);

        // Cấu hình nút Auto Play <<< THAY ĐỔI >>>


        // Cấu hình nút Quit
        btQuit.setText(BT_QUIT); // Giả sử BT_QUIT được định nghĩa trong Utils
        btQuit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btQuit.addActionListener(this);

        // Thiết lập Layout
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(188, 188, 188) // Điều chỉnh khoảng cách lề trái nếu cần
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false) // Thêm false để các nút có thể co giãn khác nhau nếu muốn
                                        // Các nút theo thứ tự từ trên xuống
                                        .addComponent(btEasy, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)     // Đặt kích thước cố định hoặc Preferred
                                        .addComponent(btMedium, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                                        .addComponent(btHard, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                                        .addComponent(btQuit, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
                                .addContainerGap(196, Short.MAX_VALUE)) // Điều chỉnh khoảng cách lề phải nếu cần
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(77, Short.MAX_VALUE) // Khoảng cách từ trên xuống
                                .addComponent(btEasy, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE) // Chiều cao nút
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED) // Khoảng cách giữa các nút
                                .addComponent(btMedium, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btHard, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGap(18, 18, 18) // Khoảng cách lớn hơn trước nút Quit
                                .addComponent(btQuit, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                                .addGap(69, 69, 69)) // Khoảng cách từ dưới lên
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Kiểm tra listener có tồn tại không trước khi gọi
        if (onClickMenuListener == null) {
            System.err.println("Warning: onClickMenuListener is null in MenuView.");
            return;
        }

        // Xử lý sự kiện dựa trên Action Command (là nội dung text của nút)
        switch (e.getActionCommand()){
            case BT_EASY:
                onClickMenuListener.onNewGameClicked(TYPE_EASY);
                break;
            case BT_MEDIUM:
                onClickMenuListener.onNewGameClicked(TYPE_MEDIUM);
                break;
            case BT_HARD:
                onClickMenuListener.onNewGameClicked(TYPE_HARD);
                break;
            // <<< THAY ĐỔI CASE VÀ LỜI GỌI >>>
            case BT_AUTO_PLAY: // Sử dụng hằng số mới
                onClickMenuListener.onAutoPlayClicked(); // Gọi phương thức listener mới
                break;
            case BT_QUIT:
                onClickMenuListener.onQuitClicked();
                break;
            default:
                // Không làm gì nếu không khớp lệnh nào
                System.out.println("MenuView: Unhandled action command: " + e.getActionCommand());
                break;
        }
    }

    /**
     * Interface định nghĩa các hành động có thể xảy ra trên Menu View.
     * Lớp Controller sẽ implement interface này.
     */
    public interface OnClickMenuListener{
        /** Wird aufgerufen, wenn ein neues Spiel gestartet wird (Easy, Medium, Hard). */
        void onNewGameClicked(int type);
        /** Wird aufgerufen, wenn der Auto Play Button geklickt wird. <<< THAY ĐỔI >>> */
        void onAutoPlayClicked();
        /** Wird aufgerufen, wenn der Quit Button geklickt wird. */
        void onQuitClicked();
        // void onSettingClicked(); // <<< BỎ ĐI >>>
    }

    /**
     * Đăng ký listener để nhận sự kiện từ Menu View.
     * @param onClickMenuListener Đối tượng (thường là GameController) implement OnClickMenuListener.
     */
    public void setOnClickMenuListener(OnClickMenuListener onClickMenuListener) {
        this.onClickMenuListener = onClickMenuListener;
    }
}