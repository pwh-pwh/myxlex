package com.coderpwh.pwhxlex_generaor;

import com.coderpwh.pwhxlex_generaor.controller.GeneratorFile;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class HelloController {
    @FXML
    private Label label_top;
    @FXML
    private Button dotbtn;
    @FXML
    private TextField dotpath;
    @FXML
    private Button filepathbtn;
    @FXML
    private TextField filepath;
    @FXML
    private Button createbtn;
    @FXML
    private TextField expression;
    @FXML
    private Tab nfatab;

    @FXML
    private Tab dfatab;
    @FXML
    private Tab mindfatab;
    @FXML
    private Tab codetab;
    private GeneratorFile gf = new GeneratorFile();
    private Map<String,Object> result = null;


    @FXML
    public void onClickLabel() {
        System.out.println("onclick label");
        System.out.println(label_top.getText());
    }
    @FXML
    public void create() throws FileNotFoundException {
        String text = expression.getText();
        System.out.println("create");
        result = gf.generator(text);
        System.out.println(gf.getSourcePath());
        createCode();
        createNFA();
        createDFA();
        createMinDFA();
    }

    public void createMinDFA() throws FileNotFoundException {
        ImageView imageView = new ImageView();
        File nfaImg = (File)result.get("mindfa");
        System.out.println("nfaImg path is "+nfaImg.getPath());
        Image image = new Image(new FileInputStream(nfaImg));
        imageView.setImage(image);
        mindfatab.setContent(imageView);
    }
    public void createDFA() throws FileNotFoundException {
        ImageView imageView = new ImageView();
        File nfaImg = (File)result.get("dfa");
        System.out.println("nfaImg path is "+nfaImg.getPath());
        Image image = new Image(new FileInputStream(nfaImg));
        imageView.setImage(image);
        dfatab.setContent(imageView);
    }

    public void createNFA() throws FileNotFoundException {
        ImageView imageView = new ImageView();
        File nfaImg = (File)result.get("nfa");
        System.out.println("nfaImg path is "+nfaImg.getPath());
        Image image = new Image(new FileInputStream(nfaImg));
        imageView.setImage(image);
        nfatab.setContent(imageView);
    }
    public void createCode() {
        TextArea cCode = new TextArea();
        cCode.setFont(new Font(20));
        cCode.setText((String)result.get("code"));
        codetab.setContent(cCode);
    }

    @FXML
    public void setDotpath() {
        String text = dotpath.getText();
        System.out.println("set dot path:"+text);
        if(text!=null&&text.length()>0) {
            gf.setDotPath(text);
        }
    }
    @FXML
    public void setFilepath() {
        String text = filepath.getText();
        System.out.println("set filepath:"+text);
        if(text!=null&&text.length()>0) {
            gf.setSourcePath(text);
        }
    }


}