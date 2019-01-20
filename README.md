# KafkaAPITest
##########################################################################################  
#zookeeper download:  
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz  

#setting conf/zoo.cfg 
tickTime=2000  
initLimit=10  
syncLimit=5  
dataDir=/tmp/zookeeper   
clientPort=2181  
server.0=192.168.31.201:2888:3888   

#before start zookeeper, ensure close firewall and iptables
systemctl status firewalld  
systemctl stop firewalld  
systemctl disable firewalld  
systemctl status iptables  
systemctl stop iptables  
systemctl disable iptables  
iptables -F  

#start zookeeper  
bash bin/zkServer.sh start  

##########################################################################################  
#kafka download:  
wget https://archive.apache.org/dist/kafka/0.10.2.1/kafka_2.10-0.10.2.1.tgz  

#setting config/server.properties. use shell command: grep -Ev "^$|^[#;]" config/server.properties  
broker.id=0  
listeners=PLAINTEXT://192.168.31.229:9092  
num.network.threads=3  
num.io.threads=8  
socket.send.buffer.bytes=102400  
socket.receive.buffer.bytes=102400  
socket.request.max.bytes=104857600  
log.dirs=/tmp/kafka-logs  
num.partitions=1  
num.recovery.threads.per.data.dir=1  
log.retention.hours=168  
log.segment.bytes=1073741824  
log.retention.check.interval.ms=300000  
zookeeper.connect=192.168.31.201:2181  
zookeeper.connection.timeout.ms=6000  

#before start kafka, ensure close firewall and iptables  
systemctl status firewalld  
systemctl stop firewalld  
systemctl disable firewalld  
systemctl status iptables  
systemctl stop iptables  
systemctl disable iptables  
iptables -F  

#start kafka  
bash bin/kafka-server-start.sh config/server.properties  

##########################################################################################  
#create topic:  
bash bin/kafka-topics.sh --create -zookeeper 192.168.31.201:2181 --replication-factor 1 --partitions 1 --topic test  

#list topic:  
bash bin/kafka-topics.sh --zookeeper 192.168.31.201:2181 --describe --topic test  

#produce message:  
bash bin/kafka-console-producer.sh --broker-list 192.168.31.229:9092 --topic test  

#consume message:  
bash bin/kafka-console-consumer.sh --bootstrap-server 192.168.31.229:9092 --topic topic_create --from-beginning  

bash bin/kafka-console-consumer.sh --bootstrap-server 192.168.31.229:9092 --topic topic_create  

