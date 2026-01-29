# ChineseChess — 象棋学JavaSE
![项目演示](res\readme\instance_01.png)
JavaSE知识点繁多，跟着网课敲一遍代码理解不深，很容易第二天就忘记。为了巩固本人的JavaSE，创建了这个中国象棋的项目，一是全面复习一遍这些知识点，二是拓深我对一些抽象的知识点的理解（比如多态），三是希望能够帮助到同为Java初学者的你。<br>
项目基于Java swing，但学习的重点不在Java swing，而且它实在是难用。所以你在复刻的时候如果有关于Java swing方面的疑惑，大可跳过，不予深究。<br>
以下是我对项目中用到的知识点的解析。
## 类（封装、 继承、 多态）
### 封装
中国象棋，很容易想到用一个Chess类封装，然后创建车马炮等子类来继承Chess。
``` Java
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
        public String getColor() {
        return color;
    }

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

    public String getColor() {
        return color;
    }

    public String getSort() {
        return sort;
    }

}
```
`封装是将类的属性（数据）和方法（行为）包装在一起，并隐藏内部实现细节，只对外暴露必要的访问接口。` 这些含义我当初学看的那叫一个一头雾水，现在我们来根据我们的代码来理解。<br>
Chess类中有4个成员变量`name`、`color`、`sort`,其中name是用来接收外部传进来的棋子全称（阵营颜色+兵种），只在本类中使用。color和sort是用字符串的substring方法切割name得到的，外部判定阵营和兵种的情况下会读取。但不会写入，因为棋子一旦初始化后，是车就是车，马就是马，不可能说下棋下到一半棋子还能变身的。所以Chess类中只有color和sort（还有initY）有get方法，而且没有set方法。这也就是所谓的对外暴露必要的访问接口，而不是一味的对每一个成员变量都实现get，set方法。<br>
而这三个变量都是用private修饰的，也就是只能在本类中直接访问：`this.成员变量`，其他类只能调用提供的get，set方法访问，也就是上面说的隐藏内部实现细节，让成员变量更安全，不是说随随便便就能增删查修的。
### 继承
``` Java
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
```
继承概念很好理解，作用是提高代码复用性，也很好理解，这里着重解析抽象类。抽象的关键字是`abstract`，抽象类中的抽象方法子类必须重写。抽象类虽然不能被实例化，但是其构造函数可以用`super`继承给子类使用，所以也是必须要有的。<br>
项目中Chess类一共有7个子类（che类是其中一个），每种棋子的走法都不一样，有像车一样简单的判定，也有像马和象一样有特殊的蹩脚判定，甚至还有像卒一样分过河状态来判定走法的。<br>
假设你日后还想给这个项目增加新的兵种继承自Chess类，那么这个兵种也必须重写走法判定这个方法，否则就会报错。
### 多态
多态我最烦网上那些解释了--`同一个方法在不同的对象上可以表现出不同的行为。`这样解释跟没解释有什么区别？还是不懂多态有什么用处啊！<br>
``` Java
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
```
简单解释一下，我将棋盘分成9*10的数组`Chess[][] chess`，每个格子的横纵坐标（左上角（0，0））对应数组的索引。代码中的棋子工厂用于创建棋子对象。<br>
如果不像我代码中那样用多态和简单工厂初始化棋盘，代码就像下面这样（反面教材）。
``` Java
for (int i = 0; i < chessName.length; i++) {
    Chess chess[i][i] = new Che("r_che", calPosX(i), calPosY(i))
    Chess chess[i][i] = new Ma("r_che", calPosX(i), calPosY(i))
    Chess chess[i][i] = new Xiang("r_che", calPosX(i), calPosY(i))
    Chess chess[i][i] = new Shi("r_che", calPosX(i), calPosY(i))
    Chess chess[i][i] = new Jiang("r_che", calPosX(i), calPosY(i)) 
    // 以下省略n行代码用于创建全部的棋子
}
```
上述代码虽然也用了多态，但根本没有体现到多态的优美与简洁，最主要的原因就是没有用到多态的解耦合。<br>
首先我要解释以下何为耦合度，一直听老登程序员耦合度来耦合度去的，说大白话就是你的功能通过传参的形式来实现的多不多。就像上面这段反面教材一样
吨吨吨的一股脑写32行代码来创建32个棋子，创建一次写一次这个棋子在哪个坐标，是什么颜色，是什么兵种。假设等你后面需要更改棋盘初始化信息时，
例如用户对战的时候是棋王vs臭棋篓子，棋王这一把想让一颗炮，下一把想让一颗车，每把还红黑交换，那你就改吧，一改一个不吱声，这个需求封装不成一个方法，
因为`new Chess子类（）`Chess子类只能自己亲手去那32行代码中翻垃圾，这就是耦合度太高带来的不便，甚至是功能都无法实现了。<br>
反观第一个例子中使用的ChessFactory类配合多态，极大的降低了代码的耦合度。因为我把要创建的棋子类的全称按顺序存储在一个数组chessName中，然后通过遍历这个数组让棋子工厂
将我要的棋子一个个，按顺序return到chess这个二维数组当中，省去了自己new棋子的过程。当我需要实现棋王vs臭棋篓子的需求时，也只需要封装一个方法去根据chessName的索引
更改数组的内容即可。<br>
而两者具体是如何配合的-- 很简单，ChessFactory类中的creatChess方法返回的是Chess这个父类，也就是常说的"父类引用指向子类对象"，如果没有多态，那这个方法该具体返回哪个子类对象呢？<br>
当然多态的特点不止有解耦合，但我相信你通过了解这个例子，对多态的理解已经比之前深很多了。
## IO流
项目里主要用到了ObjectOutputStream和ObjectInputStream，与正常的io流不同的是，它们可以序列化对象，并且可以保存对象中的属性。通过io流将棋盘信息导入自定义ser文件中实现存档读档功能
