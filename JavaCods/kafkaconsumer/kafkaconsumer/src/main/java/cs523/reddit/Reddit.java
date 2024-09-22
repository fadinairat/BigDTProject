package cs523.reddit;

import java.util.ArrayList;
import java.util.List;

public class Reddit {

    private String id;         // Unique ID of the Reddit post
    private String title;      // Title of the Reddit post
    private String text;       // Self-text or content of the Reddit post
    private String url;        // URL if it's a link post
    private String subreddit;  // Subreddit where the post was submitted
    private String username;   // Username of the post's author
    private String timeStamp;  // Timestamp of when the post was created
    private String score;         // Score of the post (upvotes - downvotes)

    // Getter and setter methods for each field
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "RedditPost [id=" + id + ", title=" + title + ", text=" + text + ", url=" + url
                + ", subreddit=" + subreddit + ", username=" + username
                + ", timeStamp=" + timeStamp + ", score=" + score + "]";
    }
}
