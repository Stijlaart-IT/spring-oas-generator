package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Boolean;
import java.lang.Object;
import java.time.OffsetDateTime;

public record PlaylistTrackObject(@JsonProperty("added_at") OffsetDateTime addedAt,
        @JsonProperty("added_by") Object addedBy, @JsonProperty("is_local") Boolean isLocal,
        Object track) {
}
