<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.kissy.day9tv.gae.model.LastVideo" %>
<%@ page import="fr.kissy.day9tv.gae.dao.LastVideoDAO" %>
<html>
<head>
    <title>Day9 TV Admin</title>
    <link type="text/css" rel="stylesheet" href="/css/admin.css"/>
</head>
<body>
<div class="g-doc">
    <%
        LastVideoDAO lastVideoDAO = LastVideoDAO.getInstance();
        LastVideo lastVideo = lastVideoDAO.get();
        if (lastVideo == null) {
            lastVideo = new LastVideo();
            lastVideoDAO.saveOrUpdate(lastVideo);
        }
    %>
    <form action="/admin/last-video" method="post" id="edit-last-video" class="ae-form">
        <h2 class="ae-section-header">Edit Last Video</h2>
        <div class="g-section-margin">
            <div class="g-section">
                <div class="g-unit">
                    <div class="ae-input-row ">
                        <div class="ae-label-row">
                            <label for="lastVideo.timestamp">Last Video Timestamp :</label>
                        </div>
                        <div class="ae-input-row">
                            <input type="text" name="lastVideo.timestamp" id="lastVideo.timestamp" value="<%= lastVideo.getTimestamp() %>" size="16" maxlength="10">
                            <div class="ae-field-hint">Store the timestamp of the last saved video.</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="ae-btn-row">
                <input id="submit-last-video" type="submit" value="Save Settings" class="goog-button">
            </div>
        </div>
    </form>

    <form action="/admin/settings" method="post" id="update-task" class="ae-form">
        <h2 class="ae-section-header">Update Task</h2>
        <div class="g-section-margin">
            <div class="g-section">
                <div class="g-unit">
                    <div class="ae-input-row ">
                        <div class="ae-label-row">
                            <label for="taskQueue.page">Starting page :</label>
                        </div>
                        <div class="ae-input-row">
                            <span>
                                <input type="text" name="taskQueue.page" id="taskQueue.page" value="0">
                            </span>
                            <div class="ae-field-hint">Set the starting page & launch a new Update task.</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="ae-btn-row">
                <input id="submit-settings" type="submit" value="Save Settings" class="goog-button">
            </div>
        </div>
    </form>

    <form action="/admin/notify" method="post" id="notifications-task" class="ae-form">
        <h2 class="ae-section-header">Notification Task</h2>

        <div class="g-section-margin">
            <div class="g-section">
                <div class="g-unit">
                    <div class="ae-input-row">
                        <div class="ae-label-row">
                            <label for="device.id">Device ID :</label>
                        </div>
                        <div class="ae-input-row">
                            <input type="text" name="device.id" id="device.id" value="" size="16" maxlength="16">
                            <div class="ae-field-hint">Notify the device using C2DM with it's device id. Use "ALL" to notify all devices.</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="ae-btn-row">
                <input id="submit-notify" type="submit" value="Notify Device" class="goog-button">
            </div>
        </div>
    </form>
</div>

</body>
</html>