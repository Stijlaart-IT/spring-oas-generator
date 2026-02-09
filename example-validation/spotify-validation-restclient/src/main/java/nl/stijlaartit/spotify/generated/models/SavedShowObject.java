package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.time.OffsetDateTime;

public record SavedShowObject(@JsonProperty("added_at") OffsetDateTime addedAt, Object show) {
}
