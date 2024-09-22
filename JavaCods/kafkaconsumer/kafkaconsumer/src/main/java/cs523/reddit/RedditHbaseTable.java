package cs523.reddit;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;

public class RedditHbaseTable {

    public static Configuration config = HBaseConfiguration.create();
    public static Connection connection = null;
    public static Admin admin = null;

    private static final String TABLE_NAME = "redditposts"; // Renamed for Reddit posts
    private static final String CF_DEFAULT = "post-info"; // Column family for post info
    private static final String CF_GENERAL = "general-info"; // Column family for general info

    private final static byte[] CF_DEFAULT_BYTES = CF_DEFAULT.getBytes();
    private final static byte[] CF_GENERAL_BYTES = CF_GENERAL.getBytes();

    private static Table redditPosts;

    static {
        try {
            connection = ConnectionFactory.createConnection(config);
            admin = connection.getAdmin();

            HTableDescriptor table = new HTableDescriptor(
                    TableName.valueOf(TABLE_NAME));
            table.addFamily(new HColumnDescriptor(CF_DEFAULT)
                    .setCompressionType(Algorithm.NONE));
            table.addFamily(new HColumnDescriptor(CF_GENERAL)
                    .setCompressionType(Algorithm.NONE));

            if (admin.tableExists(table.getTableName())) {
            	System.out.print("Appending Data.... ");
                //admin.disableTable(table.getTableName());
                //admin.deleteTable(table.getTableName());
            }
            else {
            	System.out.print("Creating table.... ");
            	admin.createTable(table);
            }

            redditPosts = connection.getTable(TableName.valueOf(TABLE_NAME));

            System.out.println(" Done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to populate HBase with Reddit post data
    public static void populateData(Reddit redditPost) throws IOException {
        Put row = new Put(redditPost.getId().getBytes()); // Use Reddit post ID

        // Add post-specific data to "post-info" column family
        row.addColumn(CF_DEFAULT_BYTES, "title".getBytes(), redditPost.getTitle().getBytes());
        row.addColumn(CF_DEFAULT_BYTES, "text".getBytes(), redditPost.getText().getBytes());
        row.addColumn(CF_DEFAULT_BYTES, "url".getBytes(), redditPost.getUrl().getBytes());
        row.addColumn(CF_DEFAULT_BYTES, "subreddit".getBytes(), redditPost.getSubreddit().getBytes());

        // Add general info to "general-info" column family
        row.addColumn(CF_GENERAL_BYTES, "username".getBytes(), redditPost.getUsername().getBytes());
        row.addColumn(CF_GENERAL_BYTES, "timestamp".getBytes(), redditPost.getTimeStamp().getBytes());
        row.addColumn(CF_GENERAL_BYTES, "score".getBytes(), String.valueOf(redditPost.getScore()).getBytes());

        // Insert the row into the HBase table
        redditPosts.put(row);
    }
}
