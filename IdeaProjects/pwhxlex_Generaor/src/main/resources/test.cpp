#include "mylex.h"

#include <iostream>
#include <string>
#include <vector>
#include<fstream>
#include<iomanip>
#include <QProcess>
#include <sstream>
#include "QDebug"
#include "QString"
#include "QFile"
#include "QByteArray"

using namespace std;
void Lex::getDFA() {
    //给DFA节点开头位置加一个空元素占位
    lexDFA.mVexs.emplace_back(0);

    vector<int>  initStatus;
    initStatus.push_back(lexNFA.startStatus);
    vector<int>    initStatusTrans(e_closure(vector<int>(initStatus)));

    //给DFA创建第一个节点
    int newDFAStartPoint = lexDFA.DFAGraph.mVexNum + 1;
    lexDFA.DFAGraph.mVexNum ++;//节点数加1

    lexDFA.startStatus = newDFAStartPoint;

    stack<int> DFAStatusStack;//存储还没有经过字母表转换的DFA状态, DFA状态栈,这个栈只需要存储DFA的序号就可以了，没存储DFA节点对应的NFA集合，
    DFAStatusStack.push(newDFAStartPoint);

    lexDFA.mVexs.push_back(initStatusTrans);//NFA起点的E转换集合赋值给DFA的第一个节点

    while (!DFAStatusStack.empty()){

        int topDFAStack = DFAStatusStack.top();
        DFAStatusStack.pop();

        for (int i = 0; i < alphabet.size() ; ++i) { //对字母表的每个元素都需要作为转换条件

            vector<int> tempArray = e_closure(nfaMove(lexDFA.mVexs[topDFAStack],alphabet[i]));

            if (tempArray.empty()){//如果转换产生的NFA集合为空，跳过该次转换即可
                continue;
            }
            int position = isDFAStatusRepeat(tempArray);//判断新生成DFA状态是否已经存在了，如果已经存在，返回该状态在节点的位置

            if (position == -1){//这个是新生成的DFA状态，没有重复
                int tempDFAStatusNode = lexDFA.DFAGraph.mVexNum + 1;
                lexDFA.DFAGraph.mVexNum ++;//节点数加1
                if (isEndDFAStatus(tempArray)){
                    lexDFA.endStatus.push_back(tempDFAStatusNode);
                }
                lexDFA.mVexs.push_back(tempArray);//把NFA集合赋值给DFA的状态

                DFAStatusStack.push(tempDFAStatusNode);//把新产生的DFA状态加入到DFA栈中
                position = tempDFAStatusNode;
            }

            //连接节点，产生边
            lexDFA.DFAGraph.addEdge(topDFAStack,position,alphabet[i]);
            lexDFA.DFAGraph.mEdgeNum ++;
        }
    }
}

//判断是否是DFA的终止状态，只要包含了NFA的终止状态的DFA状态都是终止状态
bool Lex::isEndDFAStatus(vector<int> nfaArray){
    for (int i = 0; i < nfaArray.size(); ++i) {
        if (lexNFA.endStatus == nfaArray[i]){
            return true;
        }
    }
    return false;
}


//判断新产生的DFA状态是否已经存在DFA状态表中
int Lex::isDFAStatusRepeat(vector<int> a){
    int position = -1;
    for (int i = 1; i < lexDFA.mVexs.size()+1; ++i) {
        if (a == lexDFA.mVexs[i]){
            position = i;
            break;
        }
    }

    return position;
}

