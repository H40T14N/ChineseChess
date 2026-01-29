package com.haotian.chess;

import com.haotian.main.MainFrame;

public class Jiang extends Chess{
    public Jiang(){
    }
    public Jiang(String name, int x, int y){
        super(name, x, y);
    }
    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        // 1、判断是否在皇宫内   2、判断移动方式是否是直线移动（1或2）   3、判断移动距离是否为1
        return inHome(x,y) && 0 < line(x,y) && line(x,y) < 3 && getDistance(x,y) == 1;
    }
}
