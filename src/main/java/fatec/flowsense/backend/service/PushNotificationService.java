package fatec.flowsense.backend.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import fatec.flowsense.backend.entities.PushToken;
import fatec.flowsense.backend.repository.PushTokenRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PushNotificationService {

    private final PushTokenRepository pushTokenRepository;

    public PushNotificationService(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
    }

    public void enviarAlertaVazamento(
            String ownerEmail,
            Long sensorId,
            String sensorName,
            double fluxoAtual
    ) {
        System.out.println("=================================");
        System.out.println("TENTANDO ENVIAR PUSH");
        System.out.println("Dono: " + ownerEmail);
        System.out.println("Sensor: " + sensorName);
        System.out.println("Fluxo: " + fluxoAtual);
        System.out.println("=================================");

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Firebase NÃO inicializado. Push não enviado.");
            return;
        }

        List<PushToken> tokens = pushTokenRepository.findByOwnerEmail(ownerEmail);

        System.out.println("Tokens encontrados: " + tokens.size());

        if (tokens.isEmpty()) {
            System.out.println("Nenhum token push encontrado para: " + ownerEmail);
            return;
        }

        for (PushToken pushToken : tokens) {
            enviarParaToken(
                    pushToken.getToken(),
                    sensorId,
                    sensorName,
                    fluxoAtual
            );
        }
    }

    private void enviarParaToken(
            String token,
            Long sensorId,
            String sensorName,
            double fluxoAtual
    ) {
        try {
            System.out.println("Enviando push para token:");
            System.out.println(token);

            String title = "Possível vazamento detectado";

            String body = "Sensor " + sensorName +
                    " detectou fluxo contínuo de " +
                    String.format("%.2f", fluxoAtual) +
                    " L/min.";

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .putData("type", "leak_alert")
                    .putData("sensorId", String.valueOf(sensorId))
                    .putData("sensorName", sensorName)
                    .putData("fluxoAtual", String.valueOf(fluxoAtual))
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .setNotification(
                                            AndroidNotification.builder()
                                                    .setChannelId("leak_alerts")
                                                    .setSound("default")
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            System.out.println("Push enviado com sucesso:");
            System.out.println(response);

        } catch (FirebaseMessagingException e) {
            System.out.println("ERRO FIREBASE AO ENVIAR PUSH:");
            System.out.println("Messaging error code: " + e.getMessagingErrorCode());
            System.out.println("Message: " + e.getMessage());

            if (
                    e.getMessagingErrorCode() != null &&
                    (
                            e.getMessagingErrorCode().name().equals("UNREGISTERED") ||
                            e.getMessagingErrorCode().name().equals("INVALID_ARGUMENT")
                    )
            ) {
                removerTokenInvalido(token);
            }

            e.printStackTrace();

        } catch (Exception e) {
            System.out.println("ERRO AO ENVIAR PUSH:");
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void removerTokenInvalido(String token) {
        pushTokenRepository.findByToken(token).ifPresent(pushToken -> {
            pushTokenRepository.delete(pushToken);

            System.out.println("Token FCM inválido removido do banco:");
            System.out.println(token);
        });
    }
}