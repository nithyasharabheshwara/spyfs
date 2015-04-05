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

import java.nio.file.Paths;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import neembuu.rus.Rus;
import neembuu.rus.Rusila;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class A extends Application {
    private final Rus r;

    public A() {
        r = Rusila.create(Paths.get(
                System.getProperty("user.home")).resolve(".spfs"));
    }
    
    FXMLLoader fxmll;
    Parent ui;
    UIController uic;
    
    public static A I(){return a;}
    private static A a;
    
    public Settings getSettingsCopy(){
        HashMap hm  = new HashMap();
        Rus hr = Rusila.create(hm);
        Rusila.copy(r, hr, Settings.class);
        return Rusila.I(hr, Settings.class);
    }
    
    public Settings getSettings(){
        return Rusila.I(r, Settings.class);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        A.a = this;
        
        fxmll = new FXMLLoader(SpyFS.class.getResource("UI.fxml"));
        ui = fxmll.load();
        uic = fxmll.getController();
        uic.setStage(primaryStage);

        Scene scene = new Scene(ui, 600, 320);

        primaryStage.setTitle("SpyFS");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(A.class.getResourceAsStream("spyfs.png")));
        primaryStage.show();
        
        new Thread(()->{
            
            Settings s = getSettingsCopy();
            Platform.runLater(()->{
                uic.dstdir.setText(s.destinationPath());
                uic.srcdir.setText(s.sourcePath());
                uic.virloc.setText(s.virtualLocation());
                uic.reportpth.setText(s.reportPath());
            });
        },"load settings").start();
    }
}
