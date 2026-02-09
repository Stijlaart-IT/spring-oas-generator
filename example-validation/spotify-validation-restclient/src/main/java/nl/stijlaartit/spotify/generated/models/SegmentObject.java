package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record SegmentObject(BigDecimal start, BigDecimal duration, BigDecimal confidence,
        @JsonProperty("loudness_start") BigDecimal loudnessStart,
        @JsonProperty("loudness_max") BigDecimal loudnessMax,
        @JsonProperty("loudness_max_time") BigDecimal loudnessMaxTime,
        @JsonProperty("loudness_end") BigDecimal loudnessEnd, List<BigDecimal> pitches,
        List<BigDecimal> timbre) {
}
