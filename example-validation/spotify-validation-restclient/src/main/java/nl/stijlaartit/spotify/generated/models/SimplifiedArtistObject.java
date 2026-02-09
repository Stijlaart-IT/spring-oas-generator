package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.lang.String;

public record SimplifiedArtistObject(@JsonProperty("external_urls") Object externalUrls,
        String href, String id, String name, ArtistObjectType type, String uri) {
}
