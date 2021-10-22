package com.coderpwh.pwhxlex_generaor.entry;

import java.util.Vector;

public class MyGraph {
    public int vmVexNum = 0; //顶点数目
    public int mEdgeNum = 0; //边的数目
    public Vector<Character> mMatrix[][];

    public MyGraph() {
        mMatrix = new Vector[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                mMatrix[i][j] = new Vector<>();
            }
        }
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                mMatrix[i][j].add('^');
            }
        }
    }
    public void printMyGraph() {
        System.out.println("printMyGraph");
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if(getEdgeValue(i,j).get(0)!='^') {
                    System.out.println("状态"+i+"----"+getEdgeValue(i,j).get(0)+"-->"+"状态"+j);
                }
            }
        }
    }
    public Vector<Character> getEdgeValue(int a,int b){
        return mMatrix[a][b];
    }
    public void deleteEdge(int a,int b) {
        mMatrix[a][b].clear();
        mMatrix[a][b].add('^');
        System.out.println("["+a+","+b+"]"+"断开连接");
    }
    public void addEdge(int a,int b,char edgeCondition) {
        System.out.println("edgeCondition:"+edgeCondition);
        if(mMatrix[a][b].get(0)=='^') {
            mMatrix[a][b].clear();
        }
        if(mMatrix[a][b].contains(edgeCondition)) {
            System.out.println("["+a+","+b+"]"+"重复连接");
        }else {
            mMatrix[a][b].add(edgeCondition);
            System.out.println("["+a+","+b+"]"+"连接");
        }
    }
}
