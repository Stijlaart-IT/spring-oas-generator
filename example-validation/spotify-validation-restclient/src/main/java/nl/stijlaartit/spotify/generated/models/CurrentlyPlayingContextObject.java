package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.String;

public record CurrentlyPlayingContextObject(Object device,
        @JsonProperty("repeat_state") String repeatState,
        @JsonProperty("shuffle_state") Boolean shuffleState, Object context, Long timestamp,
        @JsonProperty("progress_ms") Integer progressMs,
        @JsonProperty("is_playing") Boolean isPlaying, Object item,
        @JsonProperty("currently_playing_type") String currentlyPlayingType, Object actions) {
}
