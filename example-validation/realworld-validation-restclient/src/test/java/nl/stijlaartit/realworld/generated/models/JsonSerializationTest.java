package nl.stijlaartit.realworld.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void article() throws Exception {
        var profile = new Profile("jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg", false);
        var original = new Article("how-to-train-your-dragon", "How to train your dragon",
                "Ever wonder how?", "It takes a Lifetime of training",
                List.of("reactjs", "angularjs", "dragons"),
                "2016-02-18T03:22:56.637Z", "2016-02-18T03:48:35.824Z",
                false, 0, profile);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Article.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void comment() throws Exception {
        var profile = new Profile("jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg", false);
        var original = new Comment(1, "2016-02-18T03:22:56.637Z", "2016-02-18T03:22:56.637Z",
                "It takes a Lifetime of training.", profile);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Comment.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void genericErrorModel() throws Exception {
        var original = new GenericErrorModel(new NewComment("can't be empty"));
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, GenericErrorModel.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void loginUser() throws Exception {
        var original = new LoginUser("jake@jake.jake", "jakejake");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, LoginUser.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void newArticle() throws Exception {
        var original = new NewArticle("How to train your dragon",
                "Ever wonder how?", "You have to believe",
                List.of("reactjs", "angularjs", "dragons"));
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, NewArticle.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void newComment() throws Exception {
        var original = new NewComment("His name was my name too.");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, NewComment.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void newUser() throws Exception {
        var original = new NewUser("Jacob", "jake@jake.jake", "jakejake");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, NewUser.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void profile() throws Exception {
        var original = new Profile("jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg", false);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Profile.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void updateArticle() throws Exception {
        var original = new UpdateArticle("Did you train your dragon?", "Ever wonder how?", "Updated body");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, UpdateArticle.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void updateUser() throws Exception {
        var original = new UpdateUser("jake@jake.jake", "newpassword", "jacob", "I like to skateboard", "https://i.stack.imgur.com/xHWG8.jpg");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, UpdateUser.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void user() throws Exception {
        var original = new User("jake@jake.jake", "jwt.token.here", "jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, User.class);
        assertThat(deserialized).isEqualTo(original);
    }
}
