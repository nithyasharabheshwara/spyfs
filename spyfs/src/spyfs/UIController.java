/* 
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spyfs;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class UIController implements Initializable {

    @FXML
    TextField reportpth;

    @FXML
    private Button exit;

    @FXML
    private Button unmount;

    @FXML
    TextField dstdir;
    
    @FXML
    Label progressLabel;
    
    @FXML
    private Button start;

    @FXML
    TextField virloc;

    @FXML
    private ProgressBar progress;

    @FXML
    TextField srcdir;

    @FXML
    private VBox settingsbox;
    
    @FXML
    private Button copy;
    
    @FXML
    private Label totalfiles;

    private Window stage;

    public void setStage(Window stage) {
        this.stage = stage;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    private void genericBrowse(boolean file, TextField tf, String initPath,Consumer<String> c) {
        Consumer<String> c2 = (s)-> {
            if(!initPath.equalsIgnoreCase(s)){
                c.accept(s);
            }
        };
        try{
            GenericChooser chooser = file?GenericChooser.newFileC():GenericChooser.newDirectoryC();
            chooser.setTitle("Choose "+(file?"File":"Directory"));
            String s = chooser.show(stage);
            c2.accept(s);
            tf.setText(s);
        }catch(Exception a){
            c2.accept(initPath);
            tf.setText(initPath);
        }
    }
    
    
    private Settings s() {return A.I().getSettings();}

    @FXML
    void sb(ActionEvent event) {
        genericBrowse(false,srcdir,s().sourcePath(), (p)->{s().sourcePath(p);} );
    }

    @FXML
    void db(ActionEvent event) {
        genericBrowse(false,dstdir,s().destinationPath(), (p)->{s().destinationPath(p);} );
    }

    @FXML
    void vb(ActionEvent event) {
        genericBrowse(true,virloc,s().virtualLocation(), (p)->{s().virtualLocation(p);} );
    }

    @FXML
    void wb(ActionEvent event) {
        genericBrowse(true,reportpth,s().reportPath(), (p)->{s().reportPath(p);} );
    }

    @FXML
    void homepageb(ActionEvent event) {
        A.I().getHostServices().showDocument("https://github.com/shashaanktulsyan/spyfs");
    }

    volatile SpyFSController sfsc = null;
    
    @FXML
    void startb(ActionEvent event) {
        start.setDisable(true);
        settingsbox.setDisable(true);

        progress.setProgress(0);
        progressLabel.setText("");
        new Thread(()->{
            try{
                sfsc = SpyFS.work(A.I().getSettingsCopy(),progressLabelS());
                String t = "Total = "+sfsc.totalFilesAndDirectories()+" files:"+
                        sfsc.totalFiles()+" dirs:"+sfsc.totalDirectories();
                Platform.runLater(()-> {
                    totalfiles.setText(t);
                    copy.setDisable(false);
                    unmount.setDisable(false);
                });
            }catch(Exception a){
                a.printStackTrace();
            }
        },"VFS").start();
    }

    @FXML
    void exitb(ActionEvent event) {
        System.exit(0);
    }
    
    @FXML
    void unmount(ActionEvent event) {
        unmount.setDisable(true);
        sfsc.unmount((s)->progressLabel.setText(s));
        try{
            dump(event);
        }catch(Exception a){
            a.printStackTrace();
        }
        System.exit(0);
    }
    
    private Consumer<String> progressLabelS(){
        return (s)->{
            Platform.runLater(()-> {progressLabel.setText(s);});
        };
    }
    
    @FXML
    void dump(ActionEvent event) {
        progress.setProgress(0d);
        progressLabel.setText("");
        Consumer<String> cx = new Consumer<String>(){
            volatile long prg = 0;
            @Override public void accept(String s) {
                prg++;
                double d = 1d*prg/(sfsc.totalFilesAndDirectories());
                Platform.runLater(()-> {
                    progressLabel.setText(s);
                    progress.setProgress(d);
                });
            }  
        };
        copy.setDisable(true);
        new Thread(()->{
            sfsc.ejectCopy(cx);
            Platform.runLater(()-> {
                progress.setProgress(1d);
                progressLabel.setText("");
                copy.setDisable(false);
            });
        },"Copying").start();
    }
}
