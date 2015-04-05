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

# Java in 5 MB
Using SpyFS you can distribute a javafx application, which are (less than) 5MB in size !
Yes this is possible and this is insane small size. Even modular java 9 might not be able to beat this !
BTW, you may download the sample 5MB application from spyfs git. It is a 64-bit app, so it will not run on 32-bit OSes.

There are many tricks used for this process. I will describe the steps here.
  1. Unzip rt.jar and all other java runtime bootstrap jar files in a single folder, let us call it **rt** . To name all of them access-bridge-64.jar;charsets.jar;cldrdata.jar;dnsns.jar;jaccess.jar;javaws.jar;jce.jar;jfr.jar;jfxrt.jar;jfxswt.jar;jsse.jar;localedata.jar;local_policy.jar;management-agent.jar;nashorn.jar;plugin.jar;resources.jar;rt.jar;sunec.jar;sunjce_provider.jar;sunmscapi.jar;sunpkcs11.jar;US_export_policy.jar;zipfs.jar
  2. Now package your java application as a native image. Use javafx native packing tool for this purpose.
  3. Typically the application folder contains a folder named **app** , a folder named **runtime**, an executable which runs your application, etc.
  4. Copy **rt** which you prepared in step1, inside this application folder.
  5. Inside app\ folder you will find a **cfg** file. Example <application folder>\app\NT.cfg ; edit this file.
  6. Add following lines 
  7. jvmarg.1=-Xbootclasspath:$APPDIR\rt
  8. jvmarg.2=-Djava.ext.dirs=
  9. These two lines force java to use classes from the rt folder which we created. When the app is run, it will use only a very small portion of classes than 10000000s of classes which are present in jre. SpyFS can detect these and save only these. This makes it possible to package our java application into insanely small size.
  10. Test the app by running it and make sure everything is ok.
  11. Now we are ready to run spyfs on this.
  12. Run spyfs and make a virtual copy of this folder
  13. Run the app from the virtual folder. **Use commandline**, avoid opening the virtual folder. If you open the virtual folder, it would create an illusion to spyfs. SpyFS might add more files because when you open the folder, spyfs thinks the application opened those folders/files. So avoid visiting the virtual folder and polluting the spyfs sample. Use command line and invoke the exe, that is it. Do not touch anything else, because whatever you touch will be added to final distribution.
  14. **Use the app throughly and make sure you don't miss any feature.** This step is important, because your program would crash in client side if it uses some functionality whoes classfiles were stripped off by spyfs.
  15. Using spyfs ui, make a copy of this virtual folder
  16. Now you have a ultra small version of your java application with java runtime
  17. Compress this using the tool or your choice or make a setup out of it.
  18. After compression, the size of the application may go as low as 3MB ! For UI application made using javafx, the minimum size would be around 5MB ! 

  




