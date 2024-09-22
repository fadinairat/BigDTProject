package cs523.producer;

import com.google.gson.Gson;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.SubredditSort;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Producer {

    final Logger logger = LoggerFactory.getLogger(Producer.class);

    private KafkaProducer<String, String> producer;
    private RedditClient redditClient;

    public static void main(String[] args) {
        new Producer().run();
    }

    // Kafka Producer
    private KafkaProducer<String, String> createKafkaProducer() {
        // Create producer properties
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaProducer<>(properties);
    }

    private void run() {
        logger.info("Setting up Kafka and Reddit client");

        // 1. Create Kafka Producer
        producer = createKafkaProducer();

        // 2. Create Reddit client
        redditClient = createRedditClient();

        // Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application is now stopping!");
            logger.info("Closing Producer");
            producer.close();
            logger.info("Finished closing");
        }));

        // 3. Fetch Reddit posts every 5 seconds
        while (true) {
            // Fetch Reddit posts and send them to Kafka
            fetchAndSendRedditPosts();

            // Sleep for 5 seconds before fetching the next batch
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                logger.error("Interrupted exception", e);
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
    }

    // Create Reddit Client
    private RedditClient createRedditClient() {
        // Create credentials for Reddit API
        Credentials credentials = Credentials.script(
        		"Weekly_Instance6926",
        		"BIG@@2024",  // client_secret
                "UfhUWbStbQi3TeySW2wsUg",  // client_id
                "luh-R1HadOk07x7xxY8gyJUtXudlAg"
        );

        UserAgent userAgent = new UserAgent("bot", "cs523.producer", "1.0", "Trick-Day1507s");

        // Use OkHttpNetworkAdapter
        OkHttpNetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        
        // Return RedditClient with automatic OAuth
        return OAuthHelper.automatic(networkAdapter, credentials);
    }

    // Fetch Reddit posts and send them to Kafka
    private void fetchAndSendRedditPosts() {
        try {
        	List<String> subreddits = Arrays.asList("AZURE", "DATAENGINEERING", "BIGDATA", "MACHINELEARNING","WEBDEV");
        	
        	for (String subredditName : subreddits) {
                SubredditReference subreddit = redditClient.subreddit(subredditName);
             // Fetch the top 10 Reddit posts from the subreddit
                DefaultPaginator<Submission> paginator = subreddit.posts()
                        .sorting(SubredditSort.TOP)
                        .limit(50)
                        .build();

                Listing<Submission> posts = paginator.next();

                for (Submission submission : posts) {
                    Reddit redditPost = convertSubmissionToReddit(submission);

                    logger.info("Fetched Reddit post: " + redditPost);

                    // Send the RedditPost object as JSON to Kafka
                    producer.send(new ProducerRecord<>(
                            KafkaConfig.TOPIC, "", new Gson().toJson(redditPost)),
                            (RecordMetadata recordMetadata, Exception e) -> {
                                if (e != null) {
                                    logger.error("Error sending Reddit post", e);
                                }
                            });
                }
        	}
        } catch (Exception e) {
            logger.error("Error while fetching or sending Reddit posts", e);
        }
    }

    // Convert JRAW Submission object to Reddit object
    private Reddit convertSubmissionToReddit(Submission submission) {
        Reddit redditPost = new Reddit();

        // Assign unique ID using submission's ID
        redditPost.setId(submission.getId());

        // Set the title
        redditPost.setTitle(submission.getTitle());

        // Set the self-text (if it's a text post)
        redditPost.setText(submission.getSelfText());

        // Set the URL (could be the post URL or an external link)
        redditPost.setUrl(submission.getUrl());

        // Set the subreddit name
        redditPost.setSubreddit(submission.getSubreddit());

        // Set the username of the author
        redditPost.setUsername(submission.getAuthor());

        // Set the creation timestamp
        redditPost.setTimeStamp(String.valueOf(submission.getCreated().getTime()));

        // Set the score (upvotes/downvotes)
        redditPost.setScore(Integer.toString(submission.getScore()));

        return redditPost;
    }
}