//DFA:能够从NFA的状态数组statusArray中的某个状态s开始只通过E转换到达的NFA状态集合
vector<int> Lex::e_closure(vector<int> statusArray){
    for (int k = 0; k < statusArray.size(); ++k) {
        cout << statusArray[k] << " ";
    }
    cout << endl;

    vector<int> resultsArray; //存放状态数组的E转换集合
    stack<int> statusStack; //存放递归过程中的状态，当该栈为空的时候，递归结束

    for (int i = 0; i < statusArray.size(); ++i) {
        statusStack.push(statusArray[i]); //初始化状态栈
        resultsArray.push_back(statusArray[i]); //状态本身也可以通过E转换到达本身，所以需要将自身添加到结果数组中
    }

    while(!statusStack.empty()){
        int status = statusStack.top();
        statusStack.pop();

        for (int i = 1; i < lexNFA.mVexs.size() + 1 ; ++i) {
            if (i == status){
                for(int j = 1;j<lexNFA.mVexs.size()+1;j++){
                    if (lexNFA.NFAGraph.getEdgeValue(i,j).at(0) == 'E'){ //找到转移条件为E的的终点
                        statusStack.push(j); //加入到状态栈中
                        resultsArray.push_back(j); //加入到结果数组中
                    }
                }
            }
        }
    }

    /*去除重复元素，unique()只能去除相邻的重复元素，所有先排序再去重*/
    sort(resultsArray.begin(),resultsArray.end());
    resultsArray.erase(unique(resultsArray.begin(), resultsArray.end()), resultsArray.end());

    cout << "ecolosure转换结果为：" << endl;

    for (int k = 0; k < resultsArray.size(); ++k) {
        cout << resultsArray[k] << " ";
    }
    cout << endl;
    return resultsArray;

}

//DFA: 能够从NFA的状态数组statusArray中的某个状态出发，通过条件为condition转换到达的NFA状态集合
vector<int> Lex::nfaMove(vector<int> statusArray,char condition){
    for (int k = 0; k < statusArray.size(); ++k) {
        cout << statusArray[k] << " " ;
    }
    cout << endl;
    vector<int>resultsArray; //结果集合
    stack<int> statusStack; //状态栈

    for (int j = 0; j < statusArray.size() ; ++j) {
        statusStack.push(statusArray[j]);
    }

    while(!statusStack.empty()){
        int status = statusStack.top();
        statusStack.pop();
        for (int i = 1; i <lexNFA.mVexs.size()+1 ; ++i) {
            if (i == status){
                for (int j = 1; j < lexNFA.mVexs.size() + 1; ++j) {
                    if (lexNFA.NFAGraph.getEdgeValue(i,j).at(0) == condition){
                        resultsArray.push_back(j); //把终点加入到结果集合中，但此时不需要再将终点压入到状态栈中
                    }
                }
            }
        }
    }

    /*去除重复元素，unique()只能去除相邻的重复元素，所有先排序再去重*/
    sort(resultsArray.begin(),resultsArray.end());
    resultsArray.erase(unique(resultsArray.begin(), resultsArray.end()), resultsArray.end());

    cout << "move结果为:" << endl;
    for (int l = 0; l < resultsArray.size(); ++l) {
        cout << resultsArray[l] << " ";
    }
    cout << endl;
    return resultsArray;
}

string Lex::generateNFADotString(MyGraph myGraph){
    string tab = "    ";
    string result;
    result = "digraph G{\n" + tab + "\"\"[shape=none]\n";
    for (int i = 1; i <lexNFA.mVexs.size() + 1 ; ++i) {
        if (lexNFA.mVexs[i] != -1)
        {
            result += tab + "\"" + to_string(i) +  "\"";
            if (i == lexNFA.endStatus){
                result += "[shape=doublecircle]\n";
            }else{
                result += "[shape=circle]\n";
            };
        }
    }

    result += "\n";
    result += tab + "\"\"->\"" + to_string(lexNFA.startStatus)+ "\"\n";

    int num = lexNFA.mVexs.size()  + 1;
    for(int i = 1 ;i<num  ;i ++){
        for(int j = 1;j<num;j++){
            if (myGraph.getEdgeValue(i,j).at(0) != '^'){
                result += tab + "\"" +to_string(i) + "\"" + " -> " + to_string(j) + "[label=\"" + myGraph.getEdgeValue(i,j).at(0) + "\"]\n";
            }
        }
    }
    result.append("}");

    ofstream ofile; //定义输出文件
    ofile.open(projectFile + string("\\dots\\nfa.dot"));

    ofile << result << endl;
    ofile.close();

    //执行生成nfa图片
    string temp = dot + "\\dot -Tjpg " + projectFile +"\\dots\\nfa.dot -o " + projectFile + "\\images\\nfa.jpg";
    const char *systemString = temp.c_str();

    cout << "命令行为" + temp << endl;
    system(systemString);//调用QT里的函数
    return  result;

}

