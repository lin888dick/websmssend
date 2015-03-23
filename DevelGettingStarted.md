# Getting Started #


# IDE Installation #

The integrated development environment (IDE) utilizes the following tools and technologies
  * [Netbeans](http://netbeans.org) (starting Netbeans 7.2.1 Visual Mobile Designer Plugin needs to be installed)
  * [Sun Java Wireless Toolkit](http://www.oracle.com/technetwork/java/index-jsp-137162.html) 2.5 or newer
  * [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

For O2-Connector to be functioning the following to certificates have to be imported in the emulators keystore:
  * [VeriSign Class 3 Public Primary Certification Authority - G5](http://websmssend.googlecode.com/svn/wiki/VeriSign%20Class%203%20Public%20Primary%20Certification%20Authority%20-%20G5.cer)
  * [VeriSign International Server CA - Class 3](http://websmssend.googlecode.com/svn/wiki/VeriSign%20Inc.cer)

In oder to run WTK 2.5.2 on a Win7 64bit machine you have to change to a 32 bit version of jdk. To do this you have to edit the file `$WTK_HOME\bin\emulator.vm` and change the jdk in the first line to `C:\PROGRA~2\Java\jre6\bin\java`