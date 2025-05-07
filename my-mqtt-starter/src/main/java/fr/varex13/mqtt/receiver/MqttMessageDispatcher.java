package fr.varex13.mqtt.receiver;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.log.LogAccessor;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MqttMessageDispatcher implements MqttCallback, BeanPostProcessor {

    private final LogAccessor logger = new LogAccessor(LogFactory.getLog(getClass()));

    private final MqttAsyncClient client;
    private final ObjectMapper objectMapper;
    private final HandlersInfos handlersInfos = new HandlersInfos();
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    public MqttMessageDispatcher(final MqttAsyncClient client, final ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.client.setCallback(this);
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {

        if (this.nonAnnotatedClasses.contains(bean.getClass())) {
            return bean;
        }

        final Class<?> targetClass = AopUtils.getTargetClass(bean);
        final Set<MqttListener> classLevelListeners = findListenerAnnotationsByAnnotatedElement(targetClass);
        final boolean hasClassLevelListeners = !classLevelListeners.isEmpty();
        final Map<Method, Set<MqttListener>> methodLevelListener = findMethodLevelListenersAnnotations(targetClass);
        final boolean hasMethodLevelListeners = !methodLevelListener.isEmpty();

        if (!hasMethodLevelListeners && !hasClassLevelListeners) {
            this.nonAnnotatedClasses.add(bean.getClass());
            this.logger.trace(() -> "No @MqttListener annotations found on bean type: " + bean.getClass());
            return bean;
        }

        manageMethodLevelListeners(bean, hasMethodLevelListeners, methodLevelListener);
        manageClassLevelListeners(bean, hasClassLevelListeners, classLevelListeners, targetClass);

        return bean;
    }

    private Map<Method, Set<MqttListener>> findMethodLevelListenersAnnotations(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<Set<MqttListener>>) method -> {
                    Set<MqttListener> listenerMethods = findListenerAnnotationsByAnnotatedElement(method);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
    }

    private Set<MqttListener> findListenerAnnotationsByAnnotatedElement(AnnotatedElement element) {
        Set<MqttListener> listeners = new HashSet<>();
        MqttListener mqttListener = AnnotatedElementUtils.findMergedAnnotation(element, MqttListener.class);
        if (mqttListener != null) {
            listeners.add(mqttListener);
        }
        MqttListeners mqttListeners = AnnotationUtils.findAnnotation(element, MqttListeners.class);
        if (mqttListeners != null) {
            listeners.addAll(Arrays.asList(mqttListeners.value()));
        }
        return listeners;
    }

    private void manageMethodLevelListeners(Object bean, boolean hasMethodLevelListeners, Map<Method, Set<MqttListener>> methodLevelListener) {
        if (!hasMethodLevelListeners) {
            return;
        }

        this.logger.trace(() -> "methodLevelListener " + methodLevelListener);
        methodLevelListener.forEach((method, value) ->
                value.forEach(listener -> processMqttListener(listener, method, bean))
        );
    }

    private void manageClassLevelListeners(Object bean, boolean hasClassLevelListeners, Collection<MqttListener> classLevelListeners, Class<?> targetClass) {
        if (!hasClassLevelListeners) {
            return;
        }

        this.logger.trace(() -> "classLevelListeners " + classLevelListeners);
        Set<Method> methodsWithHandler = MethodIntrospector.selectMethods(targetClass,
                (ReflectionUtils.MethodFilter) method ->
                        AnnotationUtils.findAnnotation(method, MqttHandler.class) != null);

        if (methodsWithHandler.size() != 1) {
            throw new IllegalStateException("One and only one mqtt listener methods in bean allowed: " + bean);
        }

        this.logger.trace(() -> "methodsWithHandler " + methodsWithHandler);
        processMultiMethodListeners(classLevelListeners, methodsWithHandler, bean);

    }

    private void processMultiMethodListeners(Collection<MqttListener> classLevelListeners,
                                             Set<Method> multiMethods, Object bean) {
        multiMethods.stream()
                .<Consumer<? super MqttListener>>map(method -> mqttListener -> processMqttListener(mqttListener, method, bean))
                .forEach(classLevelListeners::forEach);

    }

    protected void processMqttListener(MqttListener mqttListener, Method method, Object bean) {
        try {
            final Class<?> type = mqttListener.payloadType();
            if (method.getParameterCount() != 1 || !method.getParameterTypes()[0].isAssignableFrom(type)) {
                throw new IllegalArgumentException("Invalid method signature for @MqttListener on " + method);
            }

            final String topic = mqttListener.topic();
            handlersInfos.put(topic, new HandlerInfo(bean, method, type, mqttListener.qos()));
            client.subscribe(topic, mqttListener.qos());
        } catch (MqttException e) {
            throw new MqttReceiverException(" ", e);
        }
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage message) {

        try {
            final HandlerInfo info = handlersInfos.get(topic);
            if (info == null) {
                logger.warn(() -> "No handler found for topic: " + topic);
                return;
            }
            final Object payload = objectMapper.readValue(message.getPayload(), info.payloadType);
            info.method.invoke(info.bean, payload);

        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e, () -> "Error while invoking handler for topic: " + topic);
            // eventuellement notifier / metrique d'erreur
        } catch (IOException e) {
            logger.error(() -> String.format("Invalid JSON for topic '%s': %s", topic, new String(message.getPayload())));
            // eventuellement notifier / metrique d'erreur
        }

    }

    @Override
    public void disconnected(final MqttDisconnectResponse mqttDisconnectResponse) {
        logger.warn(() -> {
            final String reason = mqttDisconnectResponse.getReasonString();
            final int reasonCode = mqttDisconnectResponse.getReturnCode();
            return String.format("MQTT client disconnected. Reason code: %d, reason: %s", reasonCode, reason);
        });

        try {
            logger.info(() -> "Attempting to reconnect to MQTT broker...");
            client.reconnect();
            logger.info(() -> "Reconnection successful.");
        } catch (MqttException e) {
            logger.error(e, () -> "Failed to reconnect to MQTT broker after disconnection.");
            // Ici, tu pourrais notifier un syst�me de monitoring, envoyer une alerte, etc.
        }
    }

    @Override
    public void mqttErrorOccurred(final MqttException exception) {
        logger.error(exception, () -> String.format(
                "An error occurred in the MQTT client: reason code=%d, message=%s",
                exception.getReasonCode(), exception.getMessage()
        ));


        if (!client.isConnected()) {
            try {
                logger.info(() -> "Client is not connected. Attempting to reconnect...");
                client.reconnect();
                logger.info(() -> "Reconnection after error successful.");
            } catch (MqttException reconnectException) {
                logger.error(reconnectException, () -> "Reconnection failed after mqttErrorOccurred.");
            }
        }
    }

    @Override
    public void deliveryComplete(final IMqttToken messageId) {
        logger.debug(() -> "Delivery complete for message(s) with topics: " + Arrays.toString(messageId.getTopics()));
    }

    @Override
    public void connectComplete(final boolean reconnect, final String serverURI) {
        if (reconnect) {
            logger.info(() -> "Reconnected to MQTT broker at: " + serverURI);

            // Certains brokers (selon cleanStart/cleanSession) ne gardent pas les abonnements
            // donc on peut devoir les refaire ici :
            reSubscribeToTopics();
        } else {
            logger.info(() -> "Connected to MQTT broker at: " + serverURI);
        }
    }

    private void reSubscribeToTopics() {
        handlersInfos.handlers.keySet().forEach(topic -> {
            try {
                client.subscribe(topic, 1); // ou le QoS d'origine si tu le conserves
                logger.info(() -> "Re-subscribed to topic: " + topic);
            } catch (MqttException e) {
                logger.error(e, () -> "Failed to re-subscribe to topic: " + topic);
            }
        });
    }

    @Override
    public void authPacketArrived(final int reasonCode, final MqttProperties properties) {
        logger.info(() -> "AUTH packet arrived with reason code: " + reasonCode);

        if (properties != null && !properties.getUserProperties().isEmpty()) {
            properties.getUserProperties().forEach(userProp ->
                    logger.info(() -> "Auth property: " + userProp.getKey() + " = " + userProp.getValue())
            );
        }
    }

    private record HandlerInfo(Object bean, Method method, Class<?> payloadType, int qos) {
    }

    private static final class HandlersInfos {

        private final Map<String, HandlerInfo> handlers = new ConcurrentHashMap<>();

        public void put(String topic, HandlerInfo handlerInfo) {
            handlers.put(topic, handlerInfo);
        }

        public HandlerInfo get(String topic) {
            return handlers.getOrDefault(topic, null);
        }
    }
}