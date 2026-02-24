package nl.stijlaartit.realworld.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private <T> void assertSerializesSymmetrical(T original, Class<T> type) {
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void article() {
        var profile = new Profile("jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg", false);
        var original = new Article("how-to-train-your-dragon", "How to train your dragon",
                "Ever wonder how?", "It takes a Lifetime of training",
                List.of("reactjs", "angularjs", "dragons"),
                "createdAt",
                "updatedAt",
                false, 0, profile);
        assertSerializesSymmetrical(original, Article.class);
    }

    @Test
    void comment() {
        var profile = new Profile("jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg", false);
        var original = new Comment(1,
                "createdAt",
                "updatedAt",
                "It takes a Lifetime of training.", profile);
        assertSerializesSymmetrical(original, Comment.class);
    }

    @Test
    void genericErrorModel() {
        var original = new GenericErrorModel(new GenericErrorModelErrors(List.of("can't be empty")));
        assertSerializesSymmetrical(original, GenericErrorModel.class);
    }

    @Test
    void loginUser() {
        var original = new LoginUser("jake@jake.jake", "jakejake");
        assertSerializesSymmetrical(original, LoginUser.class);
    }

    @Test
    void newArticle() {
        var original = new NewArticle("How to train your dragon",
                "Ever wonder how?", "You have to believe",
                List.of("reactjs", "angularjs", "dragons"));
        assertSerializesSymmetrical(original, NewArticle.class);
    }

    @Test
    void newComment() {
        var original = new NewComment("His name was my name too.");
        assertSerializesSymmetrical(original, NewComment.class);
    }

    @Test
    void newUser() {
        var original = new NewUser("Jacob", "jake@jake.jake", "jakejake");
        assertSerializesSymmetrical(original, NewUser.class);
    }

    @Test
    void profile() {
        var original = new Profile("jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg", false);
        assertSerializesSymmetrical(original, Profile.class);
    }

    @Test
    void updateArticle() {
        var original = new UpdateArticle("Did you train your dragon?", "Ever wonder how?", "Updated body");
        assertSerializesSymmetrical(original, UpdateArticle.class);
    }

    @Test
    void updateUser() {
        var original = new UpdateUser("jake@jake.jake", "newpassword", "jacob", new NullWrapper<>("createdAt"),
                new NullWrapper<>("https://i.stack.imgur.com/xHWG8.jpg"));
        assertSerializesSymmetrical(original, UpdateUser.class);
    }

    @Test
    void user() {
        var original = new User("jake@jake.jake", "jwt.token.here", "jake", "I work at State Farm", "https://i.stack.imgur.com/xHWG8.jpg");
        assertSerializesSymmetrical(original, User.class);
    }
}
