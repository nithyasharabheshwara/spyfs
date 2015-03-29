/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spyfs;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;
import jpfm.FileAttributesProvider;
import jpfm.MountListener;
import jpfm.fs.SimpleReadOnlyFileSystem;
import jpfm.mount.Mount;
import jpfm.mount.MountParams;
import jpfm.mount.MountParamsBuilder;
import jpfm.mount.Mounts;
import jpfm.volume.RealFile;
import jpfm.volume.RealFileProvider;
import jpfm.volume.vector.VectorDirectory;
import jpfm.volume.vector.VectorNode;
import jpfm.volume.vector.VectorRootDirectory;

/**
 *
 * @author Shashank
 */
public class SpyFS {
    private final VectorRootDirectory vrd = new VectorRootDirectory();
    private final SimpleReadOnlyFileSystem sfs = new SimpleReadOnlyFileSystem(vrd);
    private final Path storeTo;

    public SpyFS(Path storeTo) {
        this.storeTo = storeTo;
    }
    
    private void fill(final Path p)throws IOException{
        Files.walkFileTree(p, new FileVisitor<Path>() {
            private VectorNode currentDir = vrd;
            private final Stack<VectorNode> directoryStack = new Stack<>();

            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if(dir.equals(p)){return FileVisitResult.CONTINUE;}
                VectorDirectory vd = new VectorDirectory(dir.getFileName().toString(), currentDir);
                currentDir.add(vd);
                directoryStack.add(currentDir);
                currentDir = vd;
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(Files.isDirectory(file))return FileVisitResult.CONTINUE;
                RealFile rf = new SpiedFile(file,currentDir,storeTo.resolve(p.relativize(file)));
                currentDir.add(rf);
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.out.println("failed to visit "+file);
                exc.printStackTrace();
                return FileVisitResult.TERMINATE;
            }

            @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if(dir.equals(p)){return FileVisitResult.CONTINUE;}
                currentDir = directoryStack.pop();
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    public static void main(String[] args)throws Exception {
        System.out.println("Usage");
        System.out.println("java -jar spyfs.jar <param1-source> <param2-storeTo> <param3-virtual>");
        System.out.println();
        System.out.println("Example");
        System.out.println("java -jar spyfs.jar \"c:\\Python27\" \"c:\\Python27mini\" \"c:\\Python27Virtual\"");
        System.out.println();
        System.out.println();
        
        String source = "J:\\Python27sa"; 
        String storeTo = "F:\\neembuu\\resources\\minipython\\win";
        String virtual = "F:\\c";
        
        if(args.length >= 3){
            source = checkPath(args[0],source);
            storeTo = checkPath(args[1],storeTo);
            virtual = checkPath(args[2],virtual);
        }
        
        System.out.println("Using configuration .... ");
        System.out.println("Source directory : "+source);
        System.out.println("Directory where useful files will be stored: "+storeTo);
        System.out.println("Mount location of virtual reflection of the source : "+virtual);
        System.out.println();
        SpyFS fs = new SpyFS(Paths.get(storeTo));
        fs.fill(Paths.get(source));

        MountListener.WaitingMountListener
                l = new MountListener.WaitingMountListener();

        //JPfmMount jpm = JPfmMount.mount(fS,mountLocation, l);
        Mount mount = Mounts.mount(new MountParamsBuilder()
                .set(MountParams.ParamType.LISTENER, l)
                //.set(MountParams.ParamType.EXIT_ON_UNMOUNT, false)
                .set(MountParams.ParamType.FILE_SYSTEM, fs.sfs)
                .set(MountParams.ParamType.MOUNT_LOCATION,virtual)
                .build()
        );
        while(true){
            System.out.println("Press enter to initiate a copying of files which were actually used.");
            System.out.println("A report will also be generated, you can copy paste it and analyze it in msexcel.");
            System.in.read();
            walkNode(fs.vrd);
        }
        
        //l.waitUntilUnmounted();
    }
    
    private static String checkPath(String newValue,String oldValue){
        if(Files.exists(Paths.get(newValue)))
            return newValue;
        return oldValue;
    }
    
    private static void walkNode(VectorNode vn)throws IOException{
        printNode(vn);
        for (FileAttributesProvider fap : vn) {
            if(fap instanceof SpiedFile){
                SpiedFile sf = (SpiedFile)fap;
                sf.export();
            }else if(fap instanceof VectorNode){
                walkNode(((VectorNode)fap));
            }
        }
    }
    
    private static void printNode(VectorNode vn)throws IOException{
        for (FileAttributesProvider fap : vn) {
            if(fap instanceof SpiedFile){
                SpiedFile sf = (SpiedFile)fap;
                System.out.println(sf.getDest()+"\t"+sf.getFileSize()+"\t"+sf.p()+"\t"+
                        (sf.opened()?"opened":"untouched") );
            }else if(fap instanceof VectorNode){
                walkNode(((VectorNode)fap));
            }
        }
    }
}
