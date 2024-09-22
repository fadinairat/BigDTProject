# Project name: Reddit Data Streaming, Analysis, and Visualization

## Contributors:
1. 

## Synopsis:

* A project focused on retrieving current Reddit posts as a stream in real-time, storing them for later retrieval, and conducting analysis on the data.

### Mission:

* Given the massive amount of data generated on Reddit every second, this project demonstrates the use of big data tools and technologies to handle, store, and analyze real-time data efficiently.

___

## Features:

1. Select specific subreddits of interest to collect posts and insights in real time from Reddit.

2. Persist data in a way that supports large-scale data volumes.

3. Apply analysis and query capabilities on the stored data later.

___

## Motivation:

**What is the vision of this product?**

> * This demo will help simulate and understand how to handle large amounts of Reddit data in real time within production environments and on cluster mode.

**What pain point does this project solve?**

> * It provides an efficient way to handle, store, and analyze data that is continuously generated at a fast pace, particularly from Reddit.

____

## Requirements:

1. Any OS (Linux, Windows, Mac) with VM running Cloudera QuickStart or Docker with Cloudera QuickStart image (includes: `Hadoop`, `Spark`, `HBase`, `Hive`) [Cloudera QuickStart - CentOS is preferred].
2. Kafka is required for this project. ([Download](https://kafka.apache.org/downloads) any binary) (Version 2.0.1).
3. Maven as a build management tool for client dependencies.
4. IDE (IntelliJ/Eclipse).
5. Reddit API access to retrieve posts.
___

## Installation and usage:
### Steps
1. Set up Reddit API using the `praw` library (Python Reddit API Wrapper):
    - Register for Reddit API access.
    - Get `Client ID`, `Client Secret`, and `User Agent`.
    - Use these credentials to authenticate with Reddit and retrieve post data from specific subreddits.

2. Create an HBase table that will be populated by the Spark streaming consumer in real time:
    ```bash
    $ echo "create 'redditposts', 'post-info', 'general-info'" | hbase shell
    ```

3. Clone two repositories: [producer](https://github.com/fadinairat/kafka-reddit-producer) and [consumer](https://github.com/fadinairat/kafka-spark-consumer).

4. Start the Zookeeper server and Kafka server:
    ```bash
    $ bin/zookeeper-server-start.sh config/zookeeper.properties
    $ bin/kafka-server-start.sh config/server.properties
    ```

5. Create a Kafka topic (in our case `redditposts` is the topic name):
    ```bash
    $ bin/kafka-topics --create --topic redditposts --bootstrap-server localhost:9092
    ```

6. Run the Kafka Reddit producer from the main class `org.producer.RedditProducer` using IntelliJ/Eclipse on the VM.<br>In Docker, build a fat jar (mvn package), copy it to the container and run it:
    ```bash
    $ java -cp org.producer.RedditProducer reddit-producer-jar-with-dependencies.jar
    ```

7. Run the Kafka consumer from the main class `org.reddit.consumer.Listener` using IntelliJ/Eclipse on the VM.<br>In Docker, build a fat jar (mvn package), copy it to the container and run it:
    ```bash
    $ java -cp org.reddit.consumer.Listener reddit-consumer-jar-with-dependencies.jar
    ```
    Or submit as a Spark job:
    ```bash
    $ spark-submit --class "org.reddit.consumer.Listener" --master {local[*] | yarn} reddit-consumer-jar-with-dependencies.jar
    ```

8. Real-time streaming will automatically begin once the producer is running. The consumer (or listener) will continue to listen to events on the `redditposts` topic using Spark Streaming, saving the data directly to our HBase table with two main column families, `post-info` and `general-info`.

9. Optionally, start the Hive script to create an external table for Hive and load the data from HBase into Hive:
    ```sql
    CREATE EXTERNAL TABLE redditposts (
    rowkey STRING, 
    title STRING, 
    score INT, 
    text STRING, 
    url STRING, 
    username STRING, 
    subreddit STRING, 
    timestamp BIGINT
    )
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    WITH SERDEPROPERTIES ('hbase.columns.mapping' = ':key,post-info:title,general-info:score,post-info:text,post-info:url,general-info:username,post-info:subreddit,general-info:timestamp')
    TBLPROPERTIES ('hbase.table.name' = 'redditposts');
    ```

10. Optionally, start the SparkSQL class in the consumer to perform further real-time queries on the data in HBase.
____

## Non-functional requirements:

* Data integrity.
* Portability.
* Reliability.
* Adaptability.
____

## Screenshots
HBase table loaded by the Spark Streaming Kafka consumer:
![Hbase_table_loaded](OutputPic/hbase_table_loaded.png)

## Reddit API references:

https://www.reddit.com/dev/api