string Lex::generateDFADotString(MyGraph myGraph,int choice){
    string tab = "    ";
    string result;
    result = "digraph G{\n" + tab + "\"\"[shape=none]\n";
    for (int i = 1; i <myGraph.mVexNum + 1 ; ++i) {

        if (!lexDFA.mVexs[i].empty()){
            result += tab + "\"" + to_string(i) +  "\"";
            bool flag = true;

            for (int j = 0; j < lexDFA.endStatus.size(); ++j) {
                if (i == lexDFA.endStatus[j]){
                    flag = false;
                    break;
                }
            }
            if (!flag){
                result += "[shape=doublecircle]\n";
            }else{
                result += "[shape=circle]\n";
            };
        }
    }

    result += "\n";
    result += tab + "\"\"->\"" + to_string(lexDFA.startStatus)+ "\"\n";

    int num = myGraph.mVexNum + 1;
    for(int i = 0 ;i<num  ;i ++){
        for(int j = 0;j<num;j++){
            if (myGraph.getEdgeValue(i,j).at(0) != '^'){
                for (int k = 0; k <myGraph.getEdgeValue(i,j).size() ; ++k) {
                    result += tab + "\"" +to_string(i) + "\"" + " -> \"" + to_string(j) + "\"[label=\"" + myGraph.getEdgeValue(i,j).at(k) + "\"]\n";
                }
            }
        }
    }
    result.append("}");
    ofstream ofile;//定义输出文件
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

    system(systemString);
    return result;

}

// 最小化DFA
bool isContain(int a, vector<int> b){
    for (int i = 0; i <b.size() ; ++i) {
        if (a == b[i])
            return true;
    }
    return false;
}

//判断某个DFA状态在划分的集合中的序号
int getContainPosition(int a, vector<pair<vector<int>,bool>> b) {
    for (int i = 0; i <b.size() ; ++i) {
        for (int j = 0; j <b[i].first.size() ; ++j) {
            if (a == b[i].first[j]){
                return i;
            }
        }
    }
    return -1;
}

