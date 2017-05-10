package fr.kissy.day9tv.gae.service;

import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import fr.kissy.day9tv.gae.dao.LastVideoDAO;
import fr.kissy.day9tv.gae.dao.VideoDAO;
import fr.kissy.day9tv.gae.dto.VideoEntryDTO;
import fr.kissy.day9tv.gae.model.LastVideo;
import fr.kissy.day9tv.gae.model.Video;
import fr.kissy.day9tv.gae.model.VideoFile;
import fr.kissy.day9tv.gae.model.VideoPart;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * VideosServlet parser service test.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: UpdaterServiceTest.java 152 2011-12-28 15:16:50Z kissy $
 */
public class UpdaterServiceTest {
    @Mock
    private VideoDAO videoDAO;
    @Mock
    private LastVideoDAO lastVideoDAO;
    @Mock
    private ParserService parserService;
    @InjectMocks
    private UpdaterService updaterService = new UpdaterService();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void release() {
        Mockito.reset(videoDAO, lastVideoDAO, parserService);
    }

    @Test
    public void testUpdateVideosNullLastVideo() {
        int page = 35;
        when(lastVideoDAO.get()).thenReturn(null);
        when(parserService.parseNewerVideos(page, 0L)).thenReturn(Lists.newArrayList(
                new VideoEntryDTO("Title", "DetailUrl", 10L),
                new VideoEntryDTO("Title", "DetailUrl", 11L)
        ));
        when(parserService.parseVideoEntry(any(VideoEntryDTO.class))).thenReturn(new Video(new VideoEntryDTO("Title", "DetailUrl", 10L)));

        boolean result = updaterService.updateVideos(page);
        verify(lastVideoDAO, times(2)).saveOrUpdate(any(LastVideo.class));
        verify(parserService, times(1)).parseNewerVideos(page, 0L);
        verify(parserService, times(2)).parseVideoEntry(any(VideoEntryDTO.class));
        verify(videoDAO, times(1)).saveOrUpdate(anyList());
        Assert.assertTrue(result);
    }

    @Test
    public void testUpdateVideosNoVideos() {
        int page = 35;
        when(lastVideoDAO.get()).thenReturn(new LastVideo(10L));
        when(parserService.parseNewerVideos(page, 10L)).thenReturn(Lists.<VideoEntryDTO>newArrayList());

        boolean result = updaterService.updateVideos(page);
        verify(parserService, times(1)).parseNewerVideos(page, 10L);
        verifyNoMoreInteractions(parserService);
        verifyZeroInteractions(videoDAO);
        Assert.assertTrue(result);
    }
}
