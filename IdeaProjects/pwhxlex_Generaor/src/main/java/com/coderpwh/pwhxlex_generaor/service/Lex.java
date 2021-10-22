package com.coderpwh.pwhxlex_generaor.service;

import com.coderpwh.pwhxlex_generaor.entry.DFA;
import com.coderpwh.pwhxlex_generaor.entry.MyGraph;
import com.coderpwh.pwhxlex_generaor.entry.NFA;
import com.coderpwh.pwhxlex_generaor.entry.Node;

import java.util.Stack;
import java.util.Vector;

public abstract class Lex {
    public NFA lexNFA = new NFA();
    public DFA lexDFA = new DFA();
    public Stack<Character> operatorStack = new Stack<>();
    public Stack<Node> nfaStatusPointStack = new Stack<>();
    public Vector<Character> alphabet = new Vector<>();

    public abstract void getNFA(String regxInput); //获取NFA
    public abstract boolean isOperator(char ch); //判断是否是操作符
    public abstract void createBasicNFA(char ch);  //创建基本的NFA，两个节点一条边，具体就是是把两个节点连起来，边的值为转移条件
    public abstract void repeatCharacterOperation(); //遇到重复符号的操作
    public abstract void selectorCharacterOperation(); //遇到选择符的操作
    public abstract void joinerCharacterOperation();  //遇到连接符的操作
    public abstract String generateNFADotString(MyGraph myGraph); //生成NFA的dot文本

    public abstract void getDFA(); //获取DFA
    public abstract Vector<Integer> e_closure(Vector<Integer> statusArray);
    public abstract Vector<Integer> nfaMove(Vector<Integer> statusArray,char condition);
    public abstract int isDFAStatusRepeat(Vector<Integer> a);
    public abstract String generateDFADotString(MyGraph myGraph,int choice);
    public abstract boolean isEndDFAStatus(Vector<Integer> nfaArray); //判断是否是终止状态

    public abstract void minimizeDFA(); //最小化DFA
    public abstract boolean isInDFAEndStatus(int i); //判断节点序号是否在DFA终止节点集合中
    public abstract void mergeTwoNode(int a, int b); //合并两个节点

    public abstract String generateCCode(MyGraph myGraph); //生成c语言词法分析程序


}
