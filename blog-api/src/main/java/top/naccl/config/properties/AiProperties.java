package top.naccl.config.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiProperties {
	private String apiKey;
	private String model = "qwen-plus";
	private double temperature = 0.7;
	private int maxTokens = 2048;
}