void Lex::minimizeDFA() {
    vector<int> noEndPointArray;//非终止态节点集合
    vector<int> endPointArray(lexDFA.endStatus);

    //非终止状态集合
    for (int i = 1; i < lexDFA.mVexs.size(); ++i) {
        if (!isInDFAEndStatus(i)){
            noEndPointArray.push_back(i);
        }
    }//初始化非终止节点集合
    cout << endl;

    //终止状态集合
    for (int n = 0; n < endPointArray.size(); ++n) {
        cout << endPointArray[n] << " ";
    }
    cout << endl;
    vector<pair<vector<int>,bool>> dividedArrays;//first存储的是划分的集合，second存储的是该划分集合是否可继续划分
    dividedArrays.emplace_back(noEndPointArray, true);
    dividedArrays.emplace_back(endPointArray, true);

    bool flag = true;
    while(flag){
        for (int j = 0; j < dividedArrays.size(); ++j) {//对划分的每个集合进行操作
            cout << endl;
            int canNotBeDivided = 0;//经过一次字母表的转换，如果该集合的转换状态只有一个，说明该集合不能被该字母区分，该变量+1
            if (dividedArrays[j].first.size() == 1){
                dividedArrays[j].second = false;//如果集合元素只有一个，赋值为false，即不可再划分
                continue;
            }
            for (int i = 0; i < alphabet.size() ; ++i) {
                for (int m = 0; m < dividedArrays[j].first.size(); ++m) {
                    cout << dividedArrays[j].first[m] << " ";
                }

                cout << "当前字母为" << alphabet[i] << endl;
                vector<int> arrayNumVector;//存放DFA状态经过某个字母转换到的集合序号的数组

                //first 为转换状态属于的集合序号，second DFA起点的状态节点
                vector<pair<int,int>> statusMap;//存放了每个节点的转换后属于的集合序号——该节点本身序号

                for (int k = 0; k < dividedArrays[j].first.size(); ++k) { //获取到该集合的每个元素的转换状态属于的集合序号
                    int transStatus = lexDFA.getTargetStatus(dividedArrays[j].first[k],alphabet[i]);//获取节点的转换DFA节点

                    int statusInArrayNum = getContainPosition(transStatus,dividedArrays);//转换状态属于的集合序号

                    if(statusInArrayNum == -1){//必须进行划分，这个时候虽然没有转换结果，所以需要将集合序号人为设置一个唯一的数
                        statusInArrayNum = -1;
                        arrayNumVector.push_back(statusInArrayNum);
                    }else{
                        if (!isContain(statusInArrayNum,arrayNumVector)){//防止集合序号的重复
                            arrayNumVector.push_back(statusInArrayNum);//将集合序号加入到集合序号数组中
                        }
                    }
                    statusMap.emplace_back(statusInArrayNum,dividedArrays[j].first[k]);//将集合序号————对于的DFA状态组压入
                }

                if (arrayNumVector.size() == 1){
                    canNotBeDivided ++ ;
                    continue;
                }else{
                    for (int m = 0; m < arrayNumVector.size(); ++m) {
                    }

                    for (int l = 0; l <  arrayNumVector.size(); ++l) {//进行划分
                        vector<int> tempArray;
                        for (int k = 0; k < statusMap.size(); ++k) {
                            if (arrayNumVector[l] == -1 && statusMap[k].first == -1){//key为-1.说明是一定要划分的
                                //删除该元素
                                statusMap[k].first = -2;//-2代表删除状态
                                tempArray.push_back(statusMap[k].second);
                                break;
                            } else{
                                if (statusMap[k].first == arrayNumVector[l]){//根据集合序号进行划分
                                    tempArray.push_back(statusMap[k].second);
                                }
                            }

                        }
                        cout << endl;
                        dividedArrays.emplace_back(tempArray, true);
                    }

                    auto iter =  dividedArrays.begin()+j;
                    dividedArrays.erase(iter);

                    j--;
                    break;//当前集合结束，调到下一个位置的集合，因为删除了该元素，其他元素前移一位,所以j--
                }
            }
            if (canNotBeDivided == alphabet.size()){
                dividedArrays[j].second = false;//如果一个集合经过转换后还是该集合本身，该集合无需再进行划分
            }
        }

        //判断是否结束循环，如果划分集合下面的所有集合都不可划分就退出循环
        flag = false;
        for (int m = 0; m < dividedArrays.size(); ++m) {
            if (dividedArrays[m].second == true){
                flag = true;
                break;
            }
        }
    }

    //合并DFA等价状态
    for (int j1 = 0; j1 < dividedArrays.size(); ++j1) {
        if (dividedArrays[j1].first.size() > 1){//只要每个集合的大小大于1，说明有需要合并的
            int represent = dividedArrays[j1].first[0];
            for (int i = 1; i < dividedArrays[j1].first.size(); ++i) {//除了第一个节点，其他节点都和第一个节点合并
                mergeTwoNode(represent,dividedArrays[j1].first[i]);//合并这两个节点
            }
        }
    }
}

