package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.List;

public record SimplifiedTrackObject(List<SimplifiedArtistObject> artists,
        @JsonProperty("available_markets") List<String> availableMarkets,
        @JsonProperty("disc_number") Integer discNumber,
        @JsonProperty("duration_ms") Integer durationMs, Boolean explicit,
        @JsonProperty("external_urls") Object externalUrls, String href, String id,
        @JsonProperty("is_playable") Boolean isPlayable,
        @JsonProperty("linked_from") Object linkedFrom, Object restrictions, String name,
        @JsonProperty("preview_url") String previewUrl,
        @JsonProperty("track_number") Integer trackNumber, String type, String uri,
        @JsonProperty("is_local") Boolean isLocal) {
}
