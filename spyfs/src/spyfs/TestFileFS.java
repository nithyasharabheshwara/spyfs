/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spyfs;

import jpfm.DirectoryStream;
import jpfm.FileAttributesProvider;
import jpfm.FileId;
import jpfm.MountListener;
import jpfm.fs.BasicCascadableProvider;
import jpfm.fs.BasicFileSystem;
import jpfm.fs.InfinitelyDeepFS;
import jpfm.fs.Type;
import jpfm.mount.BasicCascadeMount;
import jpfm.mount.Mount;
import jpfm.mount.MountParams;
import jpfm.mount.MountParamsBuilder;
import jpfm.mount.Mounts;
import jpfm.operations.Read;
import jpfm.volume.RealFile;
import jpfm.volume.RealFileProvider;

/**
 *
 * @author Shashank
 */
public class TestFileFS implements BasicFileSystem{
    private final RealFile rf;

    public TestFileFS(String pth)throws Exception {
        this.rf = RealFileProvider.getNonBlockingRealFile(pth);
    }
    
    @Override
    public FileAttributesProvider open(String[] filePath) {
        System.out.println(filePath);
        if(filePath!=null){
            for (String string : filePath) {
                System.out.println(string);
            }
        }
        return null;
    }

    @Override
    public void open(FileAttributesProvider fap) {
        rf.open();
    }

    @Override
    public FileAttributesProvider getFileAttributes(FileId fileDescriptor) {
        if(!rf.getFileDescriptor().implies(fileDescriptor))return null;
        return rf;
    }

    @Override
    public DirectoryStream list(FileId folderToList) {
        System.out.println("list "+folderToList);
        return null;
    }

    @Override
    public void close(FileId file) {
        if(!rf.getFileDescriptor().implies(file))return;
        rf.close();
    }

    @Override
    public void delete(FileId fileToDelete) {
        
    }

    @Override
    public long capacity() {
        return rf.getFileSize();
    }

    @Override
    public void read(Read read) throws Exception {
        System.out.println("read"+read);
        rf.read(read);
    }

    @Override
    public FileAttributesProvider getRootAttributes() {
        return rf;
    }

    @Override
    public BasicCascadeMount cascadeMount(BasicCascadableProvider basicCascadable) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Type.BASIC getType() {
        return null;
    }
    
    public static void main(String[] args)throws Exception {
        TestFileFS tffs = new TestFileFS("J:\\Nithyananda\\108pearls-book.pdf");

        MountListener.WaitingMountListener
                l = new MountListener.WaitingMountListener();

        //JPfmMount jpm = JPfmMount.mount(fS,mountLocation, l);
        Mount mount = Mounts.mount(new MountParamsBuilder()
                .set(MountParams.ParamType.LISTENER, l)
                //.set(MountParams.ParamType.EXIT_ON_UNMOUNT, false)
                .set(MountParams.ParamType.FILE_SYSTEM, tffs)
                .set(MountParams.ParamType.MOUNT_LOCATION,"F:\\mm.pdf")
                .build()
        );

        l.waitUntilUnmounted();
    }
    
}
