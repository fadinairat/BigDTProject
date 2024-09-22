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
