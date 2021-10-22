package com.coderpwh.pwhxlex_generaor.service;

import com.coderpwh.pwhxlex_generaor.entry.MyGraph;
import com.coderpwh.pwhxlex_generaor.entry.Node;
import javafx.util.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LexImpl extends Lex{
    @Override
    public void getNFA(String regxInput) {
        int strLen = regxInput.length();
        char ch;

        /*1. 对输入的正则表达式进行循环扫描每一个元素，建立运算符栈和NFA栈*/
        for(int i =0; i < strLen; i++){
            ch = regxInput.charAt(i);


            //如果是运算符
            if(isOperator(ch)){
                switch (ch) {
                    case '*'://重复符优先级别第一

                        //计算结果压入NFA栈中
                        repeatCharacterOperation();

                        //如果下一个字符是字母或者是左括号，需要添加连接符
                        if((i+1<strLen)&&(regxInput.charAt(i+1) == '(' || !isOperator(regxInput.charAt(i+1)))){
                            operatorStack.push('&');
                        }
                        break;

                    case '|': //选择符优先级第三，可以省略不写,需要将运算符栈符号中的&出栈并计算，遇到左括号(停止
                        if (operatorStack.empty()){
                            System.out.println("运算符栈为空");
                            //如果运算符栈为空，就什么事情也不用做，最后一下把该符号压栈就可以了
                        }
                        else{
                            //运算符栈不为空
                            ch = operatorStack.peek();
                            System.out.println("当前栈顶元素为"+ch);
                            while(ch != '('){
                                if(ch == '&'){
                                    //栈顶元素为连接符，需要先进行连接符的计算
                                    joinerCharacterOperation();
                                } else{
                                    break;
                                }
                                if (!operatorStack.isEmpty()) {
                                    ch = operatorStack.peek();
                                } else {
                                    break;
                                }

                            }
                        }

                        operatorStack.push('|');
                        System.out.println("将选择符压入栈中");
                        break;

                    case '('://左括号
                        operatorStack.push(ch); //将左括号压入栈中
                        break;
                    case ')'://右括号

                        //这里需要对括号内进行计算，并把计算结果压栈
                        ch = operatorStack.peek();

                        while(ch != '('){

                            switch (ch) {
                                case '&':
                                    joinerCharacterOperation();
                                    break;
                                case '|':
                                    selectorCharacterOperation();
                                    break;
                                default:
                                    break;
                            }
                            ch = operatorStack.peek();
                        }

                        operatorStack.pop(); //此时运算符栈顶元素是左括号，需要移除出去
                        //如果下一个字符是字母或者是左括号，需要添加连接符
                        if((i+1<strLen)&&(regxInput.charAt(i+1) == '(' || !isOperator(regxInput.charAt(i+1)))){
                            operatorStack.push('&');
                        }
                        break;

                    default:
                        System.out.println("ok"+ch);
                        break;
                }
            }
            else{//不是运算符
                boolean flag = true;//是否添加到字母表中
                for (Character character : alphabet) {
                    if (ch == character) {
                        flag = false;
                        break;
                    }
                }

                if (flag){
                    alphabet.add(ch);
                }
                //创建一个基本的NFA，也就是比如a,a的NFA就是两个节点，起始点和终止点，边的值为a
                createBasicNFA(ch);

                //如果下一个字符是字母的话，就向符号栈中加入一个连接符&
                if(i+1 <strLen && (!isOperator(regxInput.charAt(i+1)) || regxInput.charAt(i+1) == '(')){
                    operatorStack.push('&');
                }
            }
        }

        /*2. 对最终的NFA栈和运算符栈进行处理（如果不为空的话）*/
        if (operatorStack.empty()){
            System.out.println("最终中运算符栈为空");
        }else{
            System.out.println("最终中运算符栈不为空");
        }
        while(!operatorStack.empty()){
            ch= operatorStack.peek();
            switch (ch) {
                case '|':
                    selectorCharacterOperation();
                    break;
                case '&':
                    joinerCharacterOperation();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean isOperator(char ch) {
        return ch=='('||ch==')'||ch=='*'||ch=='|';
    }

    @Override
    public void createBasicNFA(char ch) {

        System.out.println("createBasicNFA");
        //据字母创建最基本NFA的操作
        int startPoint = lexNFA.mVexs.size() + 1; //分配的节点序号是以前节点序号+1
        int endPoint = startPoint + 1;
        lexNFA.nfaGraph.addEdge(startPoint,endPoint,ch); //增加一个边对边的连接，边的条件是转换条件，就是该字符。

        lexNFA.nfaGraph.vmVexNum = lexNFA.nfaGraph.vmVexNum+2; //增加两个节点

        lexNFA.nfaGraph.mEdgeNum++; //边数加1

        //添加到节点数组中
        lexNFA.mVexs.add(startPoint);
        lexNFA.mVexs.add(endPoint);

        int[] newNFAStatusPoint = new int[2];
        newNFAStatusPoint[0] = startPoint;
        newNFAStatusPoint[1] = endPoint;

        nfaStatusPointStack.push(new Node(newNFAStatusPoint));

        //起点终点设置
        lexNFA.setStartStatus(startPoint);
        lexNFA.setEndStatus(endPoint);
    }

    @Override
    public void repeatCharacterOperation() {
        //进行重复符的操作
        //1.获取栈顶的一个元素
        int[] top1NFA = new int[2];
        for(int i =0;i<2;i++){
            top1NFA[i] = nfaStatusPointStack.peek().getnArray()[i];
        }
        nfaStatusPointStack.pop();

        //2.创建两个新的节点
        int newStartPoint1 = lexNFA.mVexs.size()+1;
        int newStartPoint2 = newStartPoint1+1;

        lexNFA.nfaGraph.addEdge(newStartPoint1,newStartPoint2,'E');
        lexNFA.nfaGraph.addEdge(newStartPoint1,top1NFA[0],'E');
        lexNFA.nfaGraph.addEdge(top1NFA[1],top1NFA[0],'E');
        lexNFA.nfaGraph.addEdge(top1NFA[1],newStartPoint2,'E');

        lexNFA.nfaGraph.vmVexNum = lexNFA.nfaGraph.vmVexNum+2; //节点数目加2
        lexNFA.nfaGraph.mEdgeNum = lexNFA.nfaGraph.mEdgeNum + 4; //边的数目加4

        //添加到节点数组中
        lexNFA.mVexs.add(newStartPoint1);
        lexNFA.mVexs.add(newStartPoint2);

        //3.将新的NFA压入NFA栈中
        int[] newNFAStatusPoint = new int[2];
        newNFAStatusPoint[0] = newStartPoint1;
        newNFAStatusPoint[1] = newStartPoint2;
        nfaStatusPointStack.push(new Node(newNFAStatusPoint));

        //起点终点设置
        lexNFA.setStartStatus(newStartPoint1);
        lexNFA.setEndStatus(newStartPoint2);
    }

    @Override
    public void selectorCharacterOperation() {
//进行选择符的操作
        //1.获取NFA栈顶的两个元素，并从栈中丢掉
        int[] top1NFA = new int[2];
        for(int i =0;i<2;i++){
            top1NFA[i] = nfaStatusPointStack.peek().getnArray()[i];
        }
        
        System.out.println("当前NFA栈顶的节点为：("+top1NFA[0]+","+top1NFA[1]+")\n");
        nfaStatusPointStack.pop();
        int[] top2NFA = new int[2];
        for(int i =0;i<2;i++){
            top2NFA[i] = nfaStatusPointStack.peek().getnArray()[i];
        }
        nfaStatusPointStack.pop(); //获取运算符栈顶的元素，并从栈中丢掉，因为运算完，这些就没用了，有用的是运算结果
        operatorStack.pop();

        //2.创建两个新的节点
        int newStartPoint1 = lexNFA.mVexs.size()+1;
        int newStartPoint2 = newStartPoint1+1;

        lexNFA.nfaGraph.addEdge(newStartPoint1,top1NFA[0],'E');
        lexNFA.nfaGraph.addEdge(newStartPoint1,top2NFA[0],'E');
        lexNFA.nfaGraph.addEdge(top2NFA[1],newStartPoint2,'E');
        lexNFA.nfaGraph.addEdge(top1NFA[1],newStartPoint2,'E');

        lexNFA.nfaGraph.vmVexNum = lexNFA.nfaGraph.vmVexNum+2; //节点数目加2
        lexNFA.nfaGraph.mEdgeNum = lexNFA.nfaGraph.mEdgeNum + 4; //边的数目加4
        lexNFA.mVexs.add(newStartPoint1); //添加到节点数组中
        lexNFA.mVexs.add(newStartPoint2);

        //3.将新的NFA压入栈中
        int[] newNFAStatusPoint = new int[2];
        newNFAStatusPoint[0] = newStartPoint1;
        newNFAStatusPoint[1] = newStartPoint2;

        nfaStatusPointStack.push(new Node(newNFAStatusPoint));

        //设置起点和终点
        lexNFA.setStartStatus(newStartPoint1);
        lexNFA.setEndStatus(newStartPoint2);
    }

    @Override
    public void joinerCharacterOperation() {
        System.out.println("joinerCharacterOperation");
//进行连接符的操作
        //1.获取NFA栈顶的两个元素，并从栈中丢掉
        int[] top1NFA = new int[2];
        for(int i =0;i<2;i++){
            top1NFA[i] = nfaStatusPointStack.peek().getnArray()[i];
        }
        nfaStatusPointStack.pop();
        int[] top2NFA = new int[2];
        for(int i =0;i<2;i++){
            top2NFA[i] = nfaStatusPointStack.peek().getnArray()[i];
        }
        nfaStatusPointStack.pop();

        //获取运算符栈顶的元素，并从栈中丢掉，因为运算完，这些就没用了，有用的是运算结果
        operatorStack.pop();

        for(int i =1;i<lexNFA.nfaGraph.vmVexNum + 1;i++){
            if (i == top1NFA[0]){
                for (int j = 1; j <lexNFA.nfaGraph.vmVexNum + 1 ; ++j) {
                    if (lexNFA.nfaGraph.getEdgeValue(i,j).get(0) != '^'){
                        lexNFA.nfaGraph.addEdge(top2NFA[1],j,lexNFA.nfaGraph.getEdgeValue(i,j).get(0));
                        lexNFA.nfaGraph.deleteEdge(top1NFA[0],j);
                        lexNFA.nfaGraph.vmVexNum --;//节点数减1
                        lexNFA.mVexs.set(top1NFA[0],-1);
                    }
                }
                break;
            }
        }

        //3.将新的NFA压入栈中
        int[] newNFAStatusPoint = new int[2];
        newNFAStatusPoint[0] = top2NFA[0];
        newNFAStatusPoint[1] = top1NFA[1];
        nfaStatusPointStack.push(new Node(newNFAStatusPoint));

        //设置起点和终点
        lexNFA.setStartStatus(top2NFA[0]);
        lexNFA.setEndStatus(top1NFA[1]);
    }

    @Override
    public String generateNFADotString(MyGraph myGraph) {
        String tab = "    ";
        StringBuilder result = new StringBuilder();
        result.append("digraph G{\n" + tab + "\"\"[shape=none]\n");
        for (int i = 1; i <lexNFA.mVexs.size() + 1 ; ++i) {
            if (lexNFA.mVexs.get(i-1) != -1)
            {
                result.append(tab+"\""+i+"\"");
                if (i == lexNFA.getEndStatus()){
                    result.append("[shape=doublecircle]\n");
                }else{
                    result.append("[shape=circle]\n");
                };
            }
        }
        result.append("\n");
        result.append(tab+"\"\"->\""+lexNFA.getStartStatus()+"\"\n");
        int num = lexNFA.mVexs.size()  + 1;
        for(int i = 1 ;i<num  ;i ++){
            for(int j = 1;j<num;j++){
                if (myGraph.getEdgeValue(i,j).get(0) != '^'){
                    result.append(tab+"\""+i+"\""+" -> "+j+"[label=\""+myGraph.getEdgeValue(i,j).get(0)+"\"]\n");
                }
            }
        }
        result.append("}");

/*        ofstream ofile; //定义输出文件
        ofile.open(projectFile + string("\\dots\\nfa.dot"));

        ofile << result << endl;
        ofile.close();

        //执行生成nfa图片
        string temp = dot + "\\dot -Tjpg " + projectFile +"\\dots\\nfa.dot -o " + projectFile + "\\images\\nfa.jpg";
    const char *systemString = temp.c_str();

        cout << "命令行为" + temp << endl;
        system(systemString);//调用QT里的函数*/
        return  result.toString();

    }

    @Override
    public void getDFA() {
        //给DFA节点开头位置加一个空元素占位
        lexDFA.mVexs.add(new Vector<>());

        Vector<Integer>  initStatus = new Vector<>();
        initStatus.add(lexNFA.getStartStatus());
        Vector<Integer> initStatusTrans = e_closure(initStatus);
        //给DFA创建第一个节点
        int newDFAStartPoint = lexDFA.dfaGraph.vmVexNum + 1;
        lexDFA.dfaGraph.vmVexNum ++;//节点数加1
        lexDFA.setStartStatus(newDFAStartPoint);
        Stack<Integer> DFAStatusStack = new Stack<>();//存储还没有经过字母表转换的DFA状态, DFA状态栈,这个栈只需要存储DFA的序号就可以了，没存储DFA节点对应的NFA集合，
        DFAStatusStack.push(newDFAStartPoint);

        lexDFA.mVexs.add(initStatusTrans);//NFA起点的E转换集合赋值给DFA的第一个节点

        while (!DFAStatusStack.empty()){

            int topDFAStack = DFAStatusStack.peek();
            DFAStatusStack.pop();

            for (int i = 0; i < alphabet.size() ; ++i) { //对字母表的每个元素都需要作为转换条件

                Vector<Integer> tempArray = e_closure(nfaMove(lexDFA.mVexs.get(topDFAStack),alphabet.get(i)));

                if (tempArray.isEmpty()){//如果转换产生的NFA集合为空，跳过该次转换即可
                    continue;
                }
                int position = isDFAStatusRepeat(tempArray);//判断新生成DFA状态是否已经存在了，如果已经存在，返回该状态在节点的位置

                if (position == -1){//这个是新生成的DFA状态，没有重复
                    int tempDFAStatusNode = lexDFA.dfaGraph.vmVexNum + 1;
                    lexDFA.dfaGraph.vmVexNum ++;//节点数加1
                    if (isEndDFAStatus(tempArray)){
                        lexDFA.getEndStatus().add(tempDFAStatusNode);
                    }
                    lexDFA.mVexs.add(tempArray);//把NFA集合赋值给DFA的状态

                    DFAStatusStack.push(tempDFAStatusNode);//把新产生的DFA状态加入到DFA栈中
                    position = tempDFAStatusNode;
                }

                //连接节点，产生边
                lexDFA.dfaGraph.addEdge(topDFAStack,position,alphabet.get(i));
                lexDFA.dfaGraph.mEdgeNum ++;
            }
        }
    }

    @Override
    public Vector<Integer> e_closure(Vector<Integer> statusArray) {
        statusArray = new Vector<>(statusArray);
        for (Integer integer : statusArray) {
            System.out.println(integer);
        }
        Vector<Integer> resultsArray = new Vector<>(); //存放状态数组的E转换集合
        Stack<Integer> statusStack = new Stack<>(); //存放递归过程中的状态，当该栈为空的时候，递归结束

        for (int i = 0; i < statusArray.size(); ++i) {
            statusStack.push(statusArray.get(i)); //初始化状态栈
            resultsArray.add(statusArray.get(i)); //状态本身也可以通过E转换到达本身，所以需要将自身添加到结果数组中
        }

        while(!statusStack.empty()){
            int status = statusStack.peek();
            statusStack.pop();

            for (int i = 1; i < lexNFA.mVexs.size() + 1 ; ++i) {
                if (i == status){
                    for(int j = 1;j<lexNFA.mVexs.size()+1;j++){
                        if (lexNFA.nfaGraph.getEdgeValue(i,j).get(0) == 'E'){ //找到转移条件为E的的终点
                            statusStack.push(j); //加入到状态栈中
                            resultsArray.add(j); //加入到结果数组中
                        }
                    }
                }
            }
        }
        List<Integer> collect = resultsArray.stream().distinct().collect(Collectors.toList());
        resultsArray = new Vector<>(collect);
        for (int i = 0; i < resultsArray.size(); i++) {
            resultsArray.remove(resultsArray.size()-1);
        }
        resultsArray.addAll(collect);
        System.out.println("ecolosure转换结果为：");
        for (Integer integer : resultsArray) {
            System.out.println(integer);
        }
        return resultsArray;
    }

    @Override
    public Vector<Integer> nfaMove(Vector<Integer> statusArray, char condition) {
        statusArray = new Vector<>(statusArray);
        for (Integer integer : statusArray) {
            System.out.println(integer);
        }
        Vector<Integer> resultsArray = new Vector<>(); //结果集合
        Stack<Integer> statusStack = new Stack<>(); //状态栈

        for (Integer value : statusArray) {
            statusStack.push(value);
        }

        while(!statusStack.empty()){
            int status = statusStack.peek();
            statusStack.pop();
            for (int i = 1; i <lexNFA.mVexs.size()+1 ; ++i) {
                if (i == status){
                    for (int j = 1; j < lexNFA.mVexs.size() + 1; ++j) {
                        if (lexNFA.nfaGraph.getEdgeValue(i,j).get(0) == condition){
                            resultsArray.add(j); //把终点加入到结果集合中，但此时不需要再将终点压入到状态栈中
                        }
                    }
                }
            }
        }


        List<Integer> collect = resultsArray.stream().distinct().collect(Collectors.toList());
        resultsArray = new Vector<>(collect);
        System.out.println("result:");
        for (Integer integer : resultsArray) {
            System.out.println(integer);
        }

        return resultsArray;
    }

    @Override
    public int isDFAStatusRepeat(Vector<Integer> a) {
        int position = -1;
        for (Vector<Integer> mVex : lexDFA.mVexs) {
            if(mVex.size()!=a.size()){
                continue;
            }else {
                for (int i = 0; i < mVex.size(); i++) {
                    if(!Objects.equals(mVex.get(i), a.get(i))) {
                        break;
                    }else {
                        if (i == mVex.size()-1) {
                            position = 1;
                        }
                    }
                }
            }
        }

        return position;
    }

    @Override
    public String generateDFADotString(MyGraph myGraph, int choice) {

        String tab = "    ";
        StringBuilder result = new StringBuilder();
        result.append("digraph G{\n" + tab + "\"\"[shape=none]\n");
        for (int i = 1; i <myGraph.vmVexNum + 1 ; ++i) {

            if (!lexDFA.mVexs.get(i-1).isEmpty()){
                result.append(tab).append("\"").append(i).append("\"");
                boolean flag =true;

                for (int j = 0; j < lexDFA.getEndStatus().size(); ++j) {
                    if (i == lexDFA.getEndStatus().get(j)){
                        flag = false;
                        break;
                    }
                }
                if (!flag){
                    result.append("[shape=doublecircle]\n");
                }else{
                    result.append("[shape=circle]\n");
                };
            }
        }
        result.append("\n");
        result.append(tab + "\"\"->\"" + lexDFA.getEndStatus()+ "\"\n");

        int num = myGraph.vmVexNum + 1;
        for(int i = 0 ;i<num  ;i ++){
            for(int j = 0;j<num;j++){
                if (myGraph.getEdgeValue(i,j).get(0) != '^'){
                    for (int k = 0; k <myGraph.getEdgeValue(i,j).size() ; ++k) {
                        result.append(tab + "\"" +i + "\"" + " -> \"" + j + "\"[label=\"" + myGraph.getEdgeValue(i,j).get(k) + "\"]\n");
                    }
                }
            }
        }
        result.append("}");
        /*ofstream ofile;//定义输出文件
        string filePath;

        if (choice == 0){
            filePath = projectFile + string("\\dots\\dfa.dot");
        }else{
            filePath = projectFile + string("\\dots\\mindfa.dot");
        }
    const char *FilePathChar = filePath.c_str();

        cout << "打开文件路径" << FilePathChar << endl;
        ofile.open(FilePathChar);

        ofile << result << endl;
        ofile.close();
        //执行生成dfa图片
        string temp;
        if (choice == 0){
            temp = dot + "\\dot -Tjpg " + projectFile + "\\dots\\dfa.dot -o " + projectFile + "\\images\\dfa.jpg";
        }else{
            temp = dot + "\\dot -Tjpg " + projectFile + "\\dots\\mindfa.dot -o " + projectFile + "\\images\\mindfa.jpg";
        }
    const char *systemString = temp.c_str();

        cout << "命令行为" + temp << endl;

        system(systemString);*/
        return result.toString();

    }

    @Override
    public boolean isEndDFAStatus(Vector<Integer> nfaArray) {
        for (Integer integer : nfaArray) {
            if (lexNFA.getEndStatus() == integer) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void minimizeDFA() {
        Vector<Integer> noEndPointArray = new Vector<>();//非终止态节点集合
        Vector<Integer> endPointArray = new Vector<>(lexDFA.getEndStatus());
        //非终止状态集合
        for (int i = 1; i < lexDFA.mVexs.size(); ++i) {
            if (!isInDFAEndStatus(i)){
                noEndPointArray.add(i);
            }
        }//初始化非终止节点集合


        //终止状态集合
        for (int n = 0; n < endPointArray.size(); ++n) {
            System.out.println(endPointArray.get(n));
        }

        Vector<Pair<Vector<Integer>, Boolean>> dividedArrays = new Vector<>();//first存储的是划分的集合，second存储的是该划分集合是否可继续划分
        dividedArrays.add(new Pair<>(noEndPointArray, true));
        dividedArrays.add(new Pair<>(endPointArray, true));

        boolean flag = true;
        while(flag){
            for (int j = 0; j < dividedArrays.size(); ++j) {//对划分的每个集合进行操作
                int canNotBeDivided = 0;//经过一次字母表的转换，如果该集合的转换状态只有一个，说明该集合不能被该字母区分，该变量+1
                if (dividedArrays.get(j).getKey().size() == 1){
                    dividedArrays.set(j,new Pair<>(dividedArrays.get(j).getKey(),false));//如果集合元素只有一个，赋值为false，即不可再划分
                    continue;
                }
                for (int i = 0; i < alphabet.size() ; ++i) {
                    for (int m = 0; m < dividedArrays.get(j).getKey().size(); ++m) {
                        System.out.println(dividedArrays.get(j).getKey().get(m));
                    }
                    System.out.println("当前字母为"+alphabet.get(i));
                    Vector<Integer> arrayNumVector = new Vector<>();//存放DFA状态经过某个字母转换到的集合序号的数组

                    //first 为转换状态属于的集合序号，second DFA起点的状态节点
                    Vector<Pair<Integer,Integer>> statusMap = new Vector<>();//存放了每个节点的转换后属于的集合序号——该节点本身序号

                    for (int k = 0; k < dividedArrays.get(j).getKey().size(); ++k) { //获取到该集合的每个元素的转换状态属于的集合序号
                        int transStatus = lexDFA.getTargetStatus(dividedArrays.get(j).getKey().get(k),alphabet.get(i));//获取节点的转换DFA节点

                        int statusInArrayNum = getContainPosition(transStatus,dividedArrays);//转换状态属于的集合序号

                        if(statusInArrayNum == -1){//必须进行划分，这个时候虽然没有转换结果，所以需要将集合序号人为设置一个唯一的数
                            arrayNumVector.add(statusInArrayNum);
                        }else{
                            if (!isContain(statusInArrayNum,arrayNumVector)){//防止集合序号的重复
                                arrayNumVector.add(statusInArrayNum);//将集合序号加入到集合序号数组中
                            }
                        }
                        statusMap.add(new Pair(statusInArrayNum,dividedArrays.get(j).getKey().get(k)));//将集合序号————对于的DFA状态组压入
                    }

                    if (arrayNumVector.size() == 1){
                        canNotBeDivided ++ ;
                        continue;
                    }else{
                        for (int m = 0; m < arrayNumVector.size(); ++m) {
                        }

                        for (int l = 0; l <  arrayNumVector.size(); ++l) {//进行划分
                            Vector<Integer> tempArray = new Vector<>();
                            for (int k = 0; k < statusMap.size(); ++k) {
                                if (arrayNumVector.get(l) == -1 && statusMap.get(k).getKey() == -1){//key为-1.说明是一定要划分的
                                    //删除该元素
                                    statusMap.set(k,new Pair<>(statusMap.get(k).getKey(),-2));//-2代表删除状态
                                    tempArray.add(statusMap.get(k).getValue());
                                    break;
                                } else{
                                    if (statusMap.get(k).getKey() == arrayNumVector.get(l)){//根据集合序号进行划分
                                        tempArray.add(statusMap.get(k).getValue());
                                    }
                                }

                            }
                            dividedArrays.add(new Pair<>(tempArray, true));
                        }
                        dividedArrays.remove(j);
                        j--;
                        break;//当前集合结束，调到下一个位置的集合，因为删除了该元素，其他元素前移一位,所以j--
                    }
                }
                if (canNotBeDivided == alphabet.size()){
                    dividedArrays.set(j,new Pair<>(dividedArrays.get(j).getKey(),false));//如果一个集合经过转换后还是该集合本身，该集合无需再进行划分
                }
            }

            //判断是否结束循环，如果划分集合下面的所有集合都不可划分就退出循环
            flag = false;
            for (Pair<Vector<Integer>, Boolean> dividedArray : dividedArrays) {
                if (dividedArray.getValue()) {
                    flag = true;
                    break;
                }
            }
        }

        //合并DFA等价状态
        for (Pair<Vector<Integer>, Boolean> dividedArray : dividedArrays) {
            if (dividedArray.getKey().size() > 1) {//只要每个集合的大小大于1，说明有需要合并的
                int represent = dividedArray.getKey().get(0);
                for (int i = 1; i < dividedArray.getKey().size(); ++i) {//除了第一个节点，其他节点都和第一个节点合并
                    mergeTwoNode(represent, dividedArray.getKey().get(i));//合并这两个节点
                }
            }
        }
    }

    @Override
    public boolean isInDFAEndStatus(int i) {
        for (int j = 0; j < lexDFA.getEndStatus().size(); ++j) {
            if (i == lexDFA.getEndStatus().get(j)){
                return true;
            }
        }
        return false;

    }

    @Override
    public void mergeTwoNode(int a, int b) {
        for (int i = 1; i < lexDFA.mVexs.size()+1 ; ++i) {
            if (i == b){
                for (int j = 1; j < lexDFA.mVexs.size()+1 ; ++j) {
                    if (lexDFA.dfaGraph.getEdgeValue(b,j).get(0) != '^'){
                        if (j == b){
                            lexDFA.dfaGraph.addEdge(a,a,lexDFA.dfaGraph.getEdgeValue(b,j).get(0));
                        } else{
                            lexDFA.dfaGraph.addEdge(a,j,lexDFA.dfaGraph.getEdgeValue(b,j).get(0));
                        }
                        lexDFA.dfaGraph.deleteEdge(b,j);
                        lexDFA.mVexs.set(b,new Vector<>());
                    }
                }
            } else{
                for (int j = 1; j < lexDFA.mVexs.size() + 1; ++j) {
                    if (j == b && lexDFA.dfaGraph.getEdgeValue(i,b).get(0)!='^'){
                        lexDFA.dfaGraph.addEdge(i,a,lexDFA.dfaGraph.getEdgeValue(i,b).get(0));
                        lexDFA.dfaGraph.deleteEdge(i,j);
                        lexDFA.mVexs.set(b,new Vector<>());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String generateCCode(MyGraph myGraph) {
        String tag1 ="   ";
        String tag2 ="      ";
        String tag3 ="         ";
        StringBuilder text= new StringBuilder("#include<stdio.h> \r\n\r\nint main(){\r\n"
                + tag1 + "int stateID = 1;\r\n"
                + tag1 + "int toexit = 0;\r\n"
                + tag1 + "while(!toexit){\r\n"
                + tag2 + "char ch = gettoken();\r\n"
                + tag2 + "switch(stateID){          //不可变部分\r\n\r\n"
                + tag3 + "//可变部分\r\n");
        StringBuilder result = new StringBuilder();

        int num = myGraph.vmVexNum+1;
        for(int i = 1; i<num; i ++){
            int flag_case=1;
            int flag_else=0;
            int flag_elseif = 0;
            for(int j = 0; j<num; j++){
                if (myGraph.getEdgeValue(i,j).get(0) != '^'){
                    if(flag_case == 1){
                        result.append(tag3 + "case " + i + ":\r\n");
                        flag_case = 0;
                        flag_else++;
                    }
                    for (int k = 0; k <myGraph.getEdgeValue(i,j).size() ; ++k) {
                        //result += tag3 + "\"" +to_string(i) + "\"" + " -> \"" + to_string(j) +
                        //"\"[label=\"" + myGraph.getEdgeValue(i,j).at(k) + "\"]\n";
                        if(flag_elseif==0){
                            result.append(tag3 + "if(ch == " + myGraph.getEdgeValue(i,j).get(k) + ") stateID = "
                                    + j + ";\r\n");
                            flag_elseif ++;
                        }
                        else{
                            result.append(tag3 + "else if(ch == " + myGraph.getEdgeValue(i,j).get(k) + ") stateID = "
                                    + j + ";\r\n");
                        }

                    }
                }
                if(flag_else!=0 && j == num-1){
                    result.append(tag3 + "else toexit = 1;\r\n"
                            + tag3 + "break;\r\n\r\n");
                }
            }
        }
        text.append(result).append(tag3).append("default:\r\n").append(tag3).append("toexit = 1;\r\n").append(tag3).append("break;\r\n");
        text.append(tag2 + "}\r\n"
                + tag1 + "}\r\n"+
                "}\r\n");



        //将c语言词法分析程序写进文件
/*        QString text1 = QString::fromStdString(text);
        if(!text1.isEmpty()){
            //文件对象
            QFile file("D:\\qt\\Experiment_2\\XLEX_Generaor\\cCode.txt");

            //打开文件，只写
            bool isok = file.open(QIODevice::WriteOnly);
            if(isok == true){
                file.write(text1.toStdString().data());
                cout<<text1.toStdString()<<endl;
            }
            file.close();
        }*/
        return text.toString();


    }
    // 最小化DFA
    public boolean isContain(int a, Vector<Integer> b){
        for (Integer integer : b) {
            if (a == integer)
                return true;
        }
        return false;
    }
    //判断某个DFA状态在划分的集合中的序号
    public int getContainPosition(int a, Vector<Pair<Vector<Integer>,Boolean>> b) {

        for (int i = 0; i <b.size() ; ++i) {
            for (int j = 0; j <b.get(i).getKey().size() ; ++j) {
                if (a == b.get(i).getKey().get(j)){
                    return i;
                }
            }
        }
        return -1;
    }


}
