package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.util.List;

public record QueueObject(@JsonProperty("currently_playing") Object currentlyPlaying,
        List<Object> queue) {
}
