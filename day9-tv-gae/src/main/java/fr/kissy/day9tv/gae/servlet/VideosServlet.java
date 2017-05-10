package fr.kissy.day9tv.gae.servlet;

import fr.kissy.day9tv.gae.proto.VideosProto;
import fr.kissy.day9tv.gae.service.VideosService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * VideosServlet servlet.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideosServlet.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class VideosServlet extends HttpServlet {
    private static final String APPLICATION_PROTOBUF = "application/x-protobuf";
    private static final String NEW_VIDEOS_PARAM = "new";
    private static final String CURSOR_PARAM = "cursor";
    private static final String UTF8 = "utf-8";

    private VideosService videosService = VideosService.getSingleton();

    /**
     * @inheritDoc
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Boolean newVideos = BooleanUtils.toBoolean(request.getParameter(NEW_VIDEOS_PARAM));
        String cursor = request.getParameter(CURSOR_PARAM);

        VideosProto.Videos videos;
        if (newVideos && StringUtils.isNotBlank(cursor)) {
            videos = videosService.getNewVideosProtoBuilder(cursor).build();
        } else {
            videos = videosService.getVideosProtoBuilder(cursor).build();
        }

        response.setContentType(APPLICATION_PROTOBUF);
        response.setCharacterEncoding(UTF8);
        if (videos != null) {
            response.setContentLength(videos.getSerializedSize());
            videos.writeTo(response.getOutputStream());
        }
        response.flushBuffer();
    }
}
