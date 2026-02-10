package nl.stijlaartit.spotify.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private <T> void assertSerializesSymmetrical(T original, Class<T> type) {
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void imageObject() {
        var original = new ImageObject("https://example.com/image.jpg", 640, 640);
        assertSerializesSymmetrical(original, ImageObject.class);
    }

    @Test
    void albumRestrictionObject() {
        var original = new AlbumRestrictionObject(AlbumRestrictionObjectReason.EXPLICIT);
        assertSerializesSymmetrical(original, AlbumRestrictionObject.class);
    }

    @Test
    void artistObject() {
        var images = List.of(new ImageObject("https://example.com/artist.jpg", 300, 300));
        var original = new ArtistObject(null, null, List.of("pop"),
                "https://api.spotify.com/v1/artists/1", "1", images,
                "Test Artist", 42, ArtistObjectType.ARTIST, "spotify:artist:1");
        assertSerializesSymmetrical(original, ArtistObject.class);
    }

    @Test
    void queueObjectOneOfs() {
        var track = sampleTrackObject();
        var episode = sampleEpisodeObject();
        var original = new QueueObject(track, List.of(episode));
        assertSerializesSymmetrical(original, QueueObject.class);
    }

    @Test
    void currentlyPlayingContextObjectItem() {
        var original = sampleTrackObject();
        assertSerializesSymmetrical(original, CurrentlyPlayingContextObjectItem.class);
    }

    @Test
    void currentlyPlayingObjectItem() {
        var original = sampleEpisodeObject();
        assertSerializesSymmetrical(original, CurrentlyPlayingObjectItem.class);
    }

    @Test
    void playlistTrackObjectTrack() {
        var original = sampleTrackObject();
        assertSerializesSymmetrical(original, PlaylistTrackObjectTrack.class);
    }

    @Test
    void queueObjectCurrentlyPlaying() {
        var original = sampleEpisodeObject();
        assertSerializesSymmetrical(original, QueueObjectCurrentlyPlaying.class);
    }

    @Test
    void queueObjectQueue() {
        var original = sampleTrackObject();
        assertSerializesSymmetrical(original, QueueObjectQueue.class);
    }

    @Test
    void addTracksToPlaylistRequest() {
        var original = new AddTracksToPlaylistRequest(List.of(), 1);
        assertSerializesSymmetrical(original, AddTracksToPlaylistRequest.class);
    }

    @Test
    void albumBase() {
        var original = new AlbumBase(AlbumBaseAlbumType.values()[0], 1, List.of(), sampleExternalUrls(),
                "value", "value", List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, AlbumBaseType.values()[0], "value");
        assertSerializesSymmetrical(original, AlbumBase.class);
    }

    @Test
    void albumBaseAlbumType() {
        var original = AlbumBaseAlbumType.values()[0];
        assertSerializesSymmetrical(original, AlbumBaseAlbumType.class);
    }

    @Test
    void albumBaseType() {
        var original = AlbumBaseType.values()[0];
        assertSerializesSymmetrical(original, AlbumBaseType.class);
    }

    @Test
    void albumObject() {
        var original = new AlbumObject(AlbumBaseAlbumType.values()[0], 1, List.of(), sampleExternalUrls(),
                "value", "value", List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, AlbumBaseType.values()[0], "value",
                List.of(), samplePagingSimplifiedTrackObject(), List.of(), sampleExternalIds(), List.of(),
                "value", 1);
        assertSerializesSymmetrical(original, AlbumObject.class);
    }

    @Test
    void albumRestrictionObjectReason() {
        var original = AlbumRestrictionObjectReason.values()[0];
        assertSerializesSymmetrical(original, AlbumRestrictionObjectReason.class);
    }

    @Test
    void artistDiscographyAlbumObject() {
        var original = new ArtistDiscographyAlbumObject(AlbumBaseAlbumType.values()[0], 1, List.of(),
                sampleExternalUrls(), "value", "value", List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, AlbumBaseType.values()[0], "value",
                List.of(), ArtistDiscographyAlbumObjectAlbumGroup.values()[0]);
        assertSerializesSymmetrical(original, ArtistDiscographyAlbumObject.class);
    }

    @Test
    void artistDiscographyAlbumObjectAlbumGroup() {
        var original = ArtistDiscographyAlbumObjectAlbumGroup.values()[0];
        assertSerializesSymmetrical(original, ArtistDiscographyAlbumObjectAlbumGroup.class);
    }

    @Test
    void artistObjectType() {
        var original = ArtistObjectType.values()[0];
        assertSerializesSymmetrical(original, ArtistObjectType.class);
    }

    @Test
    void audioAnalysisObject() {
        var original = new AudioAnalysisObject(null, null, List.of(), List.of(), List.of(), List.of(), List.of());
        assertSerializesSymmetrical(original, AudioAnalysisObject.class);
    }

    @Test
    void audioAnalysisObjectMeta() {
        var original = new AudioAnalysisObjectMeta("value", "value", "value", 1, 1L, new BigDecimal("1.0"), "value");
        assertSerializesSymmetrical(original, AudioAnalysisObjectMeta.class);
    }

    @Test
    void audioAnalysisObjectTrack() {
        var original = new AudioAnalysisObjectTrack(1, new BigDecimal("1.0"), "value", 1, 1, 1, 1, new BigDecimal("1.0"), new BigDecimal("1.0"), 1.0f, 1.0f, new BigDecimal("1.0"), 1, new BigDecimal("1.0"), 1, new BigDecimal("1.0"), 1, new BigDecimal("1.0"), "value", new BigDecimal("1.0"), "value", new BigDecimal("1.0"), "value", new BigDecimal("1.0"), "value", new BigDecimal("1.0"));
        assertSerializesSymmetrical(original, AudioAnalysisObjectTrack.class);
    }

    @Test
    void audioFeaturesObject() {
        var original = new AudioFeaturesObject(1.0f, "value", 1.0f, 1, 1.0f, "value", 1.0f, 1, 1.0f, 1.0f, 1, 1.0f, 1.0f, 1, "value", null, "value", 1.0f);
        assertSerializesSymmetrical(original, AudioFeaturesObject.class);
    }

    @Test
    void audioFeaturesObjectType() {
        var original = AudioFeaturesObjectType.values()[0];
        assertSerializesSymmetrical(original, AudioFeaturesObjectType.class);
    }

    @Test
    void audiobookBase() {
        var original = new AudiobookBase(List.of(), List.of(), List.of(), "value", "value", "value", true,
                sampleExternalUrls(), "value", "value", List.of(), List.of(), "value", "value", List.of(),
                "value", AudiobookBaseType.values()[0], "value", 1);
        assertSerializesSymmetrical(original, AudiobookBase.class);
    }

    @Test
    void audiobookBaseType() {
        var original = AudiobookBaseType.values()[0];
        assertSerializesSymmetrical(original, AudiobookBaseType.class);
    }

    @Test
    void audiobookObject() {
        var original = new AudiobookObject(List.of(), List.of(), List.of(), "value", "value", "value", true,
                sampleExternalUrls(), "value", "value", List.of(), List.of(), "value", "value", List.of(),
                "value", AudiobookBaseType.values()[0], "value", 1, samplePagingSimplifiedChapterObject());
        assertSerializesSymmetrical(original, AudiobookObject.class);
    }

    @Test
    void authorObject() {
        var original = new AuthorObject("value");
        assertSerializesSymmetrical(original, AuthorObject.class);
    }

    @Test
    void categoryObject() {
        var original = new CategoryObject("value", List.of(), "value", "value");
        assertSerializesSymmetrical(original, CategoryObject.class);
    }

    @Test
    void changePlaylistDetailsRequest() {
        var original = new ChangePlaylistDetailsRequest("value", true, true, "value");
        assertSerializesSymmetrical(original, ChangePlaylistDetailsRequest.class);
    }

    @Test
    void chapterBase() {
        var original = new ChapterBase("value", List.of(), 1, "value", "value", 1, true,
                sampleExternalUrls(), "value", "value", List.of(), true, List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, EpisodeObjectType.values()[0], "value", null);
        assertSerializesSymmetrical(original, ChapterBase.class);
    }

    @Test
    void chapterObject() {
        var original = new ChapterObject("value", List.of(), 1, "value", "value", 1, true,
                sampleExternalUrls(), "value", "value", List.of(), true, List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, EpisodeObjectType.values()[0], "value",
                null, sampleSimplifiedAudiobookObject());
        assertSerializesSymmetrical(original, ChapterObject.class);
    }

    @Test
    void chapterRestrictionObject() {
        var original = new ChapterRestrictionObject("value");
        assertSerializesSymmetrical(original, ChapterRestrictionObject.class);
    }

    @Test
    void contextObject() {
        var original = new ContextObject("value", "value", null, "value");
        assertSerializesSymmetrical(original, ContextObject.class);
    }

    @Test
    void copyrightObject() {
        var original = new CopyrightObject("value", "value");
        assertSerializesSymmetrical(original, CopyrightObject.class);
    }

    @Test
    void createPlaylistForUserRequest() {
        var original = new CreatePlaylistForUserRequest("value", true, true, "value");
        assertSerializesSymmetrical(original, CreatePlaylistForUserRequest.class);
    }

    @Test
    void createPlaylistRequest() {
        var original = new CreatePlaylistRequest("value", true, true, "value");
        assertSerializesSymmetrical(original, CreatePlaylistRequest.class);
    }

    @Test
    void currentlyPlayingContextObject() {
        var original = new CurrentlyPlayingContextObject(null, "value", true, null, 1L, 1, true, null, "value", null);
        assertSerializesSymmetrical(original, CurrentlyPlayingContextObject.class);
    }

    @Test
    void currentlyPlayingObject() {
        var original = new CurrentlyPlayingObject(null, 1L, 1, true, null, "value", null);
        assertSerializesSymmetrical(original, CurrentlyPlayingObject.class);
    }

    @Test
    void cursorObject() {
        var original = new CursorObject("value", "value");
        assertSerializesSymmetrical(original, CursorObject.class);
    }

    @Test
    void cursorPagingObject() {
        var original = new CursorPagingObject("value", 1, "value", null, 1);
        assertSerializesSymmetrical(original, CursorPagingObject.class);
    }

    @Test
    void cursorPagingPlayHistoryObject() {
        var original = new CursorPagingPlayHistoryObject("value", 1, "value", null, 1, List.of());
        assertSerializesSymmetrical(original, CursorPagingPlayHistoryObject.class);
    }

    @Test
    void cursorPagingSimplifiedArtistObject() {
        var original = new CursorPagingSimplifiedArtistObject("value", 1, "value", null, 1, List.of());
        assertSerializesSymmetrical(original, CursorPagingSimplifiedArtistObject.class);
    }

    @Test
    void deviceObject() {
        var original = new DeviceObject("value", true, true, true, "value", "value", 1, true);
        assertSerializesSymmetrical(original, DeviceObject.class);
    }

    @Test
    void disallowsObject() {
        var original = new DisallowsObject(true, true, true, true, true, true, true, true, true, true);
        assertSerializesSymmetrical(original, DisallowsObject.class);
    }

    @Test
    void episodeBase() {
        var original = new EpisodeBase("value", "value", "value", 1, true, sampleExternalUrls(), "value",
                "value", List.of(), true, true, "value", List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, EpisodeObjectType.values()[0], "value", null);
        assertSerializesSymmetrical(original, EpisodeBase.class);
    }

    @Test
    void episodeObjectReleaseDatePrecision() {
        var original = EpisodeObjectReleaseDatePrecision.values()[0];
        assertSerializesSymmetrical(original, EpisodeObjectReleaseDatePrecision.class);
    }

    @Test
    void episodeObjectType() {
        var original = EpisodeObjectType.values()[0];
        assertSerializesSymmetrical(original, EpisodeObjectType.class);
    }

    @Test
    void episodeRestrictionObject() {
        var original = new EpisodeRestrictionObject("value");
        assertSerializesSymmetrical(original, EpisodeRestrictionObject.class);
    }

    @Test
    void errorObject() {
        var original = new ErrorObject(1, "value");
        assertSerializesSymmetrical(original, ErrorObject.class);
    }

    @Test
    void explicitContentSettingsObject() {
        var original = new ExplicitContentSettingsObject(true, true);
        assertSerializesSymmetrical(original, ExplicitContentSettingsObject.class);
    }

    @Test
    void externalIdObject() {
        var original = new ExternalIdObject("value", "value", "value");
        assertSerializesSymmetrical(original, ExternalIdObject.class);
    }

    @Test
    void externalUrlObject() {
        var original = new ExternalUrlObject("value");
        assertSerializesSymmetrical(original, ExternalUrlObject.class);
    }

    @Test
    void followArtistsUsersRequest() {
        var original = new FollowArtistsUsersRequest(List.of());
        assertSerializesSymmetrical(original, FollowArtistsUsersRequest.class);
    }

    @Test
    void followPlaylistRequest() {
        var original = new FollowPlaylistRequest(true);
        assertSerializesSymmetrical(original, FollowPlaylistRequest.class);
    }

    @Test
    void followersObject() {
        var original = new FollowersObject("value", 1);
        assertSerializesSymmetrical(original, FollowersObject.class);
    }

    @Test
    void linkedTrackObject() {
        var original = new LinkedTrackObject(null, "value", "value", "value", "value");
        assertSerializesSymmetrical(original, LinkedTrackObject.class);
    }

    @Test
    void narratorObject() {
        var original = new NarratorObject("value");
        assertSerializesSymmetrical(original, NarratorObject.class);
    }

    @Test
    void pagingArtistDiscographyAlbumObject() {
        var original = new PagingArtistDiscographyAlbumObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingArtistDiscographyAlbumObject.class);
    }

    @Test
    void pagingArtistObject() {
        var original = new PagingArtistObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingArtistObject.class);
    }

    @Test
    void pagingFeaturedPlaylistObject() {
        var original = new PagingFeaturedPlaylistObject("value", null);
        assertSerializesSymmetrical(original, PagingFeaturedPlaylistObject.class);
    }

    @Test
    void pagingObject() {
        var original = new PagingObject("value", 1, "value", 1, "value", 1);
        assertSerializesSymmetrical(original, PagingObject.class);
    }

    @Test
    void pagingPlaylistObject() {
        var original = new PagingPlaylistObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingPlaylistObject.class);
    }

    @Test
    void pagingPlaylistTrackObject() {
        var original = new PagingPlaylistTrackObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingPlaylistTrackObject.class);
    }

    @Test
    void pagingSavedAlbumObject() {
        var original = new PagingSavedAlbumObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSavedAlbumObject.class);
    }

    @Test
    void pagingSavedAudiobookObject() {
        var original = new PagingSavedAudiobookObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSavedAudiobookObject.class);
    }

    @Test
    void pagingSavedEpisodeObject() {
        var original = new PagingSavedEpisodeObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSavedEpisodeObject.class);
    }

    @Test
    void pagingSavedShowObject() {
        var original = new PagingSavedShowObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSavedShowObject.class);
    }

    @Test
    void pagingSavedTrackObject() {
        var original = new PagingSavedTrackObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSavedTrackObject.class);
    }

    @Test
    void pagingSimplifiedAlbumObject() {
        var original = new PagingSimplifiedAlbumObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSimplifiedAlbumObject.class);
    }

    @Test
    void pagingSimplifiedAudiobookObject() {
        var original = new PagingSimplifiedAudiobookObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSimplifiedAudiobookObject.class);
    }

    @Test
    void pagingSimplifiedChapterObject() {
        var original = new PagingSimplifiedChapterObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSimplifiedChapterObject.class);
    }

    @Test
    void pagingSimplifiedEpisodeObject() {
        var original = new PagingSimplifiedEpisodeObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSimplifiedEpisodeObject.class);
    }

    @Test
    void pagingSimplifiedShowObject() {
        var original = new PagingSimplifiedShowObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSimplifiedShowObject.class);
    }

    @Test
    void pagingSimplifiedTrackObject() {
        var original = new PagingSimplifiedTrackObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingSimplifiedTrackObject.class);
    }

    @Test
    void pagingTrackObject() {
        var original = new PagingTrackObject("value", 1, "value", 1, "value", 1, List.of());
        assertSerializesSymmetrical(original, PagingTrackObject.class);
    }

    @Test
    void playHistoryObject() {
        var original = new PlayHistoryObject(null, OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null);
        assertSerializesSymmetrical(original, PlayHistoryObject.class);
    }

    @Test
    void playlistObject() {
        var original = new PlaylistObject(true, "value", null, "value", "value", List.of(), "value", null, true, "value", null, "value", "value", null);
        assertSerializesSymmetrical(original, PlaylistObject.class);
    }

    @Test
    void playlistOwnerObject() {
        var original = new PlaylistOwnerObject(null, "value", "value", null, "value", "value");
        assertSerializesSymmetrical(original, PlaylistOwnerObject.class);
    }

    @Test
    void playlistTrackObject() {
        var original = new PlaylistTrackObject(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, true, null);
        assertSerializesSymmetrical(original, PlaylistTrackObject.class);
    }

    @Test
    void playlistTracksRefObject() {
        var original = new PlaylistTracksRefObject("value", 1);
        assertSerializesSymmetrical(original, PlaylistTracksRefObject.class);
    }

    @Test
    void playlistUserObject() {
        var original = new PlaylistUserObject(null, "value", "value", null, "value");
        assertSerializesSymmetrical(original, PlaylistUserObject.class);
    }

    @Test
    void privateUserObject() {
        var original = new PrivateUserObject("value", "value", "value", null, null, null, "value", "value", List.of(), "value", "value", "value");
        assertSerializesSymmetrical(original, PrivateUserObject.class);
    }

    @Test
    void publicUserObject() {
        var original = new PublicUserObject("value", null, null, "value", "value", List.of(), null, "value");
        assertSerializesSymmetrical(original, PublicUserObject.class);
    }

    @Test
    void publicUserObjectType() {
        var original = PublicUserObjectType.values()[0];
        assertSerializesSymmetrical(original, PublicUserObjectType.class);
    }

    @Test
    void recommendationSeedObject() {
        var original = new RecommendationSeedObject(1, 1, "value", "value", 1, "value");
        assertSerializesSymmetrical(original, RecommendationSeedObject.class);
    }

    @Test
    void recommendationsObject() {
        var original = new RecommendationsObject(List.of(), List.of());
        assertSerializesSymmetrical(original, RecommendationsObject.class);
    }

    @Test
    void removeAlbumsUserRequest() {
        var original = new RemoveAlbumsUserRequest(List.of());
        assertSerializesSymmetrical(original, RemoveAlbumsUserRequest.class);
    }

    @Test
    void removeEpisodesUserRequest() {
        var original = new RemoveEpisodesUserRequest(List.of());
        assertSerializesSymmetrical(original, RemoveEpisodesUserRequest.class);
    }

    @Test
    void removeShowsUserRequest() {
        var original = new RemoveShowsUserRequest(List.of());
        assertSerializesSymmetrical(original, RemoveShowsUserRequest.class);
    }

    @Test
    void removeTracksPlaylistRequest() {
        var original = new RemoveTracksPlaylistRequest(List.of(), "value");
        assertSerializesSymmetrical(original, RemoveTracksPlaylistRequest.class);
    }

    @Test
    void removeTracksPlaylistRequestTracks() {
        var original = new RemoveTracksPlaylistRequestTracks("value");
        assertSerializesSymmetrical(original, RemoveTracksPlaylistRequestTracks.class);
    }

    @Test
    void removeTracksUserRequest() {
        var original = new RemoveTracksUserRequest(List.of());
        assertSerializesSymmetrical(original, RemoveTracksUserRequest.class);
    }

    @Test
    void reorderOrReplacePlaylistsTracksRequest() {
        var original = new ReorderOrReplacePlaylistsTracksRequest(List.of(), 1, 1, 1, "value");
        assertSerializesSymmetrical(original, ReorderOrReplacePlaylistsTracksRequest.class);
    }

    @Test
    void resumePointObject() {
        var original = new ResumePointObject(true, 1);
        assertSerializesSymmetrical(original, ResumePointObject.class);
    }

    @Test
    void saveAlbumsUserRequest() {
        var original = new SaveAlbumsUserRequest(List.of());
        assertSerializesSymmetrical(original, SaveAlbumsUserRequest.class);
    }

    @Test
    void saveEpisodesUserRequest() {
        var original = new SaveEpisodesUserRequest(List.of());
        assertSerializesSymmetrical(original, SaveEpisodesUserRequest.class);
    }

    @Test
    void saveShowsUserRequest() {
        var original = new SaveShowsUserRequest(List.of());
        assertSerializesSymmetrical(original, SaveShowsUserRequest.class);
    }

    @Test
    void saveTracksUserRequest() {
        var original = new SaveTracksUserRequest(List.of(), List.of());
        assertSerializesSymmetrical(original, SaveTracksUserRequest.class);
    }

    @Test
    void saveTracksUserRequestTimestampedIds() {
        var original = new SaveTracksUserRequestTimestampedIds("value", OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        assertSerializesSymmetrical(original, SaveTracksUserRequestTimestampedIds.class);
    }

    @Test
    void savedAlbumObject() {
        var original = new SavedAlbumObject(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null);
        assertSerializesSymmetrical(original, SavedAlbumObject.class);
    }

    @Test
    void savedAudiobookObject() {
        var original = new SavedAudiobookObject(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null);
        assertSerializesSymmetrical(original, SavedAudiobookObject.class);
    }

    @Test
    void savedEpisodeObject() {
        var original = new SavedEpisodeObject(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null);
        assertSerializesSymmetrical(original, SavedEpisodeObject.class);
    }

    @Test
    void savedShowObject() {
        var original = new SavedShowObject(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null);
        assertSerializesSymmetrical(original, SavedShowObject.class);
    }

    @Test
    void savedTrackObject() {
        var original = new SavedTrackObject(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null);
        assertSerializesSymmetrical(original, SavedTrackObject.class);
    }

    @Test
    void sectionObject() {
        var original = new SectionObject(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), 1, new BigDecimal("1.0"), null, new BigDecimal("1.0"), 1, new BigDecimal("1.0"));
        assertSerializesSymmetrical(original, SectionObject.class);
    }

    @Test
    void sectionObjectMode() {
        var original = SectionObjectMode.values()[0];
        assertSerializesSymmetrical(original, SectionObjectMode.class);
    }

    @Test
    void segmentObject() {
        var original = new SegmentObject(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), List.of(), List.of());
        assertSerializesSymmetrical(original, SegmentObject.class);
    }

    @Test
    void showBase() {
        var original = new ShowBase(List.of(), List.of(), "value", "value", true, sampleExternalUrls(),
                "value", "value", List.of(), true, List.of(), "value", "value", "value",
                ShowBaseType.values()[0], "value", 1);
        assertSerializesSymmetrical(original, ShowBase.class);
    }

    @Test
    void showBaseType() {
        var original = ShowBaseType.values()[0];
        assertSerializesSymmetrical(original, ShowBaseType.class);
    }

    @Test
    void showObject() {
        var original = new ShowObject(List.of(), List.of(), "value", "value", true, sampleExternalUrls(),
                "value", "value", List.of(), true, List.of(), "value", "value", "value",
                ShowBaseType.values()[0], "value", 1, samplePagingSimplifiedEpisodeObject());
        assertSerializesSymmetrical(original, ShowObject.class);
    }

    @Test
    void simplifiedAlbumObject() {
        var original = new SimplifiedAlbumObject(AlbumBaseAlbumType.values()[0], 1, List.of(),
                sampleExternalUrls(), "value", "value", List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, AlbumBaseType.values()[0], "value",
                List.of());
        assertSerializesSymmetrical(original, SimplifiedAlbumObject.class);
    }

    @Test
    void simplifiedArtistObject() {
        var original = new SimplifiedArtistObject(null, "value", "value", "value", null, "value");
        assertSerializesSymmetrical(original, SimplifiedArtistObject.class);
    }

    @Test
    void simplifiedAudiobookObject() {
        var original = new SimplifiedAudiobookObject(List.of(), List.of(), List.of(), "value", "value", "value",
                true, sampleExternalUrls(), "value", "value", List.of(), List.of(), "value", "value",
                List.of(), "value", AudiobookBaseType.values()[0], "value", 1);
        assertSerializesSymmetrical(original, SimplifiedAudiobookObject.class);
    }

    @Test
    void simplifiedChapterObject() {
        var original = new SimplifiedChapterObject("value", List.of(), 1, "value", "value", 1, true,
                sampleExternalUrls(), "value", "value", List.of(), true, List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, EpisodeObjectType.values()[0], "value",
                null);
        assertSerializesSymmetrical(original, SimplifiedChapterObject.class);
    }

    @Test
    void simplifiedEpisodeObject() {
        var original = new SimplifiedEpisodeObject("value", "value", "value", 1, true, sampleExternalUrls(),
                "value", "value", List.of(), true, true, "value", List.of(), "value", "value",
                EpisodeObjectReleaseDatePrecision.values()[0], null, EpisodeObjectType.values()[0], "value", null);
        assertSerializesSymmetrical(original, SimplifiedEpisodeObject.class);
    }

    @Test
    void simplifiedPlaylistObject() {
        var original = new SimplifiedPlaylistObject(true, "value", null, "value", "value", List.of(), "value", null, true, "value", null, "value", "value");
        assertSerializesSymmetrical(original, SimplifiedPlaylistObject.class);
    }

    @Test
    void simplifiedShowObject() {
        var original = new SimplifiedShowObject(List.of(), List.of(), "value", "value", true,
                sampleExternalUrls(), "value", "value", List.of(), true, List.of(), "value", "value",
                "value", ShowBaseType.values()[0], "value", 1);
        assertSerializesSymmetrical(original, SimplifiedShowObject.class);
    }

    @Test
    void simplifiedTrackObject() {
        var original = new SimplifiedTrackObject(List.of(), List.of(), 1, 1, true, null, "value", "value", true, null, null, "value", "value", 1, "value", "value", true);
        assertSerializesSymmetrical(original, SimplifiedTrackObject.class);
    }

    @Test
    void startAUsersPlaybackRequest() {
        var original = new StartAUsersPlaybackRequest("value", List.of(), null, 1);
        assertSerializesSymmetrical(original, StartAUsersPlaybackRequest.class);
    }

    @Test
    void timeIntervalObject() {
        var original = new TimeIntervalObject(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"));
        assertSerializesSymmetrical(original, TimeIntervalObject.class);
    }

    @Test
    void trackObjectType() {
        var original = TrackObjectType.values()[0];
        assertSerializesSymmetrical(original, TrackObjectType.class);
    }

    @Test
    void trackRestrictionObject() {
        var original = new TrackRestrictionObject("value");
        assertSerializesSymmetrical(original, TrackRestrictionObject.class);
    }

    @Test
    void transferAUsersPlaybackRequest() {
        var original = new TransferAUsersPlaybackRequest(List.of(), true);
        assertSerializesSymmetrical(original, TransferAUsersPlaybackRequest.class);
    }

    @Test
    void unfollowArtistsUsersRequest() {
        var original = new UnfollowArtistsUsersRequest(List.of());
        assertSerializesSymmetrical(original, UnfollowArtistsUsersRequest.class);
    }

    private TrackObject sampleTrackObject() {
        return new TrackObject(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Test Track",
                null,
                null,
                null,
                TrackObjectType.TRACK,
                "spotify:track:1",
                null
        );
    }

    private EpisodeObject sampleEpisodeObject() {
        return new EpisodeObject(
                "https://example.com/preview.mp3",
                "value",
                "value",
                1,
                true,
                sampleExternalUrls(),
                "value",
                "value",
                List.of(),
                true,
                true,
                null,
                List.of("en"),
                "Test Episode",
                "2024-01-01",
                EpisodeObjectReleaseDatePrecision.values()[0],
                null,
                EpisodeObjectType.EPISODE,
                "spotify:episode:1",
                null,
                sampleSimplifiedShowObject()
        );
    }

    private ExternalUrlObject sampleExternalUrls() {
        return new ExternalUrlObject("https://example.com");
    }

    private ExternalIdObject sampleExternalIds() {
        return new ExternalIdObject("value", "value", "value");
    }

    private PagingSimplifiedTrackObject samplePagingSimplifiedTrackObject() {
        return new PagingSimplifiedTrackObject("value", 1, "value", 0, "value", 1, List.of());
    }

    private PagingSimplifiedChapterObject samplePagingSimplifiedChapterObject() {
        return new PagingSimplifiedChapterObject("value", 1, "value", 0, "value", 1, List.of());
    }

    private PagingSimplifiedEpisodeObject samplePagingSimplifiedEpisodeObject() {
        return new PagingSimplifiedEpisodeObject("value", 1, "value", 0, "value", 1, List.of());
    }

    private SimplifiedAudiobookObject sampleSimplifiedAudiobookObject() {
        return new SimplifiedAudiobookObject(List.of(), List.of(), List.of(), "value", "value", null, true,
                sampleExternalUrls(), "value", "value", List.of(), List.of("en"), "value", "value", List.of(),
                "value", AudiobookBaseType.values()[0], "value", 1);
    }

    private SimplifiedShowObject sampleSimplifiedShowObject() {
        return new SimplifiedShowObject(List.of(), List.of(), "value", "value", true, sampleExternalUrls(),
                "value", "value", List.of(), true, List.of("en"), "value", "value", "value",
                ShowBaseType.values()[0], "value", 1);
    }
}
