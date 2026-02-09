package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.time.OffsetDateTime;

public record SavedAlbumObject(@JsonProperty("added_at") OffsetDateTime addedAt, Object album) {
}