//生成c语言词法分析程序
void Lex::generateCCode(MyGraph myGraph){

    string tag1 ="   ";
    string tag2 ="      ";
    string tag3 ="         ";
    string text="#include<stdio.h> \r\n\r\nint main(){\r\n"
                + tag1 + "int stateID = 1;\r\n"
                + tag1 + "int toexit = 0;\r\n"
                + tag1 + "while(!toexit){\r\n"
                + tag2 + "char ch = gettoken();\r\n"
                + tag2 + "switch(stateID){          //不可变部分\r\n\r\n"
                + tag3 + "//可变部分\r\n";

    string result = "";
    int num = myGraph.mVexNum+1;
    for(int i = 1; i<num; i ++){
        int flag_case=1;
        int flag_else=0;
        int flag_elseif = 0;
        for(int j = 0; j<num; j++){
            if (myGraph.getEdgeValue(i,j).at(0) != '^'){
                if(flag_case = 1){
                    result += tag3 + "case " + to_string(i) + ":\r\n";
                    flag_case = 0;
                    flag_else++;
                }
                for (int k = 0; k <myGraph.getEdgeValue(i,j).size() ; ++k) {
                    //result += tag3 + "\"" +to_string(i) + "\"" + " -> \"" + to_string(j) +
                    //"\"[label=\"" + myGraph.getEdgeValue(i,j).at(k) + "\"]\n";
                    if(flag_elseif==0){
                        result += tag3 + "if(ch == " + myGraph.getEdgeValue(i,j).at(k) + ") stateID = "
                                + to_string(j) + ";\r\n";
                        flag_elseif ++;
                    }
                    else{
                        result += tag3 + "else if(ch == " + myGraph.getEdgeValue(i,j).at(k) + ") stateID = "
                                + to_string(j) + ";\r\n";
                    }

                }
            }
            if(flag_else!=0 && j == num-1){
                result += tag3 + "else toexit = 1;\r\n"
                          + tag3 + "break;\r\n\r\n";
            }
        }
    }

    text += result + tag3 + string("default:\r\n") + tag3 + "toexit = 1;\r\n" + tag3 +"break;\r\n";

    text += tag2 + "}\r\n"
            + tag1 + "}\r\n"
            "}\r\n";

    //将c语言词法分析程序写进文件
    QString text1 = QString::fromStdString(text);
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
    }
}

//获取DFA的目标节点
int DFA::getTargetStatus(int node, char condition) {
    for (int i = 0; i < mVexs.size() ; ++i) {
        if (DFAGraph.getEdgeValue(node,i).at(0) == condition){
            return i;
        }
    }
    return -1;//表示目标节点不存在
}

void Lex::mergeTwoNode(int a,int b){
    for (int i = 1; i < lexDFA.mVexs.size()+1 ; ++i) {
        if (i == b){
            for (int j = 1; j < lexDFA.mVexs.size()+1 ; ++j) {
                if (lexDFA.DFAGraph.getEdgeValue(b,j).at(0) != '^'){
                    if (j == b){
                        lexDFA.DFAGraph.addEdge(a,a,lexDFA.DFAGraph.getEdgeValue(b,j).at(0));
                    } else{
                        lexDFA.DFAGraph.addEdge(a,j,lexDFA.DFAGraph.getEdgeValue(b,j).at(0));
                    }
                    lexDFA.DFAGraph.deleteEdge(b,j);
                    lexDFA.mVexs[b] = vector<int>();
                }
            }
        } else{
            for (int j = 1; j < lexDFA.mVexs.size() + 1; ++j) {
                if (j == b && lexDFA.DFAGraph.getEdgeValue(i,b).at(0)!='^'){
                    lexDFA.DFAGraph.addEdge(i,a,lexDFA.DFAGraph.getEdgeValue(i,b).at(0));
                    lexDFA.DFAGraph.deleteEdge(i,j);
                    lexDFA.mVexs[b] = vector<int>();
                    break;
                }
            }
        }
    }
}

bool Lex::isInDFAEndStatus(int i){
    for (int j = 0; j < lexDFA.endStatus.size(); ++j) {
        if (i == lexDFA.endStatus[j]){
            return true;
        }
    }
    return false;
}
