package com.coderpwh.pwhxlex_generaor.entry;

import java.util.Vector;

public class NFA {
    public Vector<Integer> mVexs = new Vector<>();
    public MyGraph nfaGraph = new MyGraph();
    private int startStatus;

    public int getStartStatus() {
        return startStatus;
    }

    public void setStartStatus(int startStatus) {
        this.startStatus = startStatus;
    }

    public int getEndStatus() {
        return endStatus;
    }

    public void setEndStatus(int endStatus) {
        this.endStatus = endStatus;
    }

    private int endStatus;

}
