package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.lang.String;
import java.util.List;

public record PrivateUserObject(String country, @JsonProperty("display_name") String displayName,
        String email, @JsonProperty("explicit_content") Object explicitContent,
        @JsonProperty("external_urls") Object externalUrls, Object followers, String href,
        String id, List<ImageObject> images, String product, String type, String uri) {
}
