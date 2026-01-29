package com.haotian.chess;

import com.haotian.main.MainFrame;

import static com.haotian.main.MainFrame.calGridY;

public class Zu extends Chess{
    public Zu(){
    }
    public Zu(String name, int x, int y) {
        super(name, x, y);
    }
    @Override
    public boolean isLegalMove(int x, int y, MainFrame mainFrame) {
        // 如果已经过河
        // 与象不同，卒过没过河是看自身的纵坐标而不是目标点的纵坐标
        if(isOverRiver( calGridY(this.getLocation().y))){
            return (line(x,y)==1 || line(x,y)==2) && getDistance(x,y) == 1 && isForward(y);
        }else {
            return line(x,y)==1 && getDistance(x,y) == 1 && isForward(y);
        }
    }
}
