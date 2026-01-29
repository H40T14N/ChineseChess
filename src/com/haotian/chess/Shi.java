package com.haotian.chess;

import com.haotian.main.MainFrame;

public class Shi extends Chess{
    public Shi(){
    }
    public Shi(String name, int x, int y) {
        super(name, x, y);
    }
    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        // 1、判断是否在皇宫内   2、斜线移动         3、判断移动距离是否为1
        return inHome(x,y) && line(x,y) == 3 && getDistance(x,y) == 1;
    }
}
