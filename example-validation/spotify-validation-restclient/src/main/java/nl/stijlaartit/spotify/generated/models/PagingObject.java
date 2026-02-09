package nl.stijlaartit.spotify.generated.models;

import java.lang.Integer;
import java.lang.String;

public record PagingObject(String href, Integer limit, String next, Integer offset, String previous,
        Integer total) {
}
