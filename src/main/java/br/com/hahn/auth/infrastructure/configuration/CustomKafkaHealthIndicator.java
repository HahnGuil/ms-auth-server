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

    @Override
    public Health health() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient client = AdminClient.create(props)) {
            client.listTopics().names().get();
            return Health.up().withDetail("kafka", "Available").build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Health.down(e).withDetail("kafka", "Interrupted").build();
        } catch (ExecutionException | RuntimeException e) {
            return Health.down(e).withDetail("kafka", "Not available").build();
        }
    }
}
