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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;
import java.util.function.Consumer;
import jpfm.FileAttributesProvider;
import jpfm.MountListener;
import jpfm.fs.SimpleReadOnlyFileSystem;
import jpfm.mount.Mount;
import jpfm.mount.MountParams;
import jpfm.mount.MountParamsBuilder;
import jpfm.mount.Mounts;
import jpfm.volume.RealFile;
import jpfm.volume.vector.VectorDirectory;
import jpfm.volume.vector.VectorNode;
import jpfm.volume.vector.VectorRootDirectory;

/**
 *
 * @author Shashank
 */
public class SpyFS{

    private final VectorRootDirectory vrd = new VectorRootDirectory();
    private final SimpleReadOnlyFileSystem sfs = new SimpleReadOnlyFileSystem(vrd);
    private final Path storeTo;
    
    private long totalFiles = 0, totalDirectories = 0;

    public SpyFS(Path storeTo) {
        this.storeTo = storeTo;
    }
    
    public static SpyFSController work(final Settings s,Consumer<String> cntr)throws Exception{
        
        final SpyFS fs = new SpyFS(Paths.get(s.destinationPath()));
        deleteC(Paths.get(s.destinationPath()));
        fs.fill(Paths.get(s.sourcePath()),cntr);
        MountListener.WaitingMountListener l = new MountListener.WaitingMountListener();

        final Mount mount = Mounts.mount(new MountParamsBuilder()
                .set(MountParams.ParamType.LISTENER, l)
                .set(MountParams.ParamType.EXIT_ON_UNMOUNT, false)
                .set(MountParams.ParamType.FILE_SYSTEM, fs.sfs)
                .set(MountParams.ParamType.MOUNT_LOCATION,s.virtualLocation())
                .build()
        );
        return new SpyFSController() {
            @Override public void unmount(Consumer<String> status) {
                try{mount.unMount();}catch(Exception a){
                    status.accept(a.getLocalizedMessage());
                    a.printStackTrace();
                }
            }

            @Override public void ejectCopy(Consumer<String> status) {
                try{
                    final PrintWriter pw = new PrintWriter(s.reportPath());
                    walkNode(fs.vrd,status,  (s)->{
                        try{pw.println(s);}
                        catch(Exception a){
                            System.out.println("err in writing -> "+s);
                        }});
                    pw.close();
                }
                catch(Exception a){
                    a.printStackTrace();
                }
            }

            @Override public long totalFiles() {return fs.totalFiles;}
            @Override public long totalFilesAndDirectories() {return totalFiles()+totalDirectories();}
            @Override public long totalDirectories() {return fs.totalDirectories;}
            
        };
    }
    
    private static void deleteC(Path c){
        System.out.println("Clearing "+c);
        try{
            Files.walkFileTree(c, new FileVisitor<Path>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file); 
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if(dir!=c) // don't delete root
                        Files.delete(dir); 
                    return FileVisitResult.CONTINUE;
                }
            });
        }catch(Exception a){
            a.printStackTrace();
        }
    }

    private void fill(final Path p,final Consumer<String> cx) throws IOException {
        Files.walkFileTree(p, new FileVisitor<Path>() {
            private VectorNode currentDir = vrd;
            private final Stack<VectorNode> directoryStack = new Stack<>();

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(p)) {
                    return FileVisitResult.CONTINUE;
                }
                cx.accept(p.relativize(dir).toString());
                totalDirectories++;
                
                VectorDirectory vd = new VectorDirectory(dir.getFileName().toString(), currentDir);
                currentDir.add(vd);
                directoryStack.add(currentDir);
                currentDir = vd;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isDirectory(file)) {
                    return FileVisitResult.CONTINUE;
                }
                cx.accept(p.relativize(file).toString());
                totalFiles++;
                
                RealFile rf = new SpiedFile(file, currentDir, storeTo.resolve(p.relativize(file)) );
                currentDir.add(rf);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.out.println("failed to visit " + file);
                exc.printStackTrace();
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (dir.equals(p)) {
                    return FileVisitResult.CONTINUE;
                }
                currentDir = directoryStack.pop();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void walkNode(VectorNode vn,Consumer<String> status,Consumer<String> writer) throws IOException {
        printNode(vn,writer);
        for (FileAttributesProvider fap : vn) {
            if (fap instanceof SpiedFile) {
                SpiedFile sf = (SpiedFile) fap;
                sf.export();
            } else if (fap instanceof VectorNode) {
                walkNode(((VectorNode) fap),status,writer);
            }
        }
    }

    private static void printNode(VectorNode vn,Consumer<String> writer) throws IOException {
        for (FileAttributesProvider fap : vn) {
            if (fap instanceof SpiedFile) {
                SpiedFile sf = (SpiedFile) fap;
                writer.accept(
                    sf.getDest() + "\t" + sf.getFileSize() + "\t" + sf.p() + "\t"
                        + (sf.opened() ? "opened" : "untouched")
                );
            } else if (fap instanceof VectorNode) {
                printNode(((VectorNode) fap),writer);
            }
        }
    }



    
    
    
}
