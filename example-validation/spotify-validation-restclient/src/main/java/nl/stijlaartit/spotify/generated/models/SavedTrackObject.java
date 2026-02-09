package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.time.OffsetDateTime;

public record SavedTrackObject(@JsonProperty("added_at") OffsetDateTime addedAt, Object track) {
}
