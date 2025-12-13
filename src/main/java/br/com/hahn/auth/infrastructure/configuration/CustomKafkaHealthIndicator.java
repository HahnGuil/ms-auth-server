package br.com.hahn.auth.infrastructure.configuration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class CustomKafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Checks the health of the Kafka cluster by attempting to list topics via an AdminClient.
     * <p>
     * This method creates a temporary AdminClient configured with the injected
     * `bootstrapServers` and tries to retrieve topic names. On success, it returns
     * a UP health status with a "kafka" detail set to "Available".
     * <p>
     * If the current thread is interrupted while waiting for the topic listing,
     * the interrupt status is restored and a DOWN health status is returned with
     * a "kafka" detail set to "Interrupted".
     * <p>
     * For execution or runtime failures when contacting Kafka, a DOWN health status
     * is returned with a "kafka" detail set to "Not available".
     *
     * @author HahnGuil
     * @return Health status representing Kafka availability
     */
    @Override
    public Health health() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient client = AdminClient.create(props)) {
            client.listTopics().names().get();
            return Health.up().withDetail("Kafka", "Available").build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Health.down(e).withDetail("Kafka", "Interrupted").build();
        } catch (ExecutionException | RuntimeException e) {
            return Health.down(e).withDetail("Kafka", "Not available").build();
        }
    }
}