package cs523.reddit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

public class SparkSql {
    private static final String TABLE_NAME = "redditposts";
    private static final String CF_DEFAULT = "post-info";
    private static final String CF_GENERAL = "general-info";

    static Configuration config;
    static JavaSparkContext jsc;

    public static void main(String[] args) {

        SparkConf sconf = new SparkConf().setAppName("SparkSQL")
                .setMaster("local[3]");
        sconf.registerKryoClasses(new Class[]{org.apache.hadoop.hbase.io.ImmutableBytesWritable.class});

        config = HBaseConfiguration.create();
        config.set(TableInputFormat.INPUT_TABLE, TABLE_NAME);

        jsc = new JavaSparkContext(sconf);
        SQLContext sqlContext = new SQLContext(jsc.sc());

        JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = readTableByJavaPairRDD();
        System.out.println("Number of rows in HBase table: " + hBaseRDD.count());

        // Mapping to Reddit object
        JavaRDD<Reddit> rows = hBaseRDD.map(x -> {
            Reddit redditPost = new Reddit();

            redditPost.setId(Bytes.toString(x._1.get()));
            redditPost.setTitle(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_DEFAULT), Bytes.toBytes("title"))));
            redditPost.setText(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_DEFAULT), Bytes.toBytes("text"))));
            redditPost.setSubreddit(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_DEFAULT), Bytes.toBytes("subreddit"))));
            redditPost.setUrl(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_DEFAULT), Bytes.toBytes("url"))));
            redditPost.setUsername(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_GENERAL), Bytes.toBytes("username"))));
           redditPost.setTimeStamp(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_GENERAL), Bytes.toBytes("time_stamp"))));
            redditPost.setScore(Bytes.toString(x._2.getValue(Bytes.toBytes(CF_GENERAL), Bytes.toBytes("score"))));

            return redditPost;
        });

        // Create DataFrame
        DataFrame tabledata = sqlContext.createDataFrame(rows, Reddit.class);
        tabledata.registerTempTable(TABLE_NAME);
        tabledata.printSchema();

        // Sample queries
        DataFrame query2 = sqlContext
                .sql("select username, count(*) from redditposts group by username order by count(*) desc limit 10");
        query2.show();

        DataFrame query3 = sqlContext
                .sql("select subreddit, count(*) from redditposts GROUP BY subreddit order by count(*) desc limit 10");
        query3.show();

        DataFrame query4 = sqlContext
                .sql("select score, count(*) from redditposts GROUP BY score order by count(*) desc limit 10");
        query4.show();

        jsc.stop();
    }

    public static JavaPairRDD<ImmutableBytesWritable, Result> readTableByJavaPairRDD() {

        JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = jsc
                .newAPIHadoopRDD(
                        config,
                        TableInputFormat.class,
                        org.apache.hadoop.hbase.io.ImmutableBytesWritable.class,
                        org.apache.hadoop.hbase.client.Result.class);
        return hBaseRDD;
    }
}
