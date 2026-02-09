package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.lang.String;
import java.math.BigDecimal;

public record AudioAnalysisObjectTrack(@JsonProperty("num_samples") Integer numSamples,
        BigDecimal duration, @JsonProperty("sample_md5") String sampleMd5,
        @JsonProperty("offset_seconds") Integer offsetSeconds,
        @JsonProperty("window_seconds") Integer windowSeconds,
        @JsonProperty("analysis_sample_rate") Integer analysisSampleRate,
        @JsonProperty("analysis_channels") Integer analysisChannels,
        @JsonProperty("end_of_fade_in") BigDecimal endOfFadeIn,
        @JsonProperty("start_of_fade_out") BigDecimal startOfFadeOut, Loudness loudness,
        Tempo tempo, @JsonProperty("tempo_confidence") BigDecimal tempoConfidence,
        @JsonProperty("time_signature") TimeSignature timeSignature,
        @JsonProperty("time_signature_confidence") BigDecimal timeSignatureConfidence, Key key,
        @JsonProperty("key_confidence") BigDecimal keyConfidence, Mode mode,
        @JsonProperty("mode_confidence") BigDecimal modeConfidence, String codestring,
        @JsonProperty("code_version") BigDecimal codeVersion, String echoprintstring,
        @JsonProperty("echoprint_version") BigDecimal echoprintVersion, String synchstring,
        @JsonProperty("synch_version") BigDecimal synchVersion, String rhythmstring,
        @JsonProperty("rhythm_version") BigDecimal rhythmVersion) {
}
