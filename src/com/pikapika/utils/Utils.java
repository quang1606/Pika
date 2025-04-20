package com.pikapika.utils;


public class Utils {
    public static final int WINDOW_WIDTH = 720;
    public static final int WINDOW_HEIGHT = 460;
    public static final String DEFAULT_FONT = "Shree Devanagari 714";
    public static final String BT_EASY = "Easy";
    public static final String BT_MEDIUM = "Medium";
    public static final String BT_HARD = "Hard";
    public static final String BT_SETTING = "Setting";
    public static final String BT_QUIT = "Quit Game";
    public static final String BT_RESUM = "resum";
    public static final String BT_REPLAY = "replay";
    public static final String BT_PAUSE = "pause";
    public static final String BT_CONTINUE = "Continue";
    public static final String BT_BACK_MENU = "Back Menu";
    public static final int MAP_ROW = 8;
    public static final int MAP_COL = 12;
    public static final int PIKACHU_NUMBER = 34;
    public static final boolean DEBUG = true;
    // Ví dụ kích thước cho độ khó Easy
    public static final int EASY_ROWS = 6;  // Ví dụ: 6 hàng Pikachu hiển thị + 2 hàng viền
    public static final int EASY_COLS = 8; // Ví dụ: 8 cột Pikachu hiển thị + 2 cột viền

    // Ví dụ kích thước cho độ khó Medium (Có thể bằng MAP_ROW/MAP_COL cũ nếu muốn)
    public static final int MEDIUM_ROWS = 8; // Ví dụ: 8 hàng Pikachu hiển thị + 2 hàng viền
    public static final int MEDIUM_COLS = 10; // Ví dụ: 10 cột Pikachu hiển thị + 2 cột viền

    // Ví dụ kích thước cho độ khó Hard
    public static final int HARD_ROWS = 10; // Ví dụ: 10 hàng Pikachu hiển thị + 2 hàng viền
    public static final int HARD_COLS = 12; // Ví dụ: 14 cột Pikachu hiển thị + 2 cột viền


    // Trong file com/pikapika/utils/Utils.java
    public static final String BT_AUTO_PLAY = "Tự Động Chơi"; // Hoặc "Auto Play"
    public static void debug(Class clz,String debug){
        if (DEBUG){
            debug = debug == null ? "Null debug string!" : debug;
            String name = clz.getCanonicalName()==null?"Debug": clz.getCanonicalName();
            System.out.println(name+":"+debug);
        }
    }
}
