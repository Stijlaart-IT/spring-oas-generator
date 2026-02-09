package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Float;
import java.lang.Integer;
import java.lang.String;

public record AudioFeaturesObject(Float acousticness,
        @JsonProperty("analysis_url") String analysisUrl, Float danceability,
        @JsonProperty("duration_ms") Integer durationMs, Float energy, String id,
        Float instrumentalness, Key key, Float liveness, Loudness loudness, Mode mode,
        Float speechiness, Tempo tempo, @JsonProperty("time_signature") TimeSignature timeSignature,
        @JsonProperty("track_href") String trackHref, AudioFeaturesObjectType type, String uri,
        Float valence) {
}
