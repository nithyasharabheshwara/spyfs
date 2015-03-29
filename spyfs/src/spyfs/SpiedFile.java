/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spyfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;
import jpfm.AccessLevel;
import jpfm.DirectoryStream;
import jpfm.FileDescriptor;
import jpfm.FileFlags;
import jpfm.FileId;
import jpfm.FileType;
import jpfm.fs.ReadOnlyRawFileData;
import jpfm.operations.readwrite.ReadRequest;
import jpfm.operations.readwrite.SimpleReadRequest;
import jpfm.volume.RealFile;
import jpfm.volume.RealFileProvider;
import jpfm.volume.vector.VectorNode;
import neembuu.rangearray.RangeArray;
import neembuu.rangearray.RangeArrayFactory;
import neembuu.rangearray.RangeArrayParams;
import neembuu.rangearray.RangeUtils;

/**
 *
 * @author Shashank
 */
public class SpiedFile implements RealFile {
    private final RealFile rf;
    private final Path dest;
    private final RangeArray ra;
    private volatile boolean opened = false;
    
    public SpiedFile(Path p,VectorNode parent,Path destBase) throws IOException{
        rf = RealFileProvider.getNonBlockingRealFile(p.toAbsolutePath().toString(),parent);
        this.dest = destBase;
        ra = RangeArrayFactory.newDefaultRangeArray(new RangeArrayParams.Builder<>().build());
    }
    

    @Override
    public String getSourceFile() {
        return rf.getSourceFile();
    }

    @Override
    public void close() {
        try{
            if(fc!=null)fc.close(); 
            fc = null;
        }catch(Exception a){
            a.printStackTrace();
        }
        rf.close();
    }

    @Override
    public boolean isOpenByCascading() {
        return rf.isOpenByCascading();
    }

    @Override
    public void open() {
        opened = true;
        rf.open();
    }

    FileChannel fc = null;

    public Path getDest() {
        return dest;
    }
    
    public boolean opened(){
        return opened;
    }
    
    public String p(){
        Object[]pz=px();
        String ret = (String)pz[1];
        double ps = (Double)pz[0];
        return ps+"\t"+ret;
    }
    
    private Object[] px(){
        double sz = 0d;
        String ret = "";
        ret+="{";
        for (int i = 0; i < ra.size(); i++) {
            sz+=RangeUtils.getSize(ra.get(i));            
            ret+=","+ra.get(i);
        }ret+="}";
        return new Object[]{(sz/(getFileSize()*1d)),ret};
    }
    
    @Override public void read(ReadRequest read) throws Exception {
        if(!Files.exists(dest.getParent())){
            Files.createDirectories(dest.getParent());
        }if(fc==null){
            fc = FileChannel.open(dest, StandardOpenOption.CREATE,StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        rf.read(read);
        //write(read);
        
        ra.addElement(read.getFileOffset(), read.getFileOffset()+read.getByteBuffer().capacity() -1,this);
    }
    
    private void write(ReadRequest read)throws Exception{
        try{
            ByteBuffer bb = ByteBuffer.allocate(read.getByteBuffer().capacity());
            SimpleReadRequest srr = new SimpleReadRequest(bb, read.getFileOffset());
            rf.read(srr);
            while(!srr.isCompleted()){Thread.sleep(10);}
            bb.limit(bb.capacity());bb.rewind();
            
            while(fc.position()!=read.getFileOffset()){
                fc.position(read.getFileOffset());
            }
            fc.write(bb);
            fc.force(true);
        }catch(Exception a){
            a.printStackTrace();
        }
    }
    
    public void export()throws IOException{
        double ps = (Double)px()[0];
        if(ps>0){
            if(!Files.exists(dest.getParent())){
                Files.createDirectories(dest.getParent());
            }
            Files.copy(Paths.get(rf.getSourceFile()), dest,StandardCopyOption.REPLACE_EXISTING);
        }else if(opened){// the file was just opened, but not read
            if(!Files.exists(dest))
                Files.createFile(dest);
        }
    }

    @Override
    public void setCannotClose(boolean cannotClose) {
        rf.setCannotClose(cannotClose);
    }

    @Override
    public DirectoryStream getParent() {
        return rf.getParent();
    }

    @Override
    public FileType getFileType() {
        return rf.getFileType();
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        return rf.getFileDescriptor();
    }

    @Override
    public long getFileSize() {
        return rf.getFileSize();
    }

    @Override
    public long getCreateTime() {
        return rf.getCreateTime();
    }

    @Override
    public long getAccessTime() {
        return rf.getAccessTime();
    }

    @Override
    public long getWriteTime() {
        return rf.getWriteTime();
    }

    @Override
    public long getChangeTime() {
        return rf.getChangeTime();
    }

    @Override
    public String getName() {
        return rf.getName();
    }

    @Override
    public FileDescriptor getParentFileDescriptor() {
        return rf.getParentFileDescriptor();
    }

    @Override
    public FileFlags getFileFlags() {
        return rf.getFileFlags();
    }

    @Override
    public ReadOnlyRawFileData getReference(FileId fileId, AccessLevel level) {
        return rf.getReference(fileId, level);
    }
    
}
