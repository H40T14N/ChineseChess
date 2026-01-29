package com.haotian.chess;

import com.haotian.main.MainFrame;

public class Xiang extends Chess{
    public Xiang(){
    }
    public Xiang(String name, int x, int y){
        super(name, x, y);
    }
    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        //     1、没有过河           2、斜着走          3、斜着走的距离为2            4、没有被蹩脚
        return !isOverRiver(y) && line(x,y) == 3 && getDistance(x,y) == 2 && !isBiejiao(x,y,mainFrame);
    }
}
