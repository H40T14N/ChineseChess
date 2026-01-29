package com.haotian.chess;

import com.haotian.main.MainFrame;

public class Che extends Chess{
    public Che(){
    }
    public Che(String name, int x, int y) {
        super(name, x, y);
    }

    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        // 如果兵种是车
        // 1、走直线（横着竖着都可以）                    2、到目标格子之间没有棋子
        return (line(x,y) == 1 || line(x,y) == 2) && countChess(x,y,mainFrame)==0;
    }
}
