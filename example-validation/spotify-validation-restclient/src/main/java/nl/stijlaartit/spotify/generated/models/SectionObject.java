package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.math.BigDecimal;

public record SectionObject(BigDecimal start, BigDecimal duration, BigDecimal confidence,
        BigDecimal loudness, BigDecimal tempo,
        @JsonProperty("tempo_confidence") BigDecimal tempoConfidence, Integer key,
        @JsonProperty("key_confidence") BigDecimal keyConfidence, SectionObjectMode mode,
        @JsonProperty("mode_confidence") BigDecimal modeConfidence,
        @JsonProperty("time_signature") TimeSignature timeSignature,
        @JsonProperty("time_signature_confidence") BigDecimal timeSignatureConfidence) {
}
