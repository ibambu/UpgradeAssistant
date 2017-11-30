sudo su #切换到root权限
#download jdk1.8.0_151
#wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u151-b12/e758a0de34e24606bca991d704f6dcbf/jdk-8u151-linux-x64.tar.gz?AuthParam=1511693957_cf326e0d137aab277b3abdd55dee9e35
mkdir /usr/local/java
#extract jdk
tar -xvf jdk/jdk-8u151-linux-x64.tar.gz
cp -r jdk1.8.0_151 /usr/local/java

#set environment
export JAVA_HOME="/usr/local/java/jdk1.8.0_151"
if ! grep "JAVA_HOME=/usr/local/java/jdk1.8.0_151" /etc/environment 
then
    echo "JAVA_HOME=/usr/local/java/jdk1.8.0_151" | sudo tee -a /etc/environment 
    echo "export JAVA_HOME" | sudo tee -a /etc/environment 
    echo "PATH=$PATH:$JAVA_HOME/bin" | sudo tee -a /etc/environment 
    echo "export PATH" | sudo tee -a /etc/environment 
    echo "CLASSPATH=.:$JAVA_HOME/lib" | sudo tee -a /etc/environment 
    echo "export CLASSPATH" | sudo tee -a /etc/environment 
fi
#update environment
source /etc/environment  
ehco "jdk is installed !"
