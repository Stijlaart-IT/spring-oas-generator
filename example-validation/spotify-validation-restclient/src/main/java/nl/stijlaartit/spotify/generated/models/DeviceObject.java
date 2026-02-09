package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;

public record DeviceObject(String id, @JsonProperty("is_active") Boolean isActive,
        @JsonProperty("is_private_session") Boolean isPrivateSession,
        @JsonProperty("is_restricted") Boolean isRestricted, String name, String type,
        @JsonProperty("volume_percent") Integer volumePercent,
        @JsonProperty("supports_volume") Boolean supportsVolume) {
}
