package com.coderpwh.pwhxlex_generaor.entry;

import java.util.Vector;

public class DFA {
    public Vector<Vector<Integer>> mVexs = new Vector<>();
    public MyGraph dfaGraph = new MyGraph();
    private int startStatus;
    private Vector<Integer> endStatus = new Vector<>();
    public int getTargetStatus(int node, char condition){
        for (int i = 0; i < mVexs.size(); i++) {
            if (dfaGraph.getEdgeValue(node,i).get(0)==condition) {
                return i;
            }
        }
        return -1;
    }
    public void setStartStatus(int startStatus) {
        this.startStatus = startStatus;
    }

    public int getStartStatus() {
        return startStatus;
    }

    public Vector<Integer> getEndStatus() {
        return endStatus;
    }

}
