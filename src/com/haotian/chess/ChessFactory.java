package com.haotian.chess;

// 简单工厂模式
public class ChessFactory {
    // 私有构造函数, 不允许实例化
    private ChessFactory() {
    }

    public static Chess CreateChess(String name, int x, int y) {
        switch (name.substring(2)) {
            case "che" -> {
                return new Che(name, x, y);
            }
            case "ma" -> {
                return new Ma(name, x, y);
            }
            case "xiang" -> {
                return new Xiang(name, x, y);
            }
            case "shi" -> {
                return new Shi(name, x, y);
            }
            case "jiang" -> {
                return new Jiang(name, x, y);
            }
            case "pao" -> {
                return new Pao(name, x, y);
            }
            case "zu" -> {
                return new Zu(name, x, y);
            }
            default -> {
                return null;
            }
        }
    }
}
