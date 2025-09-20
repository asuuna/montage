package app.timeline;

import app.domain.TrackType;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineServiceTest {
    private Timeline timeline;
    private TimelineService service;
    private TimelineTrack videoTrack;

    @BeforeEach
    void setUp() {
        timeline = new Timeline();
        service = new TimelineService(timeline, Duration.ofMillis(100));
        videoTrack = service.addTrack(TrackType.VIDEO);
    }

    @Test
    void addClipPlacesClipWithSnap() {
        service.addClip(videoTrack.getId(), Path.of("clip.mp4"), Duration.ofSeconds(1), Duration.ofSeconds(3));
        assertEquals(1, videoTrack.getClips().size());
        assertEquals(Duration.ofSeconds(1), videoTrack.getClips().get(0).getStart());
    }

    @Test
    void splitClipCreatesTwoSegments() {
        service.addClip(videoTrack.getId(), Path.of("clip.mp4"), Duration.ofSeconds(0), Duration.ofSeconds(10));
        UUID clipId = videoTrack.getClips().get(0).getId();
        service.splitClip(videoTrack.getId(), clipId, Duration.ofSeconds(4));
        assertEquals(2, videoTrack.getClips().size());
        assertTrue(videoTrack.getClips().stream().anyMatch(c -> c.getDuration().equals(Duration.ofSeconds(4))));
    }

    @Test
    void trimClipUpdatesStartAndDuration() {
        service.addClip(videoTrack.getId(), Path.of("clip.mp4"), Duration.ZERO, Duration.ofSeconds(10));
        TimelineClip clip = videoTrack.getClips().get(0);
        service.trimClip(videoTrack.getId(), clip.getId(), Duration.ofSeconds(2), Duration.ofSeconds(5));
        assertEquals(Duration.ofSeconds(2), clip.getStart());
        assertEquals(Duration.ofSeconds(5), clip.getDuration());
    }

    @Test
    void rippleDeleteShiftsSubsequentClips() {
        service.addClip(videoTrack.getId(), Path.of("a.mp4"), Duration.ZERO, Duration.ofSeconds(5));
        service.addClip(videoTrack.getId(), Path.of("b.mp4"), Duration.ofSeconds(6), Duration.ofSeconds(5));
        TimelineClip second = videoTrack.getClips().get(1);
        Duration originalStart = second.getStart();
        service.rippleDelete(videoTrack.getId(), Duration.ofSeconds(2), Duration.ofSeconds(4));
        assertTrue(second.getStart().compareTo(originalStart.minus(Duration.ofSeconds(2))) == 0);
        service.getCommandStack().undo();
        assertEquals(originalStart, second.getStart());
    }

    @Test
    void commandStackSupportsUndoRedo() {
        service.addClip(videoTrack.getId(), Path.of("clip.mp4"), Duration.ZERO, Duration.ofSeconds(5));
        assertTrue(service.getCommandStack().canUndo());
        service.getCommandStack().undo();
        assertEquals(0, videoTrack.getClips().size());
        service.getCommandStack().redo();
        assertEquals(1, videoTrack.getClips().size());
    }

    @Test
    void snapReturnsNearestAnchor() {
        service.addClip(videoTrack.getId(), Path.of("clip.mp4"), Duration.ofSeconds(5), Duration.ofSeconds(2));
        Duration snapped = service.snap(Duration.ofSeconds(5).plusMillis(50));
        assertEquals(Duration.ofSeconds(5), snapped);
    }

    @Test
    void addTrackCreatesTrack() {
        TimelineTrack audioTrack = service.addTrack(TrackType.AUDIO);
        assertNotNull(audioTrack);
        assertTrue(timeline.getTracks().contains(audioTrack));
    }
}
