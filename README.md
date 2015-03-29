# spyfs
Spy FS - The purpose of this tool is to analyze which files in a folder are actually useful and which are not. With this information you can make duplicate of that folder with only those files which are required. This had been created to create an extremely small and portable version of python, for purpose of running youtube-dl


# Why is it called SpyFS ? What does it do ? How does it work ?
SpyFS makes a virtual filesystem (think daemon tools, magic iso, fuse, mounting etc).

You specify an input folder, say **c:\python27** ,

and you also specify a virtual reflection say **c:\python27_virtual** . Here c:\python27_virtual is the mount location where a virtual folder reflecting all files from c:\python27 will appear. 

Now you run python from the virtual folder, say you use the command

c:\python27_virtual\python.exe youtube-dl --help

This command runs youtube-dl.

Now please understand, youtube-dl, requires only certain files from python runtime, it doens't require the entire python runtime. The size of the c:\python27 folder is 57 MB. Youtube-dl when you run it, it uses only a small number of files from python runtime. Some files, it just checks whether they exists or not, it doesn't actually read them. All this is observed (**spied**) by SpyFS, and based on the experience gained it known which files are required for running youtube-dl and only those files it ejects out in the output directory.


Now you ask the **SpyFS** to output a copy of the files which are actually required. So that is what is does, say you choose, **c:\python27_small** . So now, some 6.78 MB. Yes! Without compression, the size of python runtime reduces to 6.78 MB from original 57 MB. 

How? 

Simple, we removed files from python runtime which were not required by the application which we were interested in (in this example, youtube-dl ). So if you use this mini runtime to run some other python application, it is very likely that it will not work.

So this was why SpyFS was created. You might use it for some other reason. Feel free to use it whichever way you like, it is open source :D


# Usage
java -jar spyfs.jar <param1-source> <param2-storeTo> <param3-virtual>

# Example
java -jar spyfs.jar "c:\Python27" "c:\Python27mini" "c:\Python27Virtual"
