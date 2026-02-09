package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.math.BigDecimal;

public record AudioAnalysisObjectMeta(@JsonProperty("analyzer_version") String analyzerVersion,
        String platform, @JsonProperty("detailed_status") String detailedStatus,
        @JsonProperty("status_code") Integer statusCode, Long timestamp,
        @JsonProperty("analysis_time") BigDecimal analysisTime,
        @JsonProperty("input_process") String inputProcess) {
}
