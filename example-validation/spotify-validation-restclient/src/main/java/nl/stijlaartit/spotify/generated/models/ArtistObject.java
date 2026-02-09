package nl.stijlaartit.spotify.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.List;

public record ArtistObject(@JsonProperty("external_urls") Object externalUrls, Object followers,
        List<String> genres, String href, String id, List<ImageObject> images, String name,
        Integer popularity, ArtistObjectType type, String uri) {
}
