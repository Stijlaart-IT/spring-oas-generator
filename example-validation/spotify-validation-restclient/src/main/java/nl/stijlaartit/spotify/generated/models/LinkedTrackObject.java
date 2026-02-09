package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.lang.String;

public record LinkedTrackObject(@JsonProperty("external_urls") Object externalUrls, String href,
        String id, String type, String uri) {
}
