package com.haotian.main;

import com.haotian.chess.Chess;
import com.haotian.chess.ChessFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Stack;

public class MainFrame extends JFrame implements Runnable, ActionListener, MouseListener {
    // 存放棋子对象的数组
    private Chess[][] chess = new Chess[9][10];
    // 选中特效图片
    private JLabel selectedEffect;
    // 组件容器
    private final Container con;
    // 选中状态
    // 没有选中棋子：false， 选中了棋子：true
    private boolean isSelected = false;
    // 记录上一次选中的棋子格子坐标
    // 用于移动和吃子的逻辑实现
    // 选中的棋子对象表示为chess[lastX][lastY]
    private int lastX = -1;
    private int lastY = -1;
    // 选中特效 选中框闪烁线程
    private Thread flashThread = null;
    private boolean isFlashing = false;
    // 现在的下棋者的阵营
    // 0:红方 1：黑方
    private int curPlayer = 0;
    private JLabel information;
    // 用于实现悔棋的历史记录
    private final Stack<History> withdrawStack = new Stack<>();
    // 存储双方将军的位置
    // 用于王不见王判定
    // 每次将移动时更新
    private int rKingX = 4;
    private int rKingY = 9;
    private int bKingX = 4;
    private int bKingY = 0;

    public MainFrame() {
        con = this.getContentPane();
        con.setLayout(null);
        this.setTitle("中国象棋");
        // 关闭JFrame的同时退出程序
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 先计算窗体的边框大小，并根据边框大小和棋盘大小计算窗体的大小
//        this.setSize(532, 615);
        this.setSize(750, 615);
        // 窗体居中
        this.setLocationRelativeTo(null);
        // 防止用户调整窗体大小
        this.setResizable(false);
        // 显示窗体
        this.setVisible(true);
        init();
        addMouseListener( this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "withdraw" -> withdraw();
            case "save" -> save();
            case "import" -> importX();
            case "stalemate" -> System.out.println("求和");
            case "giveUp" -> System.out.println("认输");
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // e.getX()和e.getY()是相对于MainFrame的坐标
        // 而棋子和棋盘是相对于容器con的坐标
        // 所以需要减去标题栏和左侧边框的厚度
        int mouseX = calGridX(e.getX()-6);
        int mouseY = calGridY(e.getY()-30);
        // 判断点击的坐标是否越界，
        if (!(mouseX < 0 || mouseX > 8 || mouseY < 0 || mouseY > 9)) {
            // 第一次选择棋子
            if (!isSelected) {
                firstSelect(mouseX, mouseY);
            } else {
                // 第n次选择棋子 实现吃子、重新选子和移动逻辑
                // 如果点击的格子上有棋子
                if (chess[mouseX][mouseY] != null) {
                    // 判断棋子阵营
                    // 如果颜色相同
                    if (chess[lastX][lastY] != null && chess[lastX][lastY].getColor().equals(chess[mouseX][mouseY].getColor())) {
                        reSelect(mouseX, mouseY);
                    } else {
                        // 如果第一次选中的棋子到第n次选择棋子位置的移动合法
                        if (chess[lastX][lastY].isLegalMove(mouseX, mouseY, this)) {
                            // 实现吃子
                            if (0 <= lastX && lastX <= 8 && 0 <= lastY && lastY <= 9) {
                                eatChess(mouseX, mouseY);

                                // 在切换玩家前检测我方是否触发王不见王和“被”将军
                                // 也就是说，切换玩家前后我方都是可以将军对方的
                                // 如果会触发将帅碰面或者被将军，则重新落子
                                if(isKingFaceKing() || isCheck()){
                                    withdraw();
                                }
                                endTurn();
                                if(isCheckmate()){
                                    System.out.println("绝杀无解");
                                }
                            }
                        }
                    }
                } else {
                    moveChess(mouseX, mouseY);
                    // 在切换玩家前检测我方是否触发王不见王和“被”将军
                    // 也就是说，切换玩家前后我方都是可以将军对方的
                    // 如果会触发将帅碰面或者被将军，则重新落子
                    if(isKingFaceKing() || isCheck()){
                        withdraw();
                    }
                    endTurn();
                    if(isCheckmate()){
                        System.out.println("绝杀无解");
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void run() {
        // 闪烁间隔为300毫秒
        long flashInterval = 300;
        while(isSelected) {
            try {
                // 切换选中框的可见性
                SwingUtilities.invokeLater(() -> selectedEffect.setVisible(!selectedEffect.isVisible()));
                Thread.sleep(flashInterval);
            } catch (InterruptedException e) {
                // 线程被中断时退出循环
                Thread.currentThread().interrupt(); // 恢复中断状态
                break;
            }
        }
        // 确保最终选中框是隐藏的
        SwingUtilities.invokeLater(() -> selectedEffect.setVisible(false));
    }


    private void init() {
        // 先初始化的渲染在上层，后初始化的渲染在下层
        // 下层的会被上层遮挡住
        // 初始化选中特效
        initEffect();
        // 初始化棋子
        initChess();
        // 初始化棋盘
        initBoard();
        // 初始化交互界面
        initToolBar();
    }

    private void initChess(){
        String[] chessName = {
                "b_che", "b_ma", "b_xiang", "b_shi", "b_jiang", "b_shi", "b_xiang", "b_ma", "b_che",
                "b_pao", "b_pao",
                "b_zu", "b_zu", "b_zu", "b_zu", "b_zu",
                "r_zu", "r_zu", "r_zu", "r_zu", "r_zu",
                "r_pao","r_pao",
                "r_che", "r_ma", "r_xiang", "r_shi", "r_jiang", "r_shi", "r_xiang", "r_ma", "r_che"};
        int[] indexX = {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                1, 7,
                0, 2, 4, 6, 8,
                0, 2, 4, 6, 8,
                1, 7,
                0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] indexY = {
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                2, 2,
                3, 3, 3, 3, 3,
                6, 6, 6, 6, 6,
                7, 7,
                9, 9, 9, 9, 9, 9, 9, 9, 9};

        for (int i = 0; i < chessName.length; i++) {
            chess[indexX[i]][indexY[i]] = ChessFactory.CreateChess(chessName[i], calPosX( indexX[i]), calPosY( indexY[i]));
            if(chess[indexX[i]][indexY[i]]!=null){
                con.add(chess[indexX[i]][indexY[i]]);
            }
        }
    }

    private void initBoard(){
        // 棋盘图片
        JLabel boardImg;
        // 绘制棋盘
        con.add(boardImg = new JLabel(new ImageIcon("res/img/board.png")));
        boardImg.setBounds(0, 0, 520, 580);
    }

    private void initEffect(){
        con.add(selectedEffect = new JLabel(new ImageIcon("res/img/box.png")));
        selectedEffect.setBounds(calPosX( lastX), calPosY(lastY), 60, 60);
        // 没有选中棋子时，隐藏选中特效
        selectedEffect.setVisible(isSelected);
    }

    private void initToolBar(){
        // 交互界面
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new GridLayout(6, 1));
        information = new JLabel("当前玩家：" + (curPlayer == 0 ? "红方" : "黑方"));
        JButton withdrawBtn = new JButton("悔棋");
        withdrawBtn.addActionListener(this);
        JButton saveBtn = new JButton("保存存档");
        saveBtn.addActionListener(this);
        JButton importBtn = new JButton("导入存档");
        importBtn.addActionListener(this);
        JButton stalemateBtn = new JButton("求和");
        stalemateBtn.addActionListener(this);
        JButton giveUpBtn = new JButton("认输");
        giveUpBtn.addActionListener(this);
        // 为每个按钮设置标识符
        withdrawBtn.setActionCommand("withdraw");
        saveBtn.setActionCommand("save");
        importBtn.setActionCommand("import");
        stalemateBtn.setActionCommand("stalemate");
        giveUpBtn.setActionCommand("giveUp");
        toolBar.add(information);
        toolBar.add(withdrawBtn);
        toolBar.add(saveBtn);
        toolBar.add(importBtn);
        toolBar.add(stalemateBtn);
        toolBar.add(giveUpBtn);
        toolBar.setBounds(520, -20, 218, 615);
        con.add(toolBar);
    }

    // 根据棋子的网格坐标得到像素坐标
    public static int calPosX(int x){
        // 棋盘边距为3，棋子之间的间距为3
        // 例如黑马的网格坐标为(1,0)
        // 则其X轴像素坐标 = 棋盘边距 + 一整颗棋子的像素 + 棋子之间的间距*1（只有黑车与黑马的间距）
        return 3+x*58;
    }

    public static int calPosY(int y){
        return 3+y*58;
    }


    // 根据棋子的像素坐标得到网格坐标
    public static int calGridX(int x){
        return (x-3)/58;
    }
    public static int calGridY(int y){
        return (y-3)/58;
    }


    private void startFlashing() {
        if (!isFlashing) {
            isFlashing = true;
            selectedEffect.setVisible(true); // 确保选中框可见
            flashThread = new Thread(this);
            flashThread.start();
        }
    }

    private void stopFlashing() {
        isFlashing = false;
        if (flashThread != null) {
            try {
                flashThread.join(); // 等待闪烁线程结束
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        selectedEffect.setVisible(false); // 隐藏选中框
    }

    // 吃完子或者移动结束后回合结束
    private void endTurn(){
        // 回合结束切换玩家
        curPlayer = curPlayer == 0 ? 1 : 0 ;
        // 取消选中状态
        isSelected = false;
        // 结束闪烁特效
        stopFlashing();
        // 更新显示信息
        information.setText("当前玩家：" + (curPlayer == 0 ? "红" : "黑"));
    }


    private void firstSelect(int mouseX, int mouseY) {
        // 如果点击的格子上有棋子且该棋子颜色与当前玩家阵营相同
        if (chess[mouseX][mouseY] != null && chess[mouseX][mouseY].getColor().equals(curPlayer == 0 ? "r" : "b")) {
            // 选择成功，切换选中状态
            isSelected = true;
            lastX = mouseX;
            lastY = mouseY;
            // 设置选中特效的位置并开启闪烁线程
            selectedEffect.setLocation(calPosX(mouseX), calPosY(mouseY));
            startFlashing();
        }
    }

    private void reSelect(int mouseX, int mouseY){
        // 重新选子
        // 重新选子后更新选中的格子坐标
        lastX = mouseX;
        lastY = mouseY;
        selectedEffect.setLocation(calPosX(mouseX), calPosY(mouseY));
    }

    private void moveChess(int mouseX, int mouseY) {
        // 如果第一次选中棋子到第n次选中的空格子的位置的移动合法
        if (chess[lastX][lastY].isLegalMove(mouseX, mouseY, this)) {
            // 判断移动的棋子是否是将
            if (chess[lastX][lastY].getSort().equals("jiang")) {
                // 更新将军坐标
                if (chess[lastX][lastY].getColor().equals("r")) {
                    rKingX = mouseX;
                    rKingY = mouseY;
                }else{
                    bKingX = mouseX;
                    bKingY = mouseY;
                }
            }
            // 实现移动棋子
            // 动画实现
            chess[lastX][lastY].setLocation(calPosX(mouseX), calPosY(mouseY));
            // 历史记录
            History history = new History(chess[lastX][lastY], lastX, lastY, mouseX, mouseY);
            withdrawStack.push(history);
            // 逻辑实现
            chess[mouseX][mouseY] = chess[lastX][lastY];
            // 第1次选择的棋子的位置为空
            chess[lastX][lastY] = null;
            // 移动后，取消选中状态
            lastX = -1;
            lastY = -1;
        }
    }

    private void eatChess(int mouseX, int mouseY) {
        // 动画实现
        chess[lastX][lastY].setLocation(calPosX(mouseX), calPosY(mouseY));
        chess[mouseX][mouseY].setVisible(false);
        // 历史记录
        History history = new History(chess[lastX][lastY], lastX, lastY, mouseX, mouseY);
        history.setEatenChess(chess[mouseX][mouseY]);
        withdrawStack.push(history);
        // 逻辑实现
        // 第n次选择的位置上的棋子引用指向第1次选择的棋子
        chess[mouseX][mouseY] = chess[lastX][lastY];
        // 第1次选择的棋子的位置为空
        chess[lastX][lastY] = null;
        // 选中棋子的坐标恢复初始值
        lastX = -1;
        lastY = -1;
    }

    // 用于悔棋按钮实现
    // 以及吃子或者移动后触发王不见王和将军时重新落子的实现
    private void withdraw(){
        // 对局刚开始栈为空无法悔棋
        if(withdrawStack.isEmpty()){
            return;
        }
        History history = withdrawStack.pop();
        // 如果移动的棋子是将
        if(history.getTargetChess().getSort().equals("jiang")){
            if(history.getTargetChess().getColor().equals("r")){
                rKingX = history.getPreX();
                rKingY = history.getPreY();
            }else{
                bKingX = history.getPreX();
                bKingY = history.getPreY();
            }
        }
        // 动画悔棋实现
        chess[history.getPostX()][history.getPostY()].setLocation(calPosX(history.getPreX()), calPosY(history.getPreY()));
        // 逻辑悔棋实现
        // 将操作棋子的坐标修改回去
        chess[history.getPreX()][history.getPreY()] = chess[history.getPostX()][history.getPostY()];
        // 判断是吃子还是移动
        if (history.getEatenChess() != null) {
            // 恢复吃掉的棋子
            chess[history.getPostX()][history.getPostY()] = history.getEatenChess();
            history.getEatenChess().setVisible(true);
        }else{
            // 将移动后的位置重新置为空
            chess[history.getPostX()][history.getPostY()] = null;
        }
        endTurn();
    }

    private void save(){
        // 文件选择器,用于选择文件保存路径
        JFileChooser chooser = new JFileChooser();
        // 选择器模式设定为只能选择文件夹的模式（不允许选择文件）
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // 显示选择器的对话框
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION){
            return;
        }
        // 获取选择的文件（这里是文件夹）
        File directory = chooser.getSelectedFile();
        // 在选择的文件夹下创建一个文件，用于保存存档信息
        // 文件名格式为：当前时间戳.txt
        String path = directory.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".ser";
        File file = new File(path);
        // 如果文件不存在，则创建
        if( !file.exists()){
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    System.out.println("文件创建失败");
                } else {
                    System.out.println("文件创建成功");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // 创建文件输出流对象
        FileOutputStream fos;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(chess);
            oos.writeInt(curPlayer);
            oos.writeInt(rKingX);
            oos.writeInt(rKingY);
            oos.writeInt(bKingX);
            oos.writeInt(bKingY);
        }catch (IOException ex){
            System.out.println("保存存档文件时发生错误");
        }finally {
            if(oos != null){
                try {
                    oos.close();
                } catch (IOException ex) {
                    System.out.println("保存存档文件时发生错误");
                }
            }
        }
    }

    // import关键字被占用
    // 命名为importX，无实际意义
    private void importX(){
        // 文件选择器,用于选择导入文件的路径
        JFileChooser chooser = new JFileChooser();
        // 选择器模式设定为只能选择文件的模式（不允许选择文件夹）
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 显示选择器的对话框
        chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        FileInputStream fis;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            chess = (Chess[][]) ois.readObject();
            curPlayer = ois.readInt();
            rKingX = ois.readInt();
            rKingY = ois.readInt();
            bKingX = ois.readInt();
            bKingY = ois.readInt();
            update();
        }catch (StreamCorruptedException ex){
            System.out.println("存档文件已损坏");
        } catch (IOException ex){
            System.out.println("读取存档文件时发生错误");
        } catch (ClassNotFoundException e) {
            System.out.println("存档文件中包含未知的类类型");
        }finally {
            if(ois != null){
                try {
                    ois.close();
                } catch (IOException ex) {
                    System.out.println("读取存档文件时发生错误");
                }
            }
        }
    }

    private void update(){
        // 移除所有组件
        con.removeAll();
        initEffect();
        // 根据chess[][]数组初始化棋子，也是更新的主体，其他组件不变
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 10; j++){
                if(chess[i][j] != null){
                    con.add(chess[i][j]);
                }
            }
        }
        initBoard();
        initToolBar();
    }

    // 王不见王
    // 判断黑将和红将是否碰面
    private boolean isKingFaceKing(){
        // 1、判断双方将军是否在一条直线上
        if(rKingX == bKingX){
            // 2、判断直线内是否有其他棋子
            for(int i = bKingY+1; i < rKingY; i++){
                if (chess[rKingX][i] != null){
                    return false;
                }
            }
        }else{
            return false;
        }
        return true;
    }

    // 将军状态判断
    // 移动或者吃子后，己方将军会被对方吃掉（将军）则撤回行动重新落子
    private boolean isCheck(){
        // 如果当前玩家是红方
        if(curPlayer == 0){
            for(int i = 0; i < 9; i++){
                for(int j = 0; j < 10; j++){
                    // 检测黑方所有棋子能否将到我方
                    if(chess[i][j] != null && chess[i][j].getColor().equals("b")){
                        if(chess[i][j].isLegalMove(rKingX, rKingY, this)){
                            return true;
                        }
                    }
                }
            }
        }else{
            for(int i = 0; i < 9; i++){
                for(int j = 0; j < 10; j++){
                    // 检测红方所有棋子能否将到我方
                    if(chess[i][j] != null && chess[i][j].getColor().equals("r")){
                        if(chess[i][j].isLegalMove(bKingX, bKingY, this)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // 绝杀判定
    private boolean isCheckmate(){
        boolean isStillCheck = true;
        // 如果回合结束不存在将军状态
        // 则不可能是绝杀状态
        if(!isCheck()){
            return false;
        }
        // 遍历己方所有棋子
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 10; j++){
                if(chess[i][j] != null && chess[i][j].getColor().equals(curPlayer == 0 ? "r" : "b")){
                    // 遍历该棋子所有可能的移动
                    for (int k = 0; k < 9; k++){
                        for (int l = 0; l < 10; l++){
                            // 如果该格子是空或者是对方的棋子
                            if((chess[k][l] == null || chess[k][l].getColor().equals(curPlayer == 0 ? "b" : "r"))
                                    && chess[i][j].isLegalMove(k, l, this)){
                                // 如果模拟移动的棋子是将
                                // 还需模拟更新将的位置
                                if(chess[i][j].getSort().equals("jiang")){
                                    if(chess[i][j].getColor().equals("r")){
                                        rKingX = k;
                                        rKingY = l;
                                        isStillCheck =simulateMove(i, j, k, l);
                                        // 恢复红将的位置
                                        rKingX = i;
                                        rKingY = j;
                                    }else {
                                        bKingX = k;
                                        bKingY = l;
                                        isStillCheck =simulateMove(i, j, k, l);
                                        // 恢复黑将的位置
                                        bKingX = i;
                                        bKingY = j;
                                    }
                                }else{
                                    isStillCheck =simulateMove(i, j, k, l);
                                }

                                // 如果执行完棋子移动或吃子操作后，没有将军状态，则不是绝杀状态
                                if(!isStillCheck){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    // 模拟移动
    // 用于穷举移动方式，实现绝杀判定
    private boolean simulateMove(int i, int j, int k, int l){
        // 只是逻辑移动，不用更改动画
        Chess tempChess = chess[k][l];
        chess[k][l] = chess[i][j];
        chess[i][j] = null;
        // 检测是否仍然被将军
        boolean isStillCheck = isCheck();
        // 撤回
        chess[i][j] = chess[k][l];
        chess[k][l] = tempChess;
        return isStillCheck;
    }

    public Chess[][] getChess() {
        return chess;
    }
    public void setChess(Chess[][] chess) {
        this.chess = chess;
    }
}


