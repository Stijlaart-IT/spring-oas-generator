package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.lang.String;
import java.util.List;

public record PublicUserObject(@JsonProperty("display_name") String displayName,
        @JsonProperty("external_urls") Object externalUrls, Object followers, String href,
        String id, List<ImageObject> images, PublicUserObjectType type, String uri) {
}
