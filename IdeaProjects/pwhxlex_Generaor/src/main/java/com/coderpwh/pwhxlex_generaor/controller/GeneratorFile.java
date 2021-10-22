package com.coderpwh.pwhxlex_generaor.controller;

import com.coderpwh.pwhxlex_generaor.entry.DFA;
import com.coderpwh.pwhxlex_generaor.service.Lex;
import com.coderpwh.pwhxlex_generaor.service.LexImpl;

import java.io.*;

public class GeneratorFile {
    private Lex lex = null;
    private String sourcePath = null;
    private String dotPath = null;
    public boolean generator(String input) {
        return generator(input,null,null);
    }
    public boolean generator(String input,String sp,String dot) {
        try{
            if (sp == null) {
                String osName = System.getProperty("os.name");
                if (osName.indexOf("linux")>0) {
                    this.sourcePath = "/tmp/test/";
                } else if(osName.indexOf("windows")>0) {
                    this.sourcePath = "C:\\temp\\test";
                }
            }else {
                this.sourcePath = sp;
            }
            if(dot == null) {
                this.dotPath = "not";
            } else {
                this.dotPath = dot;
            }

            Lex lex = new LexImpl();
            lex.getNFA(input);
            lex.getDFA();
        }catch (Exception e){
            return false;
        }

        return true;
    }

    public File createNFAFile(){
        String str = lex.generateNFADotString(lex.lexNFA.nfaGraph);
        String imagesSourcePath = this.sourcePath + "nfa.png";

        return new File(imagesSourcePath);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("dot --help");
        InputStream inputStream = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while((line = br.readLine())!=null) {
            System.out.println(line);
        }
        process.waitFor();
    }
    public File createDFAFile(){

        return null;
    }
    public File createMinDFAFile(){

        return null;
    }
    public String createCCode(){
        return null;
    }
}
