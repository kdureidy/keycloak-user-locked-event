package com.iqvia.sampleeventlistenerprovider.provider;

import org.jboss.logging.Logger;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.theme.FreeMarkerUtil;

import java.util.HashMap;
import java.util.Map;


public class SampleEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private static final Logger logger = Logger.getLogger(SampleEventListenerProvider.class);

    public SampleEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        logger.info("Error:======================================================================" + event.getError());

        if(event.getType() == EventType.LOGIN_ERROR && "user_temporarily_disabled".equals(event.getError())) {

            String userId = event.getUserId();
            RealmModel realm = session.realms().getRealm(event.getRealmId());

            logger.info("realm name:=============================" + realm.getName());
            logger.info("user id:================================" + userId);

            if(userId == null) {
                return;
            }

            UserProvider users = this.session.getProvider(UserProvider.class);
            RealmModel realmModel = session.getContext().getRealm();
            UserModel user = users.getUserById(event.getUserId(), realmModel);

            try {
                if(user.getEmail() != null && !user.getEmail().isEmpty()) {
                    sendEmail(user, realmModel);
                }
            } catch (EmailException e) {
                logger.error("Unable to send email \n" + e.getMessage());
            }
        }
    }

    private void sendEmail(UserModel userModel, RealmModel realmModel) throws EmailException {

        logger.infof("Sending an email to user email: %s", userModel.getEmail());
        sendFreeMakerEmail(userModel, realmModel);

//        sendDefaultEmail(userModel);
    }

    private void sendDefaultEmail(UserModel userModel) throws EmailException {
        DefaultEmailSenderProvider senderProvider = new DefaultEmailSenderProvider(session);
        senderProvider.send(
                session.getContext().getRealm().getSmtpConfig(),
                userModel,
                "test",
                "body test",
                "html test"
        );
    }

    private void sendFreeMakerEmail(UserModel userModel, RealmModel realmModel) {
        FreeMarkerUtil freeMarker = new FreeMarkerUtil();
        FreeMarkerEmailTemplateProvider freeMarkerEmailTemplateProvider = new FreeMarkerEmailTemplateProvider(session, freeMarker);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", new ProfileBean(userModel));
        attributes.put("realmName", realmModel.getName());

        try {
            freeMarkerEmailTemplateProvider.send("Subject", "template-name.ftl", attributes);
        } catch (EmailException e) {
            logger.error("Unable to send email");
        }


    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

        System.out.println("Admin Event Occurred:" + toString(adminEvent));
    }

    @Override
    public void close() {

    }

    private String toString(Event event) {

        StringBuilder sb = new StringBuilder();


        sb.append("type=");

        sb.append(event.getType());

        sb.append(", realmId=");

        sb.append(event.getRealmId());

        sb.append(", clientId=");

        sb.append(event.getClientId());

        sb.append(", userId=");

        sb.append(event.getUserId());

        sb.append(", ipAddress=");

        sb.append(event.getIpAddress());


        if (event.getError() != null) {

            sb.append(", error=");

            sb.append(event.getError());

        }


        if (event.getDetails() != null) {

            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {

                sb.append(", ");

                sb.append(e.getKey());

                if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {

                    sb.append("=");

                    sb.append(e.getValue());

                } else {

                    sb.append("='");

                    sb.append(e.getValue());

                    sb.append("'");

                }

            }

        }


        return sb.toString();

    }


    private String toString(AdminEvent adminEvent) {

        StringBuilder sb = new StringBuilder();


        sb.append("operationType=");

        sb.append(adminEvent.getOperationType());

        sb.append(", realmId=");

        sb.append(adminEvent.getAuthDetails().getRealmId());

        sb.append(", clientId=");

        sb.append(adminEvent.getAuthDetails().getClientId());

        sb.append(", userId=");

        sb.append(adminEvent.getAuthDetails().getUserId());

        sb.append(", ipAddress=");

        sb.append(adminEvent.getAuthDetails().getIpAddress());

        sb.append(", resourcePath=");

        sb.append(adminEvent.getResourcePath());


        if (adminEvent.getError() != null) {

            sb.append(", error=");

            sb.append(adminEvent.getError());

        }


        return sb.toString();

    }
}