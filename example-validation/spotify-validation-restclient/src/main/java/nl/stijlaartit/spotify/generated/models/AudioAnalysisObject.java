package nl.stijlaartit.spotify.generated.models;

import java.util.List;

public record AudioAnalysisObject(AudioAnalysisObjectMeta meta, AudioAnalysisObjectTrack track,
        List<TimeIntervalObject> bars, List<TimeIntervalObject> beats, List<SectionObject> sections,
        List<SegmentObject> segments, List<TimeIntervalObject> tatums) {
}
