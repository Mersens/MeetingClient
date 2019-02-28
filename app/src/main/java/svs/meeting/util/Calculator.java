package svs.meeting.util;

/**
 * Created by jngoogle on 2016/11/27 17:05.
 * email: guyuanhaofan@cnicg.cn
 */

import java.util.ArrayList;
import java.util.Stack;

/**
 * 计算输入的表达式
 */
public class Calculator {

    public static String calculate(String downStr) throws Exception {
        ArrayList<String> inOrderList = getStringList(downStr);
        ArrayList<String> postOrderList = getPostOrderList(inOrderList);
        double result = calPostOrderList(postOrderList);
        if (result == Math.floor(result)) {
            return (long) result + "";
        }
        return result + "";
    }

    /**
     * 将字符串放入栈中 --- 中序表达式
     * 按照 数字 符号 这种中序的顺序存入栈中
     *
     * @return
     */
    private static ArrayList<String> getStringList(String string) {

        ArrayList<String> arrayList = new ArrayList<>();
        String num = "";

        for (int i = 0; i < string.length(); i++) {

            int index = i;
            if ((Character.isDigit(string.charAt(i)) || string.charAt(i) == '.')) {
                // 当前的字符串是否是数字或者是点号
                num += string.charAt(i);
            } else {// 当前输入的字符不是数字或者点号

                if (index == 0) {
                    // 当最后一次计算结果为负数时候，
                    //为输入的String第一位添加0 保证再次做减法的正确性
                    arrayList.add("0");
                }

                if (num != "") {
                    arrayList.add(num);
                }

                arrayList.add(string.charAt(i) + "");
                num = "";
            }
        }

        if (num != "") {// 添加最后一个数字
            arrayList.add(num);
        }

        return arrayList;
    }

    /**
     * 把中缀表达式转化成后缀表达式
     *
     * @param inOrderList
     * @return
     */
    private static ArrayList<String> getPostOrderList(ArrayList<String> inOrderList) {

        ArrayList<String> postOrderList = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        for (int i = 0; i < inOrderList.size(); i++) {

            String current = inOrderList.get(i);
            if (isOperator(current)) {// 如果是运算符
                while (!operatorStack.isEmpty() && compareLevel(operatorStack.peek(), current)) {
                    postOrderList.add(operatorStack.pop());
                }
                operatorStack.push(current);
            } else {// 如果不是运算符, 是数字或者是点号
                postOrderList.add(inOrderList.get(i));
            }
        }

        while (!operatorStack.isEmpty()) {
            postOrderList.add(operatorStack.pop());
        }

        return postOrderList;
    }

    /**
     * 比较运算符之间的优先级
     * 比较当前运算符与运算符栈顶的运算符的优先级
     * 若栈顶元算符优先级大于当前元算符则出栈，把运算符栈顶元素放入
     * 后缀表达式栈中
     * ---
     * 若栈顶元算符优先级大于或等于当前元算符返回true
     *
     * @param stackTop 储存运算符
     * @return
     */
    private static boolean compareLevel(String stackTop, String current) {
        if (stackTop.equals("*") && (current.equals("*")
                || current.equals("/")
                || current.equals("+")
                || current.equals("-"))) {
            return true;
        }

        if (stackTop.equals("/") && (current.equals("*")
                || current.equals("/")
                || current.equals("+")
                || current.equals("-"))) {
            return true;
        }

        if (stackTop.equals("+") && (current.equals("+")
                || current.equals("-"))) {
            return true;
        }

        if (stackTop.equals("-") && (current.equals("-")
                || current.equals("+"))) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否是运算符
     *
     * @return
     */
    private static boolean isOperator(String string) {
        if (string.equals("+")
                || string.equals("-")
                || string.equals("*")
                || string.equals("/")) {

            return true;
        }

        return false;
    }

    /**
     * 计算后缀表达式
     * 如果是数字或者点号直接存入到堆栈中，遇到运算符则弹出栈顶的两个元素
     * 然后计算得到结果之后在存入栈中
     */
    private static double calPostOrderList(ArrayList<String> postOrderList) throws Exception {

        Stack<String> stack = new Stack<>();
        for (int i = 0; i < postOrderList.size(); i++) {

            String curStr = postOrderList.get(i);
            if (isOperator(curStr)) {// 弹出栈顶的两个元素,然后进行计算,注意a是被操作数先弹出的a
                double a = Double.parseDouble(stack.pop());
                double b = Double.parseDouble(stack.pop());
                double result = 0.0;
                switch (curStr.charAt(0)) {
                    case '+':
                        result = b + a;
                        break;
                    case '-':
                        result = b - a;
                        break;
                    case '/':
                        if (a == 0) throw new Exception();
                        result = b / a;
                        break;
                    case '*':
                        result = b * a;
                        break;
                }

                stack.push(result + "");
            } else {
                stack.push(curStr);
            }
        }

        return Double.parseDouble(stack.pop());
    }
}
