package fr.kissy.day9tv.gae.service;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import fr.kissy.day9tv.gae.dao.LastVideoDAO;
import fr.kissy.day9tv.gae.dao.VideoDAO;
import fr.kissy.day9tv.gae.dto.VideoEntryDTO;
import fr.kissy.day9tv.gae.model.LastVideo;
import fr.kissy.day9tv.gae.model.Video;
import fr.kissy.day9tv.gae.servlet.worker.NotificationsServlet;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * Video List Parser Service.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: UpdaterService.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class UpdaterService {
    private static final Logger LOGGER = Logger.getLogger(UpdaterService.class.getName());
    private static final String REPORT_EMAIL = "guillaume.lebiller@gmail.com";
    private static final String REPORT_EMAIL_NAME = "Guillaume Le Biller";
    private static final String REPORT_EMAIL_TITLE = "Android :: Day9 TV - Video(s) saved";
    private static UpdaterService singleton;

    private ParserService parserService = ParserService.getInstance();

    private VideoDAO videoDAO = VideoDAO.getInstance();
    private LastVideoDAO lastVideoDAO = LastVideoDAO.getInstance();

    /**
     * Get the video list from the Day9 TV archives.
     *
     * @param page The starting page to retrieve.
     * @return The video list.
     */
    public boolean updateVideos(int page) {
        LastVideo lastVideo = lastVideoDAO.get();
        if (lastVideo == null) {
            lastVideo = new LastVideo();
            lastVideoDAO.saveOrUpdate(lastVideo);
        }

        List<VideoEntryDTO> videoEntryDTOs = parserService.parseNewerVideos(page, lastVideo.getTimestamp());
        if (videoEntryDTOs.isEmpty()) {
            LOGGER.warning("No newer videos found.");
            return true;
        }

        List<Video> videos = Lists.newArrayList();
        for (VideoEntryDTO videoEntryDTO : videoEntryDTOs) {
            Video video = parserService.parseVideoEntry(videoEntryDTO);
            if (video != null) {
                parserService.parseTypeAndSubType(video);
                videos.add(video);
                if (lastVideo.getTimestamp() < video.getTimestamp()) {
                    lastVideo.setTimestamp(video.getTimestamp());
                }
            }
        }

        LOGGER.info("Found " + videoEntryDTOs.size() + " new videos, saving it.");
        videoDAO.saveOrUpdate(videos);
        lastVideoDAO.saveOrUpdate(lastVideo);

        // Send email for normal update
        if (page <= 1) {
            Queue queue = QueueFactory.getQueue(NotificationsServlet.WORKER_QUEUE_NAME);
            queue.add(withUrl(NotificationsServlet.WORKER_QUEUE_URL).method(TaskOptions.Method.GET));
            sendSavedVideosMail(videos);
        }

        return true;
    }

    /**
     * Send the list of saved video to email.
     * 
     * @param videos The video list saved.
     */
    private void sendSavedVideosMail(List<Video> videos) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        StringBuilder msgBody = new StringBuilder();
        msgBody.append("Video(s) saved at ").append(new Date()).append("\n");
        msgBody.append("Number of video saved : ").append(videos.size()).append("\n");
        msgBody.append("\n");

        for (Video video : videos) {
            msgBody.append("\t").append(video.getType()).append(" | ").append(video.getSubType()).append(" || ")
                    .append(video.getTitle()).append(" | ").append(video.getSubTitle()).append(" || ")
                    .append(video.getVideoParts().size()).append(" parts\n");
        }

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(REPORT_EMAIL, REPORT_EMAIL_NAME));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(REPORT_EMAIL, REPORT_EMAIL_NAME));
            msg.setSubject(REPORT_EMAIL_TITLE);
            msg.setText(msgBody.toString());
            Transport.send(msg);
        } catch (Exception e) {
            LOGGER.severe("Exception while sending mail " + e.getMessage());
        }
    }

    public synchronized static UpdaterService getInstance() {
        if (singleton == null) {
            singleton = new UpdaterService();
        }
        return singleton;
    }
}
