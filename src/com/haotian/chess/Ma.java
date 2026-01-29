package com.haotian.chess;

import com.haotian.main.MainFrame;

public class Ma extends Chess{
    public Ma(){
    }
    public Ma(String name, int x, int y) {
        super(name, x, y);
    }
    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        // 1、走日字                                2、没有被蹩脚
        return (line(x,y)==4 || line(x,y)==5) && !isBiejiao(x,y,mainFrame);
    }
}
