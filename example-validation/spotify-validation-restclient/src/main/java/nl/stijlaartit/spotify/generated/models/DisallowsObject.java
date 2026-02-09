package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Boolean;

public record DisallowsObject(@JsonProperty("interrupting_playback") Boolean interruptingPlayback,
        Boolean pausing, Boolean resuming, Boolean seeking,
        @JsonProperty("skipping_next") Boolean skippingNext,
        @JsonProperty("skipping_prev") Boolean skippingPrev,
        @JsonProperty("toggling_repeat_context") Boolean togglingRepeatContext,
        @JsonProperty("toggling_shuffle") Boolean togglingShuffle,
        @JsonProperty("toggling_repeat_track") Boolean togglingRepeatTrack,
        @JsonProperty("transferring_playback") Boolean transferringPlayback) {
}
