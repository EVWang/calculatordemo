package com.ev.calc;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: wangyw
 * @time: 2023/4/24 下午4:40
 */
public class Calculator {

    @Getter@Setter
    private BigDecimal preResult; // 前面累计计算值

    @Getter
    private BigDecimal newNum; // 新计算值

    @Getter@Setter
    private List<BigDecimal> lastNumList = new ArrayList<>(); // 最近操作值数组

    @Getter@Setter
    private List<String> lastOptList = new ArrayList<>(); // 最近操作数组

    @Getter@Setter
    private List<BigDecimal> lastResultList = new ArrayList<>(); // 最近结果集数组

    @Getter@Setter
    private String curOperator; // 当前操作符 + - * /

    @Getter@Setter
    private int scale = 2; // 默认精度2位小数

    @Getter@Setter
    private int lastOptIndex = -1; // undo/redo最近操作索引

    @Getter@Setter
    private int validIndexMax = -1; // undo/redo有效索引最大值


    public void setNewNum(BigDecimal newNum) {
        if(preResult == null){ // 未计算过,累计总值为第一个输入值
            preResult = newNum;
        }else{
            this.newNum = newNum;
        }
    }

    /**
     *  计算,相当于计算器的'='按钮
     */
    public void calc(){
        preResult = preResult == null ? BigDecimal.ZERO : preResult;
        if(curOperator == null){
            System.out.println("请选择操作!");
        }
        if(newNum != null){ // 新输入值
            // 累加计算
            BigDecimal ret = calcTwoNum(preResult, curOperator, newNum);
            if(this.lastOptIndex == -1){ // 未处于redo/undo中间过程
                lastResultList.add(preResult);
                lastNumList.add(newNum);
                lastOptList.add(curOperator);
            }else{ // 处于redo/undo中间过程,覆盖undo/redo操作记录,并记录有效索引最大值
                this.lastOptIndex++;
                this.validIndexMax = this.lastOptIndex;
                this.lastResultList.set(this.lastOptIndex, ret);
                this.lastNumList.set(this.lastOptIndex-1, newNum);
                this.lastOptList.set(this.lastOptIndex-1, curOperator);
            }
            preResult = ret;
            curOperator = null;
            newNum = null;
        }
    }

    /**
     * 回撤到上一步
     */
    public void undo(){
        if(preResult != null && lastOptIndex == -1){ // 未进行undo/redo操作,存储最后计算结果
            lastResultList.add(preResult);
            curOperator = null;
            newNum = null;
        }

        if(lastResultList.size() == 0){
            System.out.println("无操作!");
        }else if(lastResultList.size() == 1){
            System.out.println("undo后值:0,"+"undo前值:"+ preResult);
            preResult = BigDecimal.ZERO;
        } else {
            if(lastOptIndex == -1){
                lastOptIndex = lastOptList.size()-1;
            }else{
                if(lastOptIndex-1 < 0){
                    System.out.println("无法再undo!");
                    return;
                }
                lastOptIndex--;
            }
            undoOperate(lastResultList.get(lastOptIndex),lastOptList.get(lastOptIndex), lastNumList.get(lastOptIndex));
        }
    }

    /**
     *  根据回撤进行重做
     */
    public void redo(){
        try{
            if(lastOptIndex > -1){
                if(lastOptIndex + 1 == lastResultList.size() || lastOptIndex+1 == this.validIndexMax+1){
                    System.out.println("无法再redo!");
                    return;
                }
                lastOptIndex++;

                redoOperate(lastResultList.get(lastOptIndex),lastOptList.get(lastOptIndex-1), lastNumList.get(lastOptIndex-1));
            }
        }catch (Exception e){
            System.out.println("redo异常,lastOptIndex:"+lastOptIndex);
        }
    }

    private void redoOperate(BigDecimal redoTotal, String redoOpt, BigDecimal redoNum) {
        System.out.println("redo后值:"+redoTotal.setScale(scale, BigDecimal.ROUND_HALF_UP)+",redo前值:"+ preResult.setScale(scale, BigDecimal.ROUND_HALF_UP) +",redo的操作:"+redoOpt+",redo操作的值:"+redoNum);
        preResult = redoTotal;
        curOperator = null;
        newNum = null;
    }

    private void undoOperate(BigDecimal lastTotal, String lastOpt, BigDecimal lastNum) {
        System.out.println("undo后值:"+lastTotal.setScale(scale, BigDecimal.ROUND_HALF_UP)+",undo前值:"+ preResult.setScale(scale, BigDecimal.ROUND_HALF_UP) +",undo的操作:"+lastOpt+",undo操作的值:"+lastNum);
        preResult = lastTotal;
        curOperator = null;
        newNum = null;
    }

    /**
     * 进行累计计算
     * @param preResult 前面已累计值
     * @param curOperator 当前操作
     * @param newNum 新输入值
     * @return 计算结果
     */
    private BigDecimal calcTwoNum(BigDecimal preResult, String curOperator, BigDecimal newNum) {
        BigDecimal ret = BigDecimal.ZERO;
        curOperator = curOperator == null ? "+" : curOperator;
        switch (curOperator){
            case "+":
                ret = preResult.add(newNum);
                break;
            case "-":
                ret = preResult.subtract(newNum).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "*":
                ret = preResult.multiply(newNum).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "/":
                ret = preResult.divide(newNum, RoundingMode.HALF_UP);
                break;
        }
        return ret;
    }

    /**
     * 显示操作结果
     */
    public String display(){
        StringBuilder sb = new StringBuilder();
        if(preResult != null){
            sb.append(preResult.setScale(scale, BigDecimal.ROUND_HALF_UP).toString());
        }
        if(curOperator != null){
            sb.append(curOperator);
        }
        if(newNum != null){
            sb.append(newNum);
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setNewNum(new BigDecimal(9.19));
        calculator.setCurOperator("*");
        calculator.setNewNum(new BigDecimal(5.50));
        calculator.display();
        calculator.calc();
        calculator.display();
        calculator.setCurOperator("/");
        calculator.setNewNum(new BigDecimal(2));
        calculator.display();
        calculator.calc();
        calculator.display();

        calculator.undo();
        calculator.display();
        calculator.undo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();

    }

}
