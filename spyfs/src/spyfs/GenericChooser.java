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

import java.io.File;
import java.util.function.Consumer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public interface GenericChooser {
    void setInitialDirectory(String path);
    String show(final Window ownerWindow);
    void setTitle(String t);
    
    public static void fixPath(String initPath,Consumer<File> x){
        File f = new File(initPath);
        if(!f.exists()){
            f = new File(System.getProperty("user.home"));
        }if(f.exists()){
            x.accept(f);
        }
    }
    
    public static GenericChooser newFileC(){
        return new GenericChooser() {
            final FileChooser fc = new FileChooser();
            @Override
            public void setInitialDirectory(String initPath) {
                fixPath(initPath, (f) -> fc.setInitialDirectory(f));
            }

            @Override
            public void setTitle(String t) {
                fc.setTitle(t);
            }
            
            @Override
            public String show(final Window ownerWindow) {
                return fc.showOpenDialog(ownerWindow).getAbsolutePath();
            }
        };
    }
    
    public static GenericChooser newDirectoryC(){
        return new GenericChooser() {
            final DirectoryChooser ch = new DirectoryChooser();
            @Override
            public void setInitialDirectory(String initPath) {
                fixPath(initPath, (f) -> ch.setInitialDirectory(f));
            }
            
            @Override
            public void setTitle(String t) {
                ch.setTitle(t);
            }

            @Override
            public String show(final Window ownerWindow) {
                return ch.showDialog(ownerWindow).getAbsolutePath();
            }
        };
    }
}
