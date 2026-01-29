package com.haotian.chess;

import com.haotian.main.MainFrame;

import javax.swing.*;
import java.io.Serializable;

import static com.haotian.main.MainFrame.calGridX;
import static com.haotian.main.MainFrame.calGridY;

public abstract class Chess extends JLabel implements Serializable {
    private String name;
    // 棋子的颜色
    private String color;
    // 棋子的兵种
    private String sort;
    // 棋子的初始纵坐标
    // 用于后续判断是否过河
    private int initY;

    public Chess(){}
    public Chess(String name, int x, int y) {
        // 用Jlabel自带的构造函数显示棋子图像
        super( new ImageIcon("res/img/"+ name +".png"));
        // 棋子大小为55*55
        this.setBounds(x, y, 55, 55 );
        this.name = name;
        this.color = name.substring(0,1);
        this.sort = name.substring(2);
        this.initY = y;
    }

    // 判断走法是否合法
    public abstract boolean isLegalMove(int x, int y, MainFrame mainFrame);

    // 用于判断是否在皇宫内移动
    // 作用对象： 将、士
    public boolean inHome(int x, int y){
        // 如果横坐标超出了皇宫
        // 移动不合法
        if(x<=2 || x>=6){
            return false ;
        }else{
            // 如果在楚河汉界之上
            if(isUp()){
                // 纵坐标不能超过2（皇宫）
                return y <= 2;
            }else {
                // 楚河汉界之下同理
                // 纵坐标不能小于7
                return y >= 7;
            }
        }
    }

    // 判断棋子在楚河汉界上方还是下方
    // 作用对象： 将、士、象、兵
    // true: 楚河汉界之上 false: 楚河汉界之下
    public boolean isUp(){
        // 如果初始纵坐标在0-4之间
        // 即在楚河汉界之上
        // 否则楚河汉界之下
        return calGridY(initY) <= 4;
    }

    // 移动方式分类
    // 0: 非法移动
    // 1：竖着走直线 2：横着走直线 3：走斜线
    // 4、5：走（正/倒）日字（本质还是斜线）
    public int line(int x, int y){
        // x轴没动，即走竖直线
        if(x==calGridX(this.getLocation().x)){
            return 1;
        }else if(y==calGridY(this.getLocation().y)){
            // y轴没动，即走竖直线
            return 2;
        }else if(Math.abs(x - calGridX(this.getLocation().x)) == Math.abs(y - calGridY(this.getLocation().y))){
            // 横纵坐标差值的绝对值相等，即走斜线
            return 3;
        }else if(Math.abs(x - calGridX(this.getLocation().x))==1 && Math.abs(y - calGridY(this.getLocation().y))==2){
            // 走正日字
            return 4;
        }else if(Math.abs(x - calGridX(this.getLocation().x))==2 && Math.abs(y - calGridY(this.getLocation().y))==1){
            // 走倒日字
            return 5;
        }
        return 0;
    }

    // 判断是否蹩脚
    // 作用对象：象、马
    public boolean isBiejiao(int x, int y, MainFrame mainFrame){
        // 蹩脚棋子的格子坐标
        int targetX, targetY;
        // 如果判断对象是象
        if("xiang".equals( sort)){
            targetX = (x + calGridX(this.getLocation().x)) / 2;
            targetY = (y + calGridY(this.getLocation().y)) / 2;
            return mainFrame.getChess()[targetX][targetY] != null;
        } else if("ma".equals(sort)){
            // 如果判断对象是马
            if(line(x, y)==4){
                // 如果马走的是正日字
                targetX = calGridX(this.getLocation().x);
                targetY = (y + calGridY(this.getLocation().y)) / 2;
                return mainFrame.getChess()[targetX][targetY] != null;
            }else if(line(x, y)==5){
                // 如果马走是倒日字
                targetX = (x + calGridX(this.getLocation().x)) / 2;
                targetY = calGridY(this.getLocation().y);
                return mainFrame.getChess()[targetX][targetY] != null;
            }
        }
        // 其他对象不会被蹩脚
        return false;
    }

    // 用于计算选中棋子到目标点这段路程中的棋子数
    // 不算自己和目标点上的棋子
    // 作用对象： 车、炮。
    public int countChess(int x, int y, MainFrame mainFrame){
        int count = 0;
        int start = 0;
        int end = 0;
        // 竖着走直线
        if(line(x, y)==1){
            // 从下往上走
            if(y<calGridY(this.getLocation().y)){
                // 不算目标格
                start = y+1;
                end = calGridY(this.getLocation().y);
            }else{
                // 等于的情况会进入重新选择逻辑，这里不考虑
                // 从上往下走
                start = calGridY(this.getLocation().y);
                end = y-1;
            }
            for(; start<=end; start++){
                if(mainFrame.getChess()[x][start] != null){
                    count++;
                }
            }

        }else if (line(x, y)==2){
            // 横着走直线
            // 从左往右走
            if(calGridX(this.getLocation().x) < x){
                start = calGridX(this.getLocation().x);
                end = x-1;
            }else {
                // 从右往左走
                start = x+1;
                end = calGridX(this.getLocation().x);
            }
            for(; start <= end; start++){
                if(mainFrame.getChess()[start][y] != null){
                    count++;
                }
            }
        }
        // 不算自己，所以要减1
        return count-1;
    }

    // 作用对象： 象
    public boolean isOverRiver(int y){
        if(isUp()){
            // 纵坐标超过4就过河
            return y > 4;
        }else{
            // 纵坐标小于5就过河
            return y < 5;
        }
    }

    // 根据移动方式获取走的距离（多少格）
    public int getDistance(int x, int y){
        // 竖着走直线
        if(line(x, y)==1){
            return Math.abs(calGridY(this.getLocation().y) - y);
        } else if(line(x, y)==2 || line(x, y)==3){
            // 横着走直线或者走斜线
            return Math.abs(calGridX(this.getLocation().x) - x);
        }
        return 0;
    }

    // 判断是否是前进
    // 作用对象：兵、卒
    public boolean isForward(int y){
        // 如果在楚河汉界之上
        if(isUp()){
            // 则只能向下走或者左右移动
            return y >= calGridY(this.getLocation().y);
        }else{
            // 否则只能向上走或者左右移动
            return y <= calGridY(this.getLocation().y);
        }
    }



    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString(){
    	return this.name;
    }


    public String getColor() {
        return color;
    }

    public String getSort() {
        return sort;
    }

    public int getInitY() {
        return initY;
    }

}
