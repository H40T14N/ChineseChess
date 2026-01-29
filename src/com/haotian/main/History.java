package com.haotian.main;

import com.haotian.chess.Chess;

// 记录每回合的棋子移动信息
// 用于悔棋功能实现
public class History {
    private Chess targetChess;
    // 移动前的坐标
    private int preX;
    private int preY;
    // 移动后的坐标
    private int postX;
    private int postY;
    // 被吃掉的
    private Chess eatenChess;

    public History() {

    }

    public History(Chess targetChess, int preX, int preY, int postX, int postY) {
        this.targetChess = targetChess;
        this.preX = preX;
        this.preY = preY;
        this.postX = postX;
        this.postY = postY;
    }

    public Chess getTargetChess() {
        return targetChess;
    }

    public void setTargetChess(Chess targetChess) {
        this.targetChess = targetChess;
    }

    public int getPreX() {
        return preX;
    }

    public void setPreX(int preX) {
        this.preX = preX;
    }

    public int getPreY() {
        return preY;
    }

    public void setPreY(int preY) {
        this.preY = preY;
    }

    public int getPostX() {
        return postX;
    }

    public void setPostX(int postX) {
        this.postX = postX;
    }

    public int getPostY() {
        return postY;
    }

    public void setPostY(int postY) {
        this.postY = postY;
    }

    public Chess getEatenChess() {
        return eatenChess;
    }

    public void setEatenChess(Chess eatenChess) {
        this.eatenChess = eatenChess;
    }

    public String toString() {
        if (eatenChess != null) {
            return targetChess.getName() + "从(" + preX + "," + preY +
                    ")吃掉了在(" + postX + "," + postY + ")的" + eatenChess.getName();
        }
    	return targetChess.getName() + "从(" + preX + "," + preY + ")移动到了(" + postX + "," + postY + ")";
    }

}
