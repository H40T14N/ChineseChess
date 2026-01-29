package com.haotian.chess;

import com.haotian.main.MainFrame;

public class Pao extends Chess{
    public Pao(){
    }
    public Pao(String name, int x, int y) {
        super(name, x, y);
    }
    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        // 判断炮正在吃子还是移动
        // 如果目标格子上没有棋子，则属于移动，移动判断跟车一样
        if(mainFrame.getChess()[x][y] == null){
            return (line(x,y) == 1 || line(x,y) == 2) && countChess(x,y,mainFrame)==0;
        }else{
            // 如果目标格子上有棋子，则属于吃子,到目标格子上有且仅有一个棋子作为跑架子
            return (line(x,y) == 1 || line(x,y) == 2) && countChess(x,y,mainFrame)==1;
        }
    }
}
