package com.coderpwh;

import com.coderpwh.pwhxlex_generaor.entry.DFA;
import com.coderpwh.pwhxlex_generaor.entry.MyGraph;
import com.coderpwh.pwhxlex_generaor.service.Lex;
import com.coderpwh.pwhxlex_generaor.service.LexImpl;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.Vector;

@Testable
public class Test4Generator {
    @Test
    public void test() {
        Lex lex = new LexImpl();
        lex.getNFA("a*b");
        MyGraph nfaGraph = lex.lexNFA.nfaGraph;
        nfaGraph.printMyGraph();
//        System.out.println(lex.generateNFADotString(nfaGraph));
        lex.getDFA();
        System.out.println(lex.generateDFADotString(lex.lexDFA.dfaGraph, 0));
        System.out.println(" -- --  --  --  --");
        System.out.println(lex.generateDFADotString(lex.lexDFA.dfaGraph, 1));
        System.out.println(lex.generateCCode(lex.lexDFA.dfaGraph));
     /*   lex.minimizeDFA();
        lex.lexDFA.dfaGraph.printMyGraph();*/




    }
}
