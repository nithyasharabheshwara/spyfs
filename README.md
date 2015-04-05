# spyfs
Spy FS - A tool to create a virtual copy of a folder and spy on that folder, to know which files are read and at what locations (file offset ranges). The purpose of this tool is to analyze which files in a folder are actually useful and which are not. With this information you can make duplicate of that folder with only those files which are required. This had been created to create an extremely small and portable version of python, for purpose of running youtube-dl


# Why is it called SpyFS ? What does it do ? 
SpyFS spies on which files are used and how much they are used. It also creates an optimized duplicate of any folder which you spy upon, keeping only files which are required. Suppose you have a 50MB folder, contains various dlls etc of which only some you need. How do you know which ones you need to copy and which ones you don't actually need?
SpyFS can help you here. Using SpyFS you can make slim-down copies of runtime environments of java, python and other things based on what you require. I have personally used SpyFS to reduce size of python runtime from 57MB to 6MB (without compression). If you further delete the compiled pyc files and then zip it, it further reduces to 1.27MB !!!!


# How does it work ? Can you give a usage scenario?
SpyFS makes a virtual filesystem (think daemon tools, magic iso, fuse, mounting etc).

You specify an input folder, say **c:\python27** ,

and you also specify a virtual reflection say **c:\python27_virtual** . Here c:\python27_virtual is the mount location where a virtual folder reflecting all files from c:\python27 will appear. 

Now you run python from the virtual folder, say you use the command

c:\python27_virtual\python.exe youtube-dl

This command runs youtube-dl. Please note, instead of using the real location of python, you are using the virtual location of python. This virtual location of python is spied upon by SpyFS. SpyFS collects information on all acitivied done in this virtual folder. SpyFS knows, which files were read, and how much ( 50% , 20%, or 10% from begining and 13% from middle etc)


Now please understand, youtube-dl, requires only certain files from python runtime, it doens't require the entire python runtime. The size of the c:\python27 folder is 57 MB. Youtube-dl when you run it, it uses only a small number of files from python runtime. Some files, it just checks whether they exists or not, it doesn't actually read them. All this is observed (**spied**) by SpyFS, and based on what files where actually read SpyFS ejects out a duplicate of the source directory in the output directory.

Say the output directory is, **c:\python27_small** . So now, some 6.78 MB files in total, are copied from source ( **c:\python27** )  to destination directory ( **c:\python27_small** ). Yes! Without compression, the size of python runtime reduces to 6.78 MB from original 57 MB. 


# How did spyfs reduce size of python runtime from 57MB to 6.78MB? 
Simple, we removed files from python runtime which were not required by the application which we were interested in and running using python  (in this example, youtube-dl ). So if you use this mini runtime to run some other python application, it is very likely that it will not work.




So this was why SpyFS was created. You might use it for some other reason. Feel free to use it whichever way you like, it is open source :D

# Runtime requirements
1. You need java 8 to run this
2. You also need to download and install pismo file mount, which is available here : http://www.pismotechnic.com/download/
3. As of now this works only on windows. Both 32-bit and 64-bit environmens are supported however. To make this thing to work on linux or mac, you will have to build jpfm from sources. Untested libraries for these platforms are also available, but they do not work so easily. 

# Usage
java -jar spyfs.jar

A javafx based user interface will help you use SpyFS.

