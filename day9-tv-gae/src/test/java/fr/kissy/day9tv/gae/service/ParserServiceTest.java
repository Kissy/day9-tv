package fr.kissy.day9tv.gae.service;

import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.dto.VideoEntryDTO;
import fr.kissy.day9tv.gae.model.Video;
import junit.framework.Assert;
import net.htmlparser.jericho.Source;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VideosServlet parser service test.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: UpdaterServiceTest.java 152 2011-12-28 15:16:50Z kissy $
 */
public class ParserServiceTest {
    @Mock
    private SourceService sourceService;
    @InjectMocks
    private ParserService parserService = new ParserService();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void release() {
        Mockito.reset(sourceService);
    }

    @Test
    public void testParseNewestArchives() throws IOException {
        when(sourceService.get(Constants.VIDEO_LIST_URL + 35)).thenReturn(new Source(this.getClass().getResource("/fr/kissy/day9tv/gae/ArchivePage35.html")));

        List<VideoEntryDTO> results = parserService.parseNewerVideos(35, 0L);
        verify(sourceService, times(1)).get(Constants.VIDEO_LIST_URL + 35);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(results.size(), 15);
        VideoEntryDTO oneArchive = results.get(4);

        Mockito.reset(sourceService);

        when(sourceService.get(Constants.VIDEO_LIST_URL + 35)).thenReturn(new Source(this.getClass().getResource("/fr/kissy/day9tv/gae/ArchivePage35.html")));

        results = parserService.parseNewerVideos(35, oneArchive.getTimestamp());
        verify(sourceService, times(1)).get(Constants.VIDEO_LIST_URL + 35);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(results.size(), 10);
    }

    @Test
    public void testParseVideoEntry() throws IOException {
        String detailUrl = "/d/Day9/day9-daily-1-flash-vs-hero-tvz/";
        when(sourceService.get(Constants.BASE_URL + detailUrl)).thenReturn(new Source(this.getClass().getResource("/fr/kissy/day9tv/gae/VideoEntry1.html")));

        VideoEntryDTO videoEntryDTO = new VideoEntryDTO("Day[9] Daily #1 – Flash vs Hero TvZ", detailUrl, 10L);

        Video video = parserService.parseVideoEntry(videoEntryDTO);
        verify(sourceService, times(1)).get(Constants.BASE_URL + detailUrl);
        Assert.assertNotNull(video);
        Assert.assertEquals(video.getTitle(), "Day[9] Daily #1");
        Assert.assertEquals(video.getSubTitle(), "Flash vs Hero TvZ");
        Assert.assertEquals(video.getTimestamp(), Long.valueOf(10));
        Assert.assertEquals(video.getDescription(), "The very first Day[9] Daily!\n" +
                "Day[9] focuses on Terran mid game aggression forcing Zerg to be out of position. Terran's aggressive push also forces Zerg to be out of his base and at home defending.\n");
        Assert.assertEquals(video.getVideoParts().size(), 1);
        Assert.assertEquals(video.getTags().size(), 17);
    }
}
