cd /home/cloudera/kafka/bin
./kafka-topics.sh --create --bootstrap-server localhost:9092 --partitions 1 --replication-factor 2 --topic redditposts
