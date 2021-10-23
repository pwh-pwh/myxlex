package com.coderpwh.pwhxlex_generaor.controller;

import com.coderpwh.pwhxlex_generaor.entry.DFA;
import com.coderpwh.pwhxlex_generaor.service.Lex;
import com.coderpwh.pwhxlex_generaor.service.LexImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeneratorFile {
    private Lex lex = null;
    private String sourcePath = null;
    private String dotPath = null;

    public Lex getLex() {
        return lex;
    }

    public void setLex(Lex lex) {
        this.lex = lex;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDotPath() {
        return dotPath;
    }

    public void setDotPath(String dotPath) {
        this.dotPath = dotPath;
    }

    public Map<String,Object> generator(String input) {
        return generator(input,null,null);
    }
    public Map<String,Object> generator(String input,String sp,String dot) {
        Map<String,Object> map = new HashMap<>();
        try{
            System.out.println("----=====-----");
            if (sp == null&&this.sourcePath==null) {
                System.out.println("elelelel");
                String osName = System.getProperty("os.name");
                System.out.println(osName.toLowerCase(Locale.ROOT));
                System.out.println(osName.toLowerCase(Locale.ROOT).indexOf("linux"));
                if (osName.toLowerCase(Locale.ROOT).contains("linux")) {
                    this.sourcePath = "/home/pwh/homework/temp/";
                } else if(osName.toLowerCase(Locale.ROOT).contains("windows")) {
                    this.sourcePath = "C:\\temp\\test\\";
                }
            }else {
                System.out.println("ttttltltlllt");
                if(sp!=null) {
                    this.sourcePath = sp;
                }


            }
            if(dot == null&&getDotPath()==null) {
                this.dotPath = "dot";
            } else {
                if(dot!=null){
                    this.dotPath = dot;
                }
            }

            lex = new LexImpl();
            lex.getNFA(input);
            lex.getDFA();
            File nfaFile = createNFAFile();
            map.put("nfa",nfaFile);
            File dfaFile = createDFAFile();
            map.put("dfa",dfaFile);
            File minDFAFile = createMinDFAFile();
            map.put("mindfa",minDFAFile);
            String cCode = createCCode();
            map.put("code",cCode);
        }catch (Exception e){
            System.out.println("aaabbbccc");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return map;
    }

    public File createNFAFile() throws IOException, InterruptedException {
        String str = lex.generateNFADotString(lex.lexNFA.nfaGraph);
        String imagesSourcePath = this.sourcePath + "nfa.png";
        String nfaDotPath = this.sourcePath + "nfa.dot";
        File dotfile = new File(nfaDotPath);
        if (dotfile.exists()&&dotfile.isFile()) {
            dotfile.delete();
        }
        boolean newFile = dotfile.createNewFile();
        System.out.println("is create "+newFile);
        FileOutputStream dotfos = new FileOutputStream(dotfile);
        dotfos.write(str.getBytes(StandardCharsets.UTF_8));
        dotfos.flush();
        dotfos.close();
        execDot(nfaDotPath,imagesSourcePath);
        return new File(imagesSourcePath);
    }
    public void execDot(String dotfilePath,String imgPath) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String[] shell = new String[] {dotPath,dotfilePath,"-Tpng","-o",imgPath};
        for (String s : shell) {
            System.out.println(s);
        }

        if(runtime == null) {
            System.out.println("runtime is null");
        }else {
            System.out.println("shell is:");
            for (String s : shell) {
                System.out.println(s);
            }
            Process process = runtime.exec(shell);
            process.waitFor();
        }



    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        System.out.println(osName);
        GeneratorFile gf = new GeneratorFile();
        gf.setSourcePath("/home/pwh/homework/temp/");
        Map<String, Object> generator = gf.generator("a*b");
        String code = (String)generator.get("code");
        System.out.println(code);
        /*GeneratorFile generatorFile = new GeneratorFile();
        Lex lex = new LexImpl();
        lex.getNFA("a*b");
        generatorFile.setLex(lex);
        generatorFile.setDotPath("dot");
        generatorFile.setSourcePath("/home/pwh/桌面/");
        File dfaFile = generatorFile.createNFAFile();*/


/*        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("dot /home/pwh/桌面/test.dot -Tpng -o /home/pwh/桌面/tmp.png");
        InputStream inputStream = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while((line = br.readLine())!=null) {
            System.out.println(line);
        }
        process.waitFor();*/
    }
    public File createDFAFile() throws IOException, InterruptedException {
        String str = lex.generateDFADotString(lex.lexDFA.dfaGraph,0);
        String imagesSourcePath = this.sourcePath + "dfa.png";
        String dfaDotPath = this.sourcePath + "dfa.dot";
        File dotfile = new File(dfaDotPath);
        if (dotfile.exists()&&dotfile.isFile()) {
            dotfile.delete();
        }
        boolean newFile = dotfile.createNewFile();
        System.out.println("is create "+newFile);
        FileOutputStream dotfos = new FileOutputStream(dotfile);
        dotfos.write(str.getBytes(StandardCharsets.UTF_8));
        dotfos.flush();
        dotfos.close();
        execDot(dfaDotPath,imagesSourcePath);
        return new File(imagesSourcePath);
    }
    public File createMinDFAFile() throws IOException, InterruptedException {
        lex.minimizeDFA();
        String str = lex.generateDFADotString(lex.lexDFA.dfaGraph,1);
        String imagesSourcePath = this.sourcePath + "mindfa.png";
        String dfaDotPath = this.sourcePath + "mindfa.dot";
        File dotfile = new File(dfaDotPath);
        if (dotfile.exists()&&dotfile.isFile()) {
            dotfile.delete();
        }
        boolean newFile = dotfile.createNewFile();
        System.out.println("is create "+newFile);
        FileOutputStream dotfos = new FileOutputStream(dotfile);
        dotfos.write(str.getBytes(StandardCharsets.UTF_8));
        dotfos.flush();
        dotfos.close();
        execDot(dfaDotPath,imagesSourcePath);
        return new File(imagesSourcePath);

    }
    public String createCCode(){
        return lex.generateCCode(lex.lexDFA.dfaGraph);
    }
}
