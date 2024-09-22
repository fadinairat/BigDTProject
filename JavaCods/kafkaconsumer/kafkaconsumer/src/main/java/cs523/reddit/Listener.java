package cs523.reddit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kafka.serializer.StringDecoder;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Listener {

    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("RedditListener");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        JavaStreamingContext ssc = new JavaStreamingContext(jsc,
                Durations.seconds(5));

        // Set the topic to read Reddit posts from Kafka
        Set<String> topics = new HashSet<>(Arrays.asList("redditposts".split(",")));
        Map<String, String> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "StringDeserializer");
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "StringDeserializer");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");

        // Set up the Kafka stream
        JavaPairInputDStream<String, String> stream = KafkaUtils
                .createDirectStream(ssc, String.class, String.class,
                        StringDecoder.class, StringDecoder.class, kafkaParams,
                        topics);

        // Process the stream and convert the JSON string into RedditPost objects
        stream.foreachRDD(rdd -> {
            JavaRDD<Reddit> jrdd = rdd.map(f -> new Gson().fromJson(f._2, Reddit.class));

            jrdd.foreach(redditPost -> {
                //System.out.println("Processing Reddit post: " + redditPost);
                // Store the Reddit post in HBase
            	System.out.println("Populating the Data...");
                RedditHbaseTable.populateData(redditPost);
            });
        });

        ssc.start();
        ssc.awaitTermination();
    }

    // Helper method to parse the Reddit JSON object (if necessary for manual processing)
    public static Reddit getRedditPost(JsonObject o) {
        Reddit redditPost = new Reddit();

        redditPost.setId(o.get("id").getAsString());
        redditPost.setTitle(o.get("title").getAsString());
        redditPost.setText(o.get("selftext").getAsString());  // Use "selftext" for the text of the post
        redditPost.setUrl(o.get("url").getAsString());
        redditPost.setSubreddit(o.get("subreddit").getAsString());
        redditPost.setUsername(o.get("author").getAsString());
        redditPost.setTimeStamp(o.get("created_utc").getAsString());  // Use "created_utc" for timestamp
        redditPost.setScore(o.get("score").getAsString());

        return redditPost;
    }
}
