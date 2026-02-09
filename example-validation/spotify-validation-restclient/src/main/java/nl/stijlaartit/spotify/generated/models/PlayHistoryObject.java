package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.time.OffsetDateTime;

public record PlayHistoryObject(Object track, @JsonProperty("played_at") OffsetDateTime playedAt,
        Object context) {
}
